/*
 * appConfirm - hop thoai xac nhan dung chung cho toan he thong (thay confirm() mac dinh).
 *
 * Dung truc tiep:
 *   appConfirm({ title: 'Xoa de tai', message: '...', okText: 'Co, xoa luon!' })
 *     .then(function (ok) { if (ok) { ... } });
 *
 * Dung khai bao (khong can viet JS): gan len <form> hoac <a>:
 *   data-confirm="Noi dung thong diep"
 *   data-confirm-title="Tieu de"            (mac dinh: 'Xac nhan thao tac')
 *   data-confirm-ok="Co, xoa luon!"         (mac dinh: 'Co, tiep tuc')
 *   data-confirm-cancel="Khong, giu lai"    (mac dinh: 'Khong, giu lai')
 *   data-confirm-variant="danger|warning"   (mac dinh: 'danger' - do; 'warning' - cam)
 *
 * Neu Bootstrap chua san sang, tu fallback ve confirm() goc de khong chan thao tac.
 */
(function () {
    'use strict';

    var DEFAULTS = {
        title: 'Xác nhận thao tác',
        message: 'Bạn có chắc muốn tiếp tục?',
        okText: 'Có, tiếp tục',
        cancelText: 'Không, giữ lại',
        variant: 'danger'
    };

    var modalEl = document.getElementById('appConfirmModal');
    var titleEl = modalEl ? modalEl.querySelector('.app-confirm-title') : null;
    var messageEl = modalEl ? modalEl.querySelector('.app-confirm-message') : null;
    var okBtn = modalEl ? modalEl.querySelector('.app-confirm-btn-ok') : null;
    var cancelBtn = modalEl ? modalEl.querySelector('.app-confirm-btn-cancel') : null;

    var resolvePending = null;
    var confirmed = false;

    function settle(value) {
        if (resolvePending) {
            var resolve = resolvePending;
            resolvePending = null;
            resolve(value);
        }
    }

    function openConfirm(options) {
        var opts = Object.assign({}, DEFAULTS, options || {});

        // Fallback an toan: thieu Bootstrap hoac thieu markup -> dung confirm() goc.
        if (!window.bootstrap || !modalEl || !okBtn) {
            return Promise.resolve(window.confirm(opts.message));
        }

        // Huy hop thoai dang mo truoc do (neu co).
        settle(false);
        confirmed = false;

        titleEl.textContent = opts.title;
        messageEl.textContent = opts.message;
        okBtn.textContent = opts.okText;
        cancelBtn.textContent = opts.cancelText;
        modalEl.setAttribute('data-variant', opts.variant === 'warning' ? 'warning' : 'danger');

        return new Promise(function (resolve) {
            resolvePending = resolve;
            var instance = window.bootstrap.Modal.getOrCreateInstance(modalEl);
            instance.show();
            // Luon noi tren cac menu noi cua trang (vd: menu trang thai dot z-index 1200).
            var backdrop = document.querySelector('.modal-backdrop');
            if (backdrop) backdrop.style.zIndex = '1300';
            modalEl.style.zIndex = '1310';
        });
    }

    if (modalEl && okBtn) {
        okBtn.addEventListener('click', function () {
            confirmed = true;
            settle(true);
            window.bootstrap.Modal.getOrCreateInstance(modalEl).hide();
        });
        modalEl.addEventListener('hidden.bs.modal', function () {
            if (!confirmed) settle(false);
            confirmed = false;
        });
        modalEl.addEventListener('shown.bs.modal', function () {
            // Mac dinh focus nut Huy de tranh Enter nham vao thao tac pha huy.
            if (cancelBtn) cancelBtn.focus();
        });
    }

    window.appConfirm = openConfirm;
    window.UIComponents = Object.assign(window.UIComponents || {}, { confirm: openConfirm });

    function optionsFrom(el) {
        return {
            title: el.getAttribute('data-confirm-title') || DEFAULTS.title,
            message: el.getAttribute('data-confirm') || DEFAULTS.message,
            okText: el.getAttribute('data-confirm-ok') || DEFAULTS.okText,
            cancelText: el.getAttribute('data-confirm-cancel') || DEFAULTS.cancelText,
            variant: el.getAttribute('data-confirm-variant') || DEFAULTS.variant
        };
    }

    // Chan submit cua form co data-confirm: hoi truoc, dong y moi submit lai.
    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (!form || form.tagName !== 'FORM' || !form.hasAttribute('data-confirm')) return;
        if (form.dataset.appConfirmed === '1') {
            delete form.dataset.appConfirmed;
            return;
        }
        event.preventDefault();
        openConfirm(optionsFrom(form)).then(function (ok) {
            if (!ok) return;
            form.dataset.appConfirmed = '1';
            if (form.requestSubmit) form.requestSubmit();
            else form.submit();
        });
    }, true);

    // Tuong tu cho link <a data-confirm href="...">.
    document.addEventListener('click', function (event) {
        var link = event.target.closest ? event.target.closest('a[data-confirm]') : null;
        if (!link) return;
        event.preventDefault();
        openConfirm(optionsFrom(link)).then(function (ok) {
            if (ok) window.location.href = link.href;
        });
    });
})();
