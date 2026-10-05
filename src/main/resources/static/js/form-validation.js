(function () {
    function update(form) {
        const valid = form.checkValidity();
        form.querySelectorAll('button[type="submit"],input[type="submit"]').forEach(function (button) {
            button.disabled = !valid;
            button.setAttribute('aria-disabled', String(!valid));
        });
    }

    document.querySelectorAll('form').forEach(function (form) {
        update(form);
        form.addEventListener('input', function () { update(form); });
        form.addEventListener('change', function () { update(form); });
        form.addEventListener('reset', function () {
            window.setTimeout(function () { update(form); }, 0);
        });
        form.addEventListener('schedule-validitychange', function () { update(form); });
        form.addEventListener('submit', function (event) {
            if (form.checkValidity()) return;
            event.preventDefault();
            form.reportValidity();
            update(form);
        });
    });
})();
