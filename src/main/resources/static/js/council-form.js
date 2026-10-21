(function () {
    const form = document.querySelector('[data-council-create]');
    if (!form) return;

    const nameInput = document.getElementById('name');
    const codeInput = document.getElementById('councilCode');
    const periodSelect = document.getElementById('periodId');
    const topicsList = document.getElementById('assignedTopicsList');
    const topicItems = topicsList ? Array.from(topicsList.querySelectorAll('[data-period-id]')) : [];
    const submitBtn = document.getElementById('councilSubmitBtn');
    const noTopicsHint = document.getElementById('noTopicsHint');
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

    // Chi form tao hoi dong (data-require-topics): khong co de tai nao trong dot dang chon thi an nut Tao.
    function updateSubmitVisibility(visibleTopics) {
        if (!form.hasAttribute('data-require-topics') || !submitBtn) return;
        const hasTopics = visibleTopics > 0;
        submitBtn.classList.toggle('d-none', !hasTopics);
        if (noTopicsHint) noTopicsHint.classList.toggle('d-none', hasTopics);
    }

    function filterTopicsByPeriod() {
        const periodId = periodSelect.value;
        let visibleTopics = 0;
        topicItems.forEach(function (item) {
            const matches = !periodId || item.dataset.periodId === periodId;
            item.style.display = matches ? '' : 'none';
            const checkbox = item.querySelector('input[type="checkbox"]');
            if (checkbox) {
                checkbox.disabled = !matches;
                if (!matches) checkbox.checked = false;
            }
            if (matches) visibleTopics++;
        });
        updateSubmitVisibility(visibleTopics);
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