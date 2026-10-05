(function () {
    const calendarRoot = document.getElementById('periodCalendar');
    const kanbanRoot = document.getElementById('periodKanban');
    if (!calendarRoot || !kanbanRoot) return;

    const periods = Array.from(document.querySelectorAll('[data-kanban-period]')).map(function (element) {
        return element.dataset;
    });
    const stageDefinitions = [
        { key: 'teacher', start: 'teacherStart', end: 'teacherEnd', color: '#2b7a78' },
        { key: 'approval', start: 'approvalStart', end: 'approvalEnd', color: '#c07a28' },
        { key: 'student', start: 'studentStart', end: 'studentEnd', color: '#4776a8' }
    ];
    const statusKeys = [
        ['DRAFT', 'period.status.draft'],
        ['PHASE1_TEACHER_REG', 'period.status.teacher'],
        ['PHASE1_DEAN_REVIEW', 'period.status.approval'],
        ['PUBLISHED_TOPICS', 'period.status.published'],
        ['PHASE2_STUDENT_REG', 'period.status.student'],
        ['EXECUTION', 'period.status.execution'],
        ['DEFENSE_EVALUATION', 'period.status.defense'],
        ['COMPLETED', 'period.status.completed'],
        ['CANCELLED', 'period.status.cancelled']
    ];
    const colors = {
        DRAFT: '#667085',
        PHASE1_TEACHER_REG: '#16867a',
        PHASE1_DEAN_REVIEW: '#c77d18',
        PUBLISHED_TOPICS: '#628b22',
        PHASE2_STUDENT_REG: '#3478c5',
        EXECUTION: '#7655b5',
        DEFENSE_EVALUATION: '#c14f78',
        COMPLETED: '#258558',
        CANCELLED: '#c44336'
    };
    let calendar;

    function followingDay(date) {
        const parts = date.split('-').map(Number);
        const next = new Date(parts[0], parts[1] - 1, parts[2] + 1);
        return next.getFullYear() + '-' + String(next.getMonth() + 1).padStart(2, '0')
            + '-' + String(next.getDate()).padStart(2, '0');
    }

    function calendarEvents() {
        return periods.flatMap(function (period) {
            return stageDefinitions.filter(function (stage) {
                return period[stage.start] && period[stage.end];
            }).map(function (stage) {
                const label = window.appTranslate('period.stage.' + stage.key);
                return {
                    title: period.name + ' - ' + label,
                    start: period[stage.start],
                    end: followingDay(period[stage.end]),
                    allDay: true,
                    color: stage.color,
                    contrastColor: '#ffffff'
                };
            });
        });
    }

    function renderKanban() {
        kanbanRoot.replaceChildren();
        kanbanRoot.classList.add('period-kanban-board');
        statusKeys.forEach(function (status) {
            const title = window.appTranslate(status[1]);
            const column = document.createElement('section');
            column.className = 'period-kanban-column';
            column.style.setProperty('--period-column-color', colors[status[0]]);

            const heading = document.createElement('h6');
            heading.className = 'period-kanban-heading';
            heading.textContent = title;
            column.appendChild(heading);

            const list = document.createElement('div');
            list.className = 'period-kanban-items';
            list.dataset.status = status[0];
            list.setAttribute('aria-label', title);

            const boardPeriods = periods.filter(function (period) {
                return period.status === status[0];
            });
            if (boardPeriods.length === 0) {
                const empty = document.createElement('p');
                empty.className = 'period-kanban-empty';
                empty.textContent = window.appTranslate('period.emptyColumn');
                list.appendChild(empty);
            }

            boardPeriods.forEach(function (period) {
                const card = document.createElement('article');
                card.className = 'period-kanban-card';
                card.dataset.status = status[0];
                card.dataset.statusUrl = period.statusUrl;

                const name = document.createElement('strong');
                name.textContent = period.name;
                card.appendChild(name);

                const dates = document.createElement('div');
                dates.className = 'small text-muted mt-1';
                dates.textContent = (period.teacherStart || '') + ' - ' + (period.studentEnd || '');
                card.appendChild(dates);
                list.appendChild(card);
            });

            column.appendChild(list);
            kanbanRoot.appendChild(column);

            if (window.Sortable) {
                window.Sortable.create(list, {
                    group: 'period-statuses',
                    animation: 150,
                    ghostClass: 'period-kanban-card--ghost',
                    draggable: '.period-kanban-card',
                    onEnd: function (event) {
                        const card = event.item;
                        const newStatus = event.to.dataset.status;
                        if (card.dataset.status === newStatus) return;

                        const form = document.createElement('form');
                        form.method = 'post';
                        form.action = card.dataset.statusUrl;
                        const input = document.createElement('input');
                        input.type = 'hidden';
                        input.name = 'status';
                        input.value = newStatus;
                        form.appendChild(input);
                        document.body.appendChild(form);
                        form.submit();
                    }
                });
            }
        });
    }

    function renderCalendar() {
        if (!window.FullCalendar) return;
        const locale = document.documentElement.lang === 'en' ? 'en' : 'vi';
        if (calendar) {
            calendar.setOption('locale', locale);
            calendar.removeAllEvents();
            calendar.addEventSource(calendarEvents());
            return;
        }
        calendar = new window.FullCalendar.Calendar(calendarRoot, {
            initialView: 'dayGridMonth',
            themeSystem: 'classic',
            height: 'auto',
            locale: locale,
            firstDay: 1,
            events: calendarEvents()
        });
        calendar.render();
    }

    function render() {
        renderCalendar();
        renderKanban();
    }

    window.addEventListener('app-language-changed', render);
    render();
})();
