(async function () {
    const form = document.querySelector('[data-period-schedule]');
    if (!form) return;

    await customElements.whenDefined('wa-input');

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
    const originalTeacherStart = fields[0].value;
    const optionalDateFields = [reviewDeadline, councilDate];
    const allDateFields = fields.concat(optionalDateFields);

    function updateTotalWindow() {
        const start = fields[0].value;
        const end = fields[fields.length - 1].value;
        if (!totalWindow || !start || !end) return;
        const language = document.documentElement.lang === 'en' ? 'en-US' : 'vi-VN';
        const options = { day: '2-digit', month: '2-digit', year: 'numeric' };
        totalWindow.textContent = new Intl.DateTimeFormat(language, options).format(new Date(start))
            + ' - ' + new Intl.DateTimeFormat(language, options).format(new Date(end));
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
    }

    function validate() {
        let invalidField = null;
        let startIsInPast = false;
        const teacherStartChanged = fields[0].value !== originalTeacherStart;
        if (form.hasAttribute('data-period-create') || teacherStartChanged) {
            const minimumStart = nextCurrentMinute();
            if (fields[0].value && fields[0].value < minimumStart) {
                invalidField = fields[0];
                startIsInPast = true;
            }
        }

        fields.forEach(function (field, index) {
            field.setCustomValidity('');
            if (index > 0 && fields[index - 1].value) {
                if (field.value && field.value <= fields[index - 1].value && !invalidField) {
                    invalidField = field;
                }
            }
        });

        if (invalidField) {
            const message = window.appTranslate(startIsInPast
                ? 'period.startMustBeFuture'
                : 'period.invalidSchedule');
            invalidField.setCustomValidity(message);
            error.textContent = message;
            form.dispatchEvent(new Event('schedule-validitychange'));
            return false;
        }
        error.textContent = '';
        updateTotalWindow();
        form.dispatchEvent(new Event('schedule-validitychange'));
        return true;
    }

    allDateFields.forEach(function (field) {
        field.addEventListener('input', validate);
        field.addEventListener('change', validate);
    });
    periodType.addEventListener('change', function () {
        updateTypeRequirements();
        validate();
    });
    form.addEventListener('submit', function (event) {
        const isScheduleValid = validate();
        const invalidControl = [periodType].concat(allDateFields)
            .find(function (field) { return !field.checkValidity(); });
        if (!isScheduleValid || invalidControl) {
            event.preventDefault();
            (invalidControl || fields[0]).reportValidity();
        }
    });
    window.addEventListener('app-language-changed', updateTotalWindow);

    updateTypeRequirements();
    validate();
})();
