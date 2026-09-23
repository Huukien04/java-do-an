(function () {
    var stored = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-theme', stored);
    document.documentElement.setAttribute('data-bs-theme', stored);
})();
