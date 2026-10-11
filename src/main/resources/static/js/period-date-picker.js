(function () {
    const inputs = document.querySelectorAll('[data-period-date-picker]');
    if (!inputs.length) return;

    const pad = function (number) { return String(number).padStart(2, '0'); };
    const makeDate = function (year, month, day) {
        const date = new Date(0);
        date.setFullYear(year, month - 1, day);
        date.setHours(0, 0, 0, 0);
        return date;
    };
    const parseValue = function (value) {
        const isoMatch = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})$/.exec(value);
        const displayMatch = /^(\d{1,2})\/(\d{1,2})\/(\d{4})\s+(\d{1,2}):(\d{2})$/.exec(value);
        const match = isoMatch
            ? [isoMatch[0], isoMatch[3], isoMatch[2], isoMatch[1], isoMatch[4], isoMatch[5]]
            : displayMatch;
        if (!match) return null;
        const day = Number(match[1]);
        const month = Number(match[2]);
        const year = Number(match[3]);
        const hour = Number(match[4]);
        const minute = Number(match[5]);
        const date = makeDate(year, month, day);
        if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day
            || hour > 23 || minute > 59) return null;
        return {
            date: date,
            hour: hour,
            minute: minute
        };
    };
    const formatValue = function (date, hour, minute) {
        return pad(date.getDate()) + '/' + pad(date.getMonth() + 1) + '/'
            + String(date.getFullYear()).padStart(4, '0')
            + ' ' + pad(hour) + ':' + pad(minute);
    };
    const dateSegments = [
        { start: 0, length: 2 },
        { start: 3, length: 2 },
        { start: 6, length: 4 },
        { start: 11, length: 2 },
        { start: 14, length: 2 }
    ];
    const segmentSeparators = { 2: 3, 5: 6, 10: 11, 13: 14 };
    const emptyParts = function () { return dateSegments.map(function (segment) {
        return Array(segment.length).fill('');
    }); };
    const partsFromParsed = function (parsed) {
        if (!parsed) return emptyParts();
        const date = parsed.date;
        return [
            pad(date.getDate()).split(''),
            pad(date.getMonth() + 1).split(''),
            String(date.getFullYear()).padStart(4, '0').split(''),
            pad(parsed.hour).split(''),
            pad(parsed.minute).split('')
        ];
    };
    const formatParts = function (parts) {
        if (!parts.some(function (part) { return part.some(Boolean); })) return '';
        const values = parts.map(function (part) {
            return part.map(function (digit) { return digit || '_'; }).join('');
        });
        return values[0] + '/' + values[1] + '/' + values[2] + ' ' + values[3] + ':' + values[4];
    };
    const valueFromParts = function (parts) {
        if (parts.some(function (part) { return part.some(function (digit) { return !digit; }); })) return null;
        return parts.map(function (part) { return part.join(''); }).join('');
    };
    const dateFromParts = function (parts) {
        const day = parts[0].join('');
        const month = parts[1].join('');
        const year = parts[2].join('');
        if ([day, month, year].some(function (part) { return !/^\d+$/.test(part); })) return null;
        const numericDay = Number(day);
        const numericMonth = Number(month);
        const numericYear = Number(year);
        const date = makeDate(numericYear, numericMonth, numericDay);
        if (date.getFullYear() !== numericYear || date.getMonth() !== numericMonth - 1
            || date.getDate() !== numericDay) return null;
        return date;
    };
    const segmentMaximums = [31, 12, null, 23, 59];
    const toSubmissionValue = function (value) {
        const parsed = parseValue(value);
        if (!parsed) return null;
        return String(parsed.date.getFullYear()).padStart(4, '0') + '-' + pad(parsed.date.getMonth() + 1) + '-'
            + pad(parsed.date.getDate()) + 'T' + pad(parsed.hour) + ':' + pad(parsed.minute);
    };
    window.periodDatePicker = {
        parseValue: parseValue,
        toSubmissionValue: toSubmissionValue
    };

    let activePicker = null;

    function closePicker(picker) {
        picker.panel.hidden = true;
        picker.input.parentElement.classList.remove('is-open');
        picker.input.setAttribute('aria-expanded', 'false');
        if (activePicker === picker) activePicker = null;
    }

    function updateInput(picker) {
        if (!picker.selectedDate) return;
        picker.input.value = formatValue(picker.selectedDate, picker.hourInput.value, picker.minuteInput.value);
        picker.input.dispatchEvent(new Event('input', { bubbles: true }));
        picker.input.dispatchEvent(new Event('change', { bubbles: true }));
    }

    function renderDays(picker) {
        const year = picker.currentDate.getFullYear();
        const month = picker.currentDate.getMonth();
        const firstDayIndex = new Date(year, month, 1).getDay();
        const totalDays = new Date(year, month + 1, 0).getDate();
        const previousMonthDays = new Date(year, month, 0).getDate();
        const days = [];

        for (let day = firstDayIndex; day > 0; day--) {
            days.push('<div class="period-date-picker__day is-other-month" aria-hidden="true">'
                + (previousMonthDays - day + 1) + '</div>');
        }
        for (let day = 1; day <= totalDays; day++) {
            const isSelected = picker.selectedDate
                && day === picker.selectedDate.getDate()
                && month === picker.selectedDate.getMonth()
                && year === picker.selectedDate.getFullYear();
            days.push('<button type="button" class="period-date-picker__day'
                + (isSelected ? ' is-selected' : '')
                + '" data-day="' + day + '"' + (isSelected ? ' aria-pressed="true"' : '')
                + '>' + day + '</button>');
        }
        const remainingCells = 42 - firstDayIndex - totalDays;
        for (let day = 1; day <= remainingCells; day++) {
            days.push('<div class="period-date-picker__day is-other-month" aria-hidden="true">' + day + '</div>');
        }

        picker.daysGrid.innerHTML = days.join('');
        picker.monthTrigger.textContent = 'Tháng ' + (month + 1);
        picker.yearTrigger.textContent = String(year);
    }

    function renderMonths(picker) {
        picker.overlay.className = 'period-date-picker__overlay is-months';
        picker.overlay.innerHTML = Array.from({ length: 12 }, function (_, month) {
            return '<button type="button" class="period-date-picker__overlay-item'
                + (month === picker.currentDate.getMonth() ? ' is-active' : '')
                + '" data-month="' + month + '">Thg ' + (month + 1) + '</button>';
        }).join('');
    }

    function renderYears(picker) {
        const startYear = picker.viewYear - 7;
        const endYear = picker.viewYear + 8;
        picker.overlay.className = 'period-date-picker__overlay is-years';
        picker.yearTrigger.textContent = startYear + ' - ' + endYear;
        picker.overlay.innerHTML = Array.from({ length: 16 }, function (_, index) {
            const year = startYear + index;
            return '<button type="button" class="period-date-picker__overlay-item'
                + (year === picker.currentDate.getFullYear() ? ' is-active' : '')
                + '" data-year="' + year + '">' + year + '</button>';
        }).join('');
    }

    function setCurrentMonth(picker, month) {
        picker.currentDate = new Date(picker.currentDate.getFullYear(), month, 1);
        picker.overlay.hidden = true;
        renderDays(picker);
    }

    function setCurrentYear(picker, year) {
        const month = picker.currentDate.getMonth();
        picker.currentDate = new Date(year, month, 1);
        picker.overlay.hidden = true;
        renderDays(picker);
    }

    function createPicker(input) {
        input.inputMode = 'numeric';
        input.placeholder = 'DD/MM/YYYY HH:mm';
        const wrapper = document.createElement('div');
        wrapper.className = 'period-date-picker-wrap';
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        const panel = document.createElement('div');
        panel.className = 'period-date-picker';
        panel.hidden = true;
        panel.setAttribute('role', 'dialog');
        panel.setAttribute('aria-label', 'Chọn ngày và thời gian');
        panel.innerHTML = [
            '<div class="period-date-picker__header">',
            '<button type="button" class="period-date-picker__nav" data-action="previous" aria-label="Tháng trước">&lt;</button>',
            '<div class="period-date-picker__selectors">',
            '<button type="button" class="period-date-picker__title" data-action="month">Tháng</button>',
            '<button type="button" class="period-date-picker__title" data-action="year">Năm</button>',
            '</div>',
            '<button type="button" class="period-date-picker__nav" data-action="next" aria-label="Tháng sau">&gt;</button>',
            '</div>',
            '<div class="period-date-picker__calendar">',
            '<div class="period-date-picker__weekdays"><span>CN</span><span>T2</span><span>T3</span><span>T4</span><span>T5</span><span>T6</span><span>T7</span></div>',
            '<div class="period-date-picker__days"></div>',
            '</div>',
            '<div class="period-date-picker__overlay" hidden></div>',
            '<div class="period-date-picker__time">',
            '<label for="' + input.id + '-hour">Thời gian:</label>',
            '<input type="number" id="' + input.id + '-hour" min="0" max="23" value="14" aria-label="Giờ" required>',
            '<span>:</span>',
            '<input type="number" id="' + input.id + '-minute" min="0" max="59" step="5" value="30" aria-label="Phút" required>',
            '</div>'
        ].join('');
        wrapper.appendChild(panel);

        const picker = {
            input: input,
            panel: panel,
            currentDate: new Date(),
            selectedDate: null,
            viewYear: new Date().getFullYear(),
            daysGrid: panel.querySelector('.period-date-picker__days'),
            monthTrigger: panel.querySelector('[data-action="month"]'),
            yearTrigger: panel.querySelector('[data-action="year"]'),
            overlay: panel.querySelector('.period-date-picker__overlay'),
            hourInput: panel.querySelector('[aria-label="Giờ"]'),
            minuteInput: panel.querySelector('[aria-label="Phút"]'),
            parts: null,
            activeSegment: 0,
            activeOffset: 0
        };

        const parsed = parseValue(input.value);
        picker.parts = partsFromParsed(parsed);
        if (parsed) {
            picker.selectedDate = parsed.date;
            picker.currentDate = new Date(parsed.date.getFullYear(), parsed.date.getMonth(), 1);
            picker.hourInput.value = String(parsed.hour);
            picker.minuteInput.value = String(parsed.minute);
        } else {
            const now = new Date();
            picker.hourInput.value = String(now.getHours());
            picker.minuteInput.value = String(Math.floor(now.getMinutes() / 5) * 5);
        }

        input.setAttribute('aria-haspopup', 'dialog');
        input.setAttribute('aria-expanded', 'false');
        input.addEventListener('click', function () { openPicker(picker); });
        input.addEventListener('focus', function () { openPicker(picker); });
        function getCaretPosition(position, direction) {
            if (segmentSeparators[position]) {
                return direction < 0 ? position - 1 : segmentSeparators[position];
            }
            return Math.max(0, Math.min(15, position));
        }
        function setActivePosition(position, direction) {
            const caret = getCaretPosition(position, direction);
            let segmentIndex = dateSegments.findIndex(function (segment) {
                return caret >= segment.start && caret < segment.start + segment.length;
            });
            if (segmentIndex < 0) segmentIndex = dateSegments.length - 1;
            picker.activeSegment = segmentIndex;
            picker.activeOffset = Math.max(0, Math.min(
                dateSegments[segmentIndex].length - 1,
                caret - dateSegments[segmentIndex].start
            ));
        }
        function syncPickerFromParts() {
            const date = dateFromParts(picker.parts);
            picker.selectedDate = date;
            if (date) {
                picker.currentDate = new Date(date.getFullYear(), date.getMonth(), 1);
            } else {
                const month = Number(picker.parts[1].join(''));
                const year = Number(picker.parts[2].join(''));
                if (month >= 1 && month <= 12 && year >= 1) {
                    picker.currentDate = new Date(year, month - 1, 1);
                }
            }
            const hour = picker.parts[3].join('');
            const minute = picker.parts[4].join('');
            if (/^\d{1,2}$/.test(hour) && Number(hour) <= 23) picker.hourInput.value = String(Number(hour));
            if (/^\d{1,2}$/.test(minute) && Number(minute) <= 59) picker.minuteInput.value = String(Number(minute));
            renderDays(picker);
        }
        function dispatchEditedValue(caretPosition) {
            input.value = formatParts(picker.parts);
            syncPickerFromParts();
            input.dispatchEvent(new Event('input', { bubbles: true }));
            input.dispatchEvent(new Event('change', { bubbles: true }));
            const caret = caretPosition === undefined
                ? dateSegments[picker.activeSegment].start + picker.activeOffset
                : caretPosition;
            input.setSelectionRange(caret, caret);
        }
        function insertDigit(digit, selectionStart, selectionEnd) {
            let start = selectionStart === undefined ? input.selectionStart : selectionStart;
            let end = selectionEnd === undefined ? input.selectionEnd : selectionEnd;
            if (start === 0 && end === input.value.length) {
                picker.parts = emptyParts();
                start = 0;
                end = 0;
            }
            setActivePosition(start, 1);
            const segment = dateSegments[picker.activeSegment];
            const part = picker.parts[picker.activeSegment];
            const offset = picker.activeOffset;
            const selectedStart = Math.max(segment.start, start);
            const selectedEnd = Math.min(segment.start + segment.length, end);
            let nextPart = part.slice();
            if (selectedEnd > selectedStart) {
                for (let index = selectedStart - segment.start; index < selectedEnd - segment.start; index++) {
                    nextPart[index] = '';
                }
            }
            nextPart[offset] = digit;
            const maximum = segmentMaximums[picker.activeSegment];
            if (maximum !== null && nextPart.every(Boolean) && Number(nextPart.join('')) > maximum) return;
            part.splice(0, part.length, ...nextPart);

            let nextPosition = segment.start + offset + 1;
            if (nextPosition >= segment.start + segment.length && picker.activeSegment < dateSegments.length - 1) {
                picker.activeSegment++;
                picker.activeOffset = 0;
                nextPosition = dateSegments[picker.activeSegment].start;
            } else {
                picker.activeOffset = Math.min(offset + 1, segment.length - 1);
            }
            dispatchEditedValue(nextPosition);
        }
        input.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closePicker(picker);
            } else if (/^\d$/.test(event.key)) {
                event.preventDefault();
                insertDigit(event.key, input.selectionStart, input.selectionEnd);
            } else if (event.key === 'Backspace' || event.key === 'Delete') {
                event.preventDefault();
                if (input.selectionStart === 0 && input.selectionEnd === input.value.length) {
                    picker.parts = emptyParts();
                    picker.activeSegment = 0;
                    picker.activeOffset = 0;
                    dispatchEditedValue();
                    return;
                }
                let start = input.selectionStart;
                let end = input.selectionEnd;
                if (start === end) {
                    if (event.key === 'Backspace') {
                        start = getCaretPosition(start - 1, -1);
                    } else {
                        start = getCaretPosition(start, 1);
                        end = start + 1;
                    }
                }
                let removed = false;
                dateSegments.forEach(function (segment, index) {
                    const from = Math.max(start, segment.start);
                    const to = Math.min(end, segment.start + segment.length);
                    if (to <= from) return;
                    for (let position = from; position < to; position++) {
                        picker.parts[index][position - segment.start] = '';
                    }
                    picker.activeSegment = index;
                    picker.activeOffset = from - segment.start;
                    removed = true;
                });
                if (removed) dispatchEditedValue(start);
            } else if (event.key === '/' || event.key === ':' || event.key === ' ') {
                event.preventDefault();
                const nextSegment = Math.min(picker.activeSegment + 1, dateSegments.length - 1);
                picker.activeSegment = nextSegment;
                picker.activeOffset = 0;
                const next = dateSegments[nextSegment].start;
                input.setSelectionRange(next, next);
            } else if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
                event.preventDefault();
                let next = input.selectionStart + (event.key === 'ArrowLeft' ? -1 : 1);
                next = getCaretPosition(next, event.key === 'ArrowLeft' ? -1 : 1);
                setActivePosition(next, event.key === 'ArrowLeft' ? -1 : 1);
                input.setSelectionRange(next, next);
            } else if (event.key === 'Home' || event.key === 'End') {
                event.preventDefault();
                const nextSegment = event.key === 'Home' ? 0 : dateSegments.length - 1;
                picker.activeSegment = nextSegment;
                picker.activeOffset = event.key === 'Home' ? 0 : dateSegments[nextSegment].length - 1;
                const next = dateSegments[nextSegment].start + picker.activeOffset;
                input.setSelectionRange(next, next);
            } else if (event.key.length === 1 && !event.ctrlKey && !event.metaKey && !event.altKey) {
                event.preventDefault();
            }
        });
        input.addEventListener('click', function () {
            setActivePosition(input.selectionStart, 1);
        });
        input.addEventListener('keyup', function () {
            setActivePosition(input.selectionStart, 1);
        });
        input.addEventListener('select', function () {
            if (input.selectionStart === input.selectionEnd) setActivePosition(input.selectionStart, 1);
        });
        input.addEventListener('paste', function (event) { event.preventDefault(); });
        input.addEventListener('beforeinput', function (event) {
            if (event.inputType === 'insertText' || event.inputType === 'insertFromPaste') {
                event.preventDefault();
                const text = event.data || '';
                if (/^\d$/.test(text)) insertDigit(text);
            }
        });
        input.addEventListener('input', function () {
            const value = parseValue(input.value);
            if (!value) return;
            picker.parts = partsFromParsed(value);
            picker.selectedDate = value.date;
            picker.currentDate = new Date(value.date.getFullYear(), value.date.getMonth(), 1);
            picker.hourInput.value = String(value.hour);
            picker.minuteInput.value = String(value.minute);
            renderDays(picker);
        });
        panel.addEventListener('click', function (event) {
            const button = event.target.closest('button');
            if (!button) return;
            const action = button.dataset.action;
            if (action === 'previous' || action === 'next') {
                const direction = action === 'previous' ? -1 : 1;
                if (picker.overlay.classList.contains('is-years')) {
                    picker.viewYear += direction * 16;
                    renderYears(picker);
                } else if (picker.overlay.classList.contains('is-months')) {
                    return;
                } else {
                    picker.currentDate = new Date(
                        picker.currentDate.getFullYear(),
                        picker.currentDate.getMonth() + direction,
                        1
                    );
                    renderDays(picker);
                }
            } else if (action === 'month') {
                if (!picker.overlay.hidden && picker.overlay.classList.contains('is-months')) {
                    picker.overlay.hidden = true;
                } else {
                    renderMonths(picker);
                    picker.overlay.hidden = false;
                }
            } else if (action === 'year') {
                if (!picker.overlay.hidden && picker.overlay.classList.contains('is-years')) {
                    picker.overlay.hidden = true;
                    renderDays(picker);
                } else {
                    picker.viewYear = picker.currentDate.getFullYear();
                    renderYears(picker);
                    picker.overlay.hidden = false;
                }
            } else if (button.hasAttribute('data-month')) {
                setCurrentMonth(picker, Number(button.dataset.month));
            } else if (button.hasAttribute('data-year')) {
                setCurrentYear(picker, Number(button.dataset.year));
            } else if (button.hasAttribute('data-day')) {
                picker.selectedDate = new Date(
                    picker.currentDate.getFullYear(),
                    picker.currentDate.getMonth(),
                    Number(button.dataset.day)
                );
                renderDays(picker);
                updateInput(picker);
            }
        });

        [picker.hourInput, picker.minuteInput].forEach(function (timeInput) {
            timeInput.addEventListener('input', function () {
                if (timeInput.value === '') return;
                const min = Number(timeInput.min);
                const max = Number(timeInput.max);
                timeInput.value = String(Math.max(min, Math.min(max, Number(timeInput.value))));
                updateInput(picker);
            });
            timeInput.addEventListener('change', function () {
                if (timeInput.value === '') {
                    const current = parseValue(input.value);
                    timeInput.value = current
                        ? String(timeInput === picker.hourInput ? current.hour : current.minute)
                        : '0';
                }
                if (timeInput === picker.minuteInput) {
                    timeInput.value = String(Math.max(0, Math.min(55, Math.round(Number(timeInput.value) / 5) * 5)));
                }
                updateInput(picker);
            });
        });

        renderDays(picker);
        return picker;
    }

    function openPicker(picker) {
        if (activePicker && activePicker !== picker) closePicker(activePicker);
        const parsed = parseValue(picker.input.value);
        if (parsed) {
            picker.selectedDate = parsed.date;
            picker.currentDate = new Date(parsed.date.getFullYear(), parsed.date.getMonth(), 1);
            picker.hourInput.value = String(parsed.hour);
            picker.minuteInput.value = String(parsed.minute);
        }
        renderDays(picker);
        picker.panel.hidden = false;
        picker.input.parentElement.classList.add('is-open');
        positionPicker(picker);
        picker.input.setAttribute('aria-expanded', 'true');
        picker.overlay.hidden = true;
        activePicker = picker;
    }

    function positionPicker(picker) {
        const panel = picker.panel;
        const gap = 6;
        const inputRect = picker.input.getBoundingClientRect();
        const availableAbove = inputRect.top - gap;
        const availableBelow = window.innerHeight - inputRect.bottom - gap;

        panel.classList.remove('opens-up', 'is-compact');
        panel.style.maxHeight = '';
        panel.style.overflowY = '';

        if (Math.max(availableAbove, availableBelow) < panel.getBoundingClientRect().height) {
            panel.classList.add('is-compact');
        }

        const openUp = availableBelow < panel.getBoundingClientRect().height
            && availableAbove > availableBelow;
        panel.classList.toggle('opens-up', openUp);

        const availableSpace = openUp ? availableAbove : availableBelow;
        if (availableSpace < panel.getBoundingClientRect().height) {
            panel.style.maxHeight = Math.max(160, availableSpace) + 'px';
            panel.style.overflowY = 'auto';
        }
    }

    const pickers = Array.from(inputs, createPicker);
    window.addEventListener('resize', function () {
        if (activePicker) positionPicker(activePicker);
    });
    window.addEventListener('scroll', function () {
        if (activePicker) positionPicker(activePicker);
    }, true);
    document.addEventListener('click', function (event) {
        if (activePicker && !activePicker.panel.contains(event.target)
            && !activePicker.input.contains(event.target)) {
            closePicker(activePicker);
        }
    });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && activePicker) closePicker(activePicker);
    });
})();
