package com.hcmute.topicmanagement.service;

import com.hcmute.topicmanagement.dto.CalendarEventDTO;
import com.hcmute.topicmanagement.security.CustomUserDetails;

import java.util.List;

public interface CalendarService {
    List<CalendarEventDTO> getEventsForUser(CustomUserDetails user);
}
