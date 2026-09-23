(function () {
    var root = document.getElementById('roleCalendar');
    if (!root) return;

    var events = [];
    try {
        var payload = document.getElementById('calendarEventsData');
        events = JSON.parse((payload && payload.textContent) ? payload.textContent : '[]');
    } catch (e) {
        events = [];
    }

    var monthLabel = document.getElementById('calMonthLabel');
    var grid = document.getElementById('calGrid');
    var list = document.getElementById('calEventList');
    var cursor = new Date();
    cursor.setDate(1);

    var weekdays = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];

    function pad(n) { return String(n).padStart(2, '0'); }
    function iso(d) {
        return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
    }
    function eventsOn(dateStr) {
        return events.filter(function (ev) { return ev.date === dateStr; });
    }

    function render() {
        var year = cursor.getFullYear();
        var month = cursor.getMonth();
        monthLabel.textContent = 'Tháng ' + (month + 1) + ' / ' + year;

        var first = new Date(year, month, 1);
        var startOffset = (first.getDay() + 6) % 7;
        var daysInMonth = new Date(year, month + 1, 0).getDate();
        var todayStr = iso(new Date());

        var html = weekdays.map(function (d) {
            return '<div class="cal-dow">' + d + '</div>';
        }).join('');

        for (var i = 0; i < startOffset; i++) {
            html += '<div class="cal-day empty"></div>';
        }
        for (var day = 1; day <= daysInMonth; day++) {
            var dateStr = year + '-' + pad(month + 1) + '-' + pad(day);
            var dayEvents = eventsOn(dateStr);
            var cls = 'cal-day' + (dateStr === todayStr ? ' today' : '');
            html += '<div class="' + cls + '"><div class="cal-day-num">' + day + '</div>';
            dayEvents.slice(0, 3).forEach(function (ev) {
                html += '<span class="cal-dot ' + ev.type + '" title="' + ev.title + '">' + ev.title + '</span>';
            });
            if (dayEvents.length > 3) {
                html += '<span class="cal-dot sv">+' + (dayEvents.length - 3) + '</span>';
            }
            html += '</div>';
        }
        grid.innerHTML = html;

        var monthPrefix = year + '-' + pad(month + 1);
        var monthEvents = events.filter(function (ev) { return ev.date.indexOf(monthPrefix) === 0; })
            .sort(function (a, b) { return a.date.localeCompare(b.date); });

        if (!monthEvents.length) {
            list.innerHTML = '<p class="text-muted mb-0">Không có mốc thời gian trong tháng này.</p>';
            return;
        }
        list.innerHTML = monthEvents.map(function (ev) {
            return '<div class="cal-event-item">' +
                '<div class="d-flex justify-content-between gap-2">' +
                '<strong>' + ev.title + '</strong>' +
                '<span class="text-muted small">' + ev.date.split('-').reverse().join('/') + (ev.time ? ' ' + ev.time : '') + '</span>' +
                '</div>' +
                (ev.detail ? '<div class="small text-muted mt-1">' + ev.detail + '</div>' : '') +
                '</div>';
        }).join('');
    }

    document.getElementById('calPrev').addEventListener('click', function () {
        cursor.setMonth(cursor.getMonth() - 1);
        render();
    });
    document.getElementById('calNext').addEventListener('click', function () {
        cursor.setMonth(cursor.getMonth() + 1);
        render();
    });
    render();
})();
