(function () {
    const languageButtons = Array.from(document.querySelectorAll('[data-language]'));
    const language = localStorage.getItem('app-language') || 'vi';
    let messages = {};

    function lookup(key) {
        return key.split('.').reduce(function (value, part) {
            return value && value[part];
        }, messages);
    }

    function translatePage() {
        const currentLanguage = localStorage.getItem('app-language') === 'en' ? 'en' : 'vi';
        document.documentElement.lang = currentLanguage;
        const autoMessages = Object.assign({}, messages.auto || {}, messages.autoExtra || {});
        const title = document.querySelector('title');
        if (title) {
            if (title._i18nSource === undefined) title._i18nSource = title.textContent;
            title.textContent = currentLanguage === 'en'
                ? (autoMessages[title._i18nSource] || title._i18nSource)
                : title._i18nSource;
        }
        document.querySelectorAll('[data-i18n]').forEach(function (element) {
            const value = lookup(element.dataset.i18n);
            if (typeof value !== 'string') return;
            const textNodes = Array.from(element.childNodes).filter(function (node) {
                return node.nodeType === Node.TEXT_NODE;
            });
            if (textNodes.length) {
                const source = textNodes[0].nodeValue;
                textNodes[0].nodeValue = source.replace(source.trim(), value);
                textNodes.slice(1).forEach(function (node) { node.nodeValue = ''; });
            } else {
                element.textContent = value;
            }
        });
        const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
        let textNode;
        while ((textNode = walker.nextNode())) {
            const parent = textNode.parentElement;
            if (!parent || parent.closest('script,style,[data-i18n]')) continue;
            if (textNode._i18nSource === undefined) textNode._i18nSource = textNode.nodeValue;
            const source = textNode._i18nSource;
            const trimmed = source.trim();
            const translated = currentLanguage === 'en' ? autoMessages[trimmed] : null;
            if (translated) {
                textNode.nodeValue = source.replace(trimmed, translated);
            } else if (currentLanguage === 'vi') {
                textNode.nodeValue = source;
            }
        }
        document.querySelectorAll('input[placeholder],textarea[placeholder],button[title],button[aria-label],input[aria-label]').forEach(function (element) {
            ['placeholder', 'title', 'aria-label'].forEach(function (attribute) {
                if (!element.hasAttribute(attribute)) return;
                const sourceKey = '_i18n' + attribute.replace(/(^|-)(.)/g, function (_, separator, letter) { return letter.toUpperCase(); });
                if (element[sourceKey] === undefined) element[sourceKey] = element.getAttribute(attribute);
                const source = element[sourceKey];
                const translated = currentLanguage === 'en' ? autoMessages[source] : null;
                element.setAttribute(attribute, translated || source);
            });
        });
        document.querySelectorAll('[data-i18n-placeholder],[data-i18n-title],[data-i18n-aria-label]').forEach(function (element) {
            ['placeholder', 'title', 'aria-label'].forEach(function (attribute) {
                const key = element.dataset['i18n' + attribute.charAt(0).toUpperCase() + attribute.slice(1)];
                const value = key && lookup(key);
                if (typeof value === 'string') element.setAttribute(attribute, value);
            });
        });
        document.querySelectorAll('[data-i18n-label]').forEach(function (element) {
            const value = lookup(element.dataset.i18nLabel);
            if (typeof value === 'string') element.setAttribute('label', value);
        });
        languageButtons.forEach(function (button) {
            const active = button.dataset.language === currentLanguage;
            button.classList.toggle('active', active);
            button.setAttribute('aria-pressed', String(active));
        });
        window.dispatchEvent(new Event('app-language-changed'));
    }

    window.appTranslate = function (key) {
        return lookup(key) || key;
    };

    async function setLanguage(value) {
        const nextLanguage = value === 'en' ? 'en' : 'vi';
        try {
            const response = await fetch('/locales/' + nextLanguage + '.json', { cache: 'no-cache' });
            if (!response.ok) return;
            messages = await response.json();
            localStorage.setItem('app-language', nextLanguage);
            translatePage();
        } catch (error) {
            localStorage.setItem('app-language', 'vi');
        }
    }

    languageButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            setLanguage(button.dataset.language);
        });
    });
    setLanguage(language);
})();
