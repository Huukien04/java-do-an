(function () {
    const refreshInterval = 30000;
    window.setInterval(function () {
        if (document.visibilityState === 'visible') window.location.reload();
    }, refreshInterval);
})();
