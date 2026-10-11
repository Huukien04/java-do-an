(function () {
    const form = document.querySelector('[data-period-schedule]');
    if (!form) return;

    const fields = [
        'teacherStartDate',
        'teacherEndDate',
        'approvalStartDate',
        'approvalEndDate',
        'studentStartDate',
        'studentEndDate'
    ].map(function (id) { return document.getElementById(id); });
    const error = form.querySelector('[data-schedule-error]');
    const totalWindow = document.getElementById('periodTotalWindow');
    const periodType = document.getElementById('periodType');
    const reviewDeadline = document.getElementById('reviewSubmissionDeadline');
    const councilDate = document.getElementById('councilReportDate');
    const optionalDateFields = [reviewDeadline, councilDate];
    const allDateFields = fields.concat(optionalDateFields);
    const allFields = [document.getElementById('name'), periodType].concat(allDateFields);
    const touchedFields = new Set(allFields.filter(function (field) {
        return field.classList.contains('is-invalid');
    }));
    const originalTeacherStart = window.periodDatePicker.toSubmissionValue(fields[0].value);
    let submitted = false;

    function getDateValue(field) {
        return field.value ? window.periodDatePicker.parseValue(field.value) : null;
    }

    function getTimestamp(value) {
        return value.date.getTime() + (value.hour * 60 + value.minute) * 60 * 1000;
    }

    function updateTotalWindow() {
        const start = getDateValue(fields[0]);
        const end = getDateValue(fields[fields.length - 1]);
        if (!totalWindow || !start || !end) return;
        const language = document.documentElement.lang === 'en' ? 'en-US' : 'vi-VN';
        const options = { day: '2-digit', month: '2-digit', year: 'numeric' };
        totalWindow.textContent = new Intl.DateTimeFormat(language, options).format(start.date)
            + ' - ' + new Intl.DateTimeFormat(language, options).format(end.date);
    }

    function nextCurrentMinute() {
        const date = new Date();
        date.setSeconds(0, 0);
        date.setMinutes(date.getMinutes() + 1);
        const pad = function (number) { return String(number).padStart(2, '0'); };
        return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
            + 'T' + pad(date.getHours()) + ':' + pad(date.getMinutes());
    }

    function updateTypeRequirements() {
        const type = periodType.value;
        reviewDeadline.required = type === 'TLCN' || type === 'KLTN';
        councilDate.required = type === 'KLTN';
        optionalDateFields.forEach(function (field) {
            const marker = field.labels[0].querySelector('[data-required-marker]');
            if (marker) marker.hidden = !field.required;
        });
    }

    function validate(force) {
        const invalidFields = new Map();
        const markInvalid = function (field, message) {
            if (!invalidFields.has(field)) invalidFields.set(field, message);
            field.setCustomValidity(invalidFields.get(field));
        };

        allFields.forEach(function (field) {
            field.setCustomValidity('');
        });

        const parsedDates = allDateFields.map(function (field) {
            if (!field.value) return null;
            const parsed = getDateValue(field);
            if (!parsed) {
                if (force !== true && field === document.activeElement) return null;
                markInvalid(field, window.appTranslate('period.invalidSchedule'));
            }
            return parsed;
        });
        const teacherStart = parsedDates[0];
        const teacherStartChanged = teacherStart
            && window.periodDatePicker.toSubmissionValue(fields[0].value) !== originalTeacherStart;
        if (form.hasAttribute('data-period-create') || teacherStartChanged) {
            if (teacherStart && getTimestamp(teacherStart) < new Date(nextCurrentMinute()).getTime()) {
                markInvalid(fields[0], window.appTranslate('period.startMustBeFuture'));
            }
        }

        fields.forEach(function (field, index) {
            const currentDate = parsedDates[index];
            const previousDate = index > 0 ? parsedDates[index - 1] : null;
            if (index > 0 && currentDate && previousDate
                    && getTimestamp(currentDate) <= getTimestamp(previousDate)) {
                const message = window.appTranslate('period.invalidSchedule');
                markInvalid(fields[index - 1], message);
                markInvalid(field, message);
            }
        });

        allFields.forEach(function (field) {
            const isEmpty = !field.value || (field.id === 'name' && !field.value.trim());
            if (field.required && isEmpty && (submitted || touchedFields.has(field))) {
                markInvalid(field, window.appTranslate('period.missingDate'));
            }
        });

        allFields.forEach(function (field) {
            const label = field.labels[0];
            let fieldError = label.querySelector('[data-field-error]');
            if (!fieldError) {
                fieldError = document.createElement('span');
                fieldError.className = 'text-danger small ms-2';
                fieldError.dataset.fieldError = field.id;
                fieldError.hidden = true;
                label.appendChild(fieldError);
            }

            field.classList.toggle('is-invalid', invalidFields.has(field));
            field.setAttribute('aria-invalid', String(invalidFields.has(field)));
            fieldError.textContent = invalidFields.get(field) || '';
            fieldError.hidden = !invalidFields.has(field);
        });

        if (invalidFields.size) {
            if (error) error.textContent = '';
            form.dispatchEvent(new Event('schedule-validitychange'));
            return false;
        }
        if (error) error.textContent = '';
        updateTotalWindow();
        form.dispatchEvent(new Event('schedule-validitychange'));
        return true;
    }

    allFields.forEach(function (field) {
        field.addEventListener('input', function () {
            touchedFields.add(field);
            validate();
        });
        field.addEventListener('change', function () {
            touchedFields.add(field);
            validate();
        });
        field.addEventListener('blur', function () { validate(true); });
    });
    periodType.addEventListener('change', function () {
        updateTypeRequirements();
        validate();
    });
    form.addEventListener('submit', function (event) {
        submitted = true;
        const isScheduleValid = validate(true);
        const invalidControl = [periodType].concat(allDateFields)
            .find(function (field) { return !field.checkValidity(); });
        if (!isScheduleValid || invalidControl) {
            event.preventDefault();
            return;
        }
        allDateFields.forEach(function (field) {
            if (field.value) field.value = window.periodDatePicker.toSubmissionValue(field.value);
        });
    });
    window.addEventListener('app-language-changed', function () {
        validate();
        updateTotalWindow();
    });

    updateTypeRequirements();
    validate();
})();
