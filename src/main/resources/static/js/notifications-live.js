(function () {
    const badge = document.querySelector('[data-notification-unread]');
    if (!badge) return;

    function updateUnreadCount() {
        fetch('/notifications/unread-count', { credentials: 'same-origin' })
            .then(function (response) {
                if (!response.ok) throw new Error('Could not load unread notification count');
                return response.json();
            })
            .then(function (data) {
                const count = Number(data.count) || 0;
                badge.textContent = count > 99 ? '99+' : String(count);
                badge.hidden = count === 0;
                badge.setAttribute('aria-label', count + ' thông báo chưa đọc');
            })
            .catch(function (error) {
                console.error(error);
            });
    }

    function showNotification(message) {
        const toast = document.createElement('a');
        toast.className = 'notification-live-toast';
        toast.href = message.url || '/notifications';
        const title = document.createElement('strong');
        title.textContent = message.title || 'Thông báo mới';
        const content = document.createElement('span');
        content.textContent = message.content || 'Bạn có thông báo mới.';
        toast.append(title, content);
        document.body.appendChild(toast);
        window.setTimeout(function () { toast.remove(); }, 8000);
    }

    function connect() {
        const socketUrl = new URL('/ws/notifications', window.location.href);
        socketUrl.protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const socket = new WebSocket(socketUrl);
        socket.addEventListener('message', function (event) {
            try {
                showNotification(JSON.parse(event.data));
                updateUnreadCount();
            } catch (error) {
                console.error('Could not process live notification', error);
            }
        });
        socket.addEventListener('close', function () {
            window.setTimeout(connect, 5000);
        });
        socket.addEventListener('error', function () {
            socket.close();
        });
    }

    updateUnreadCount();
    connect();
    window.addEventListener('focus', updateUnreadCount);
})();
