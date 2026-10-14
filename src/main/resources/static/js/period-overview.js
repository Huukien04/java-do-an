(function () {
    const periodRows = Array.from(document.querySelectorAll('[data-period-row]'));
    const searchInput = document.getElementById('periodSearch');
    const statusFilter = document.getElementById('periodStatusFilter');
    const typeFilter = document.getElementById('periodTypeFilter');
    const resultsCount = document.getElementById('periodResultsCount');
    const pageInfo = document.getElementById('periodPageInfo');
    const previousPage = document.getElementById('periodPreviousPage');
    const nextPage = document.getElementById('periodNextPage');
    const noResults = document.getElementById('periodNoResults');
    const pageSize = 10;
    let currentPage = 1;

    function normalizeSearchText(value) {
        return value.toLowerCase().replace(/đ/g, 'd').normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    }

    function renderPeriodList() {
        if (!searchInput || !statusFilter || !typeFilter) return;

        const query = normalizeSearchText(searchInput.value.trim());
        const selectedStatus = statusFilter.value;
        const selectedType = typeFilter.value;
        const filteredRows = periodRows.filter(function (row) {
            const matchesStatus = selectedStatus === 'all'
                || (selectedStatus === 'active'
                    ? row.dataset.status !== 'COMPLETED' && row.dataset.status !== 'CANCELLED'
                    : row.dataset.status === selectedStatus);
            const matchesType = selectedType === 'all' || row.dataset.type === selectedType;
            const matchesQuery = !query || normalizeSearchText(row.textContent).includes(query);
            return matchesStatus && matchesType && matchesQuery;
        });
        const pageCount = Math.max(1, Math.ceil(filteredRows.length / pageSize));
        currentPage = Math.min(currentPage, pageCount);

        periodRows.forEach(function (row) {
            row.hidden = true;
        });
        filteredRows.slice((currentPage - 1) * pageSize, currentPage * pageSize)
            .forEach(function (row, index) {
                row.hidden = false;
                row.querySelector('.period-index').textContent = String((currentPage - 1) * pageSize + index + 1);
            });

        if (noResults) noResults.hidden = periodRows.length === 0 || filteredRows.length > 0;
        const translate = window.appTranslate || function (key) { return key; };
        if (resultsCount) resultsCount.textContent = filteredRows.length + ' ' + translate('period.count');
        if (pageInfo) {
            const first = filteredRows.length === 0 ? 0 : (currentPage - 1) * pageSize + 1;
            const last = Math.min(currentPage * pageSize, filteredRows.length);
            pageInfo.textContent = translate('period.showing')
                .replace('{from}', first).replace('{to}', last).replace('{total}', filteredRows.length);
        }
        if (previousPage) previousPage.disabled = currentPage <= 1;
        if (nextPage) nextPage.disabled = currentPage >= pageCount;
    }

    if (searchInput && statusFilter && typeFilter) {
        [searchInput, statusFilter, typeFilter].forEach(function (control) {
            control.addEventListener('input', function () {
                currentPage = 1;
                renderPeriodList();
            });
            control.addEventListener('change', function () {
                currentPage = 1;
                renderPeriodList();
            });
        });
        if (previousPage) previousPage.addEventListener('click', function () {
            currentPage = Math.max(1, currentPage - 1);
            renderPeriodList();
        });
        if (nextPage) nextPage.addEventListener('click', function () {
            currentPage += 1;
            renderPeriodList();
        });
        renderPeriodList();
    }

    let activeStatusDropdown = null;

    function positionStatusDropdown() {
        if (!activeStatusDropdown) return;

        const toggleRect = activeStatusDropdown.toggle.getBoundingClientRect();
        const menu = activeStatusDropdown.menu;
        const menuWidth = menu.offsetWidth;
        const menuHeight = menu.offsetHeight;
        const left = Math.max(8, Math.min(toggleRect.right - menuWidth, window.innerWidth - menuWidth - 8));
        let top = toggleRect.bottom + 4;
        if (top + menuHeight > window.innerHeight - 8 && toggleRect.top - menuHeight - 4 >= 8) {
            top = toggleRect.top - menuHeight - 4;
        }

        menu.style.setProperty('--period-menu-top', top + 'px');
        menu.style.setProperty('--period-menu-left', left + 'px');
    }

    function getStatusToggle(event) {
        const dropdown = event.target.closest('.period-status-control');
        return dropdown ? dropdown.querySelector('.period-status-toggle') : null;
    }

    document.addEventListener('shown.bs.dropdown', function (event) {
        const toggle = getStatusToggle(event);
        if (!toggle) return;

        const menu = toggle.parentElement.querySelector('.period-status-menu');
        if (!menu) return;
        menu.classList.add('period-status-menu-floating');
        activeStatusDropdown = { toggle: toggle, menu: menu };
        positionStatusDropdown();
    });

    document.addEventListener('hidden.bs.dropdown', function (event) {
        const toggle = getStatusToggle(event);
        if (!toggle) return;

        const menu = toggle.parentElement.querySelector('.period-status-menu');
        if (menu) {
            menu.classList.remove('period-status-menu-floating');
            menu.style.removeProperty('--period-menu-top');
            menu.style.removeProperty('--period-menu-left');
        }
        if (activeStatusDropdown && activeStatusDropdown.toggle === toggle) {
            activeStatusDropdown = null;
        }
    });

    window.addEventListener('scroll', positionStatusDropdown, true);
    window.addEventListener('resize', positionStatusDropdown);

    const calendarRoot = document.getElementById('periodCalendar');
    if (!calendarRoot) return;

    const periods = Array.from(document.querySelectorAll('[data-period-calendar]')).map(function (element) {
        return element.dataset;
    });
    const stageDefinitions = [
        { key: 'teacher', start: 'teacherStart', end: 'teacherEnd', color: '#2b7a78' },
        { key: 'approval', start: 'approvalStart', end: 'approvalEnd', color: '#c07a28' },
        { key: 'student', start: 'studentStart', end: 'studentEnd', color: '#4776a8' }
    ];
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
        renderPeriodList();
        renderCalendar();
    }

    window.addEventListener('app-language-changed', render);
    render();
})();
