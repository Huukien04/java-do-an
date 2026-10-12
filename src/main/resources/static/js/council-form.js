(function () {
    const form = document.querySelector('[data-council-create]');
    if (!form) return;

    const nameInput = document.getElementById('name');
    const codeInput = document.getElementById('councilCode');
    const periodSelect = document.getElementById('periodId');
    const topicsSelect = document.getElementById('assignedTopicIds');
    const defenseDate = document.getElementById('defenseDate');

    function updateCouncilCode() {
        const normalized = nameInput.value
            .replace(/[đĐ]/g, function (letter) { return letter === 'đ' ? 'd' : 'D'; })
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toUpperCase()
            .replace(/[^A-Z0-9]+/g, '_')
            .replace(/^_+|_+$/g, '')
            .slice(0, 30);
        codeInput.value = normalized;
    }

    function filterTopicsByPeriod() {
        const periodId = periodSelect.value;
        Array.from(topicsSelect.options).forEach(function (option) {
            const matches = !periodId || option.dataset.periodId === periodId;
            option.hidden = !matches;
            option.disabled = !matches;
            if (!matches) option.selected = false;
        });
    }

    nameInput.addEventListener('input', updateCouncilCode);
    periodSelect.addEventListener('change', filterTopicsByPeriod);
    defenseDate.addEventListener('input', function () {
        defenseDate.setCustomValidity('');
    });
    form.addEventListener('submit', function (event) {
        if (!defenseDate.value) return;
        const submissionValue = window.periodDatePicker.toSubmissionValue(defenseDate.value);
        if (!submissionValue) {
            event.preventDefault();
            defenseDate.setCustomValidity('Vui lòng nhập thời gian theo định dạng ngày/tháng/năm giờ:phút.');
            defenseDate.reportValidity();
            return;
        }
        defenseDate.setCustomValidity('');
        defenseDate.value = submissionValue;
    });

    updateCouncilCode();
    filterTopicsByPeriod();
})();
