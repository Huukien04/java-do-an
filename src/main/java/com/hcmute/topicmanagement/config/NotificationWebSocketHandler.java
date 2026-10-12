package com.hcmute.topicmanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Set<WebSocketSession>> sessionsByUsername = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (session.getPrincipal() == null) {
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication required"));
            } catch (IOException exception) {
                logger.warn("Could not close unauthenticated notification socket", exception);
            }
            return;
        }
        sessionsByUsername.computeIfAbsent(session.getPrincipal().getName(), ignored -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (session.getPrincipal() == null) {
            return;
        }
        Set<WebSocketSession> sessions = sessionsByUsername.get(session.getPrincipal().getName());
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionsByUsername.remove(session.getPrincipal().getName(), sessions);
            }
        }
    }

    public void sendToUsers(Collection<String> usernames, Object message) {
        final String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not serialize notification websocket message", exception);
        }

        usernames.forEach(username -> {
            Set<WebSocketSession> sessions = sessionsByUsername.get(username);
            if (sessions == null) {
                return;
            }
            sessions.removeIf(session -> {
                if (!session.isOpen()) {
                    return true;
                }
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(payload));
                    }
                    return false;
                } catch (IOException exception) {
                    logger.warn("Could not send notification to connected user {}", username, exception);
                    try {
                        session.close(CloseStatus.SERVER_ERROR);
                    } catch (IOException closeException) {
                        logger.warn("Could not close failed notification socket for user {}", username, closeException);
                    }
                    return true;
                }
            });
        });
    }
}
