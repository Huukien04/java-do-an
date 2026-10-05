(function () {
    function initCharts() {
        document.querySelectorAll('[data-ui-chart-type]').forEach(function (chartRoot) {
            const fallback = chartRoot.parentElement.querySelector('.ui-chart-widget__fallback');
            if (!window.ApexCharts) {
                if (fallback) fallback.hidden = false;
                return;
            }

            try {
                const labels = chartRoot.dataset.uiChartLabels.split('|');
                const values = chartRoot.dataset.uiChartValues.split('|').map(Number);
                const colors = chartRoot.dataset.uiChartColors.split('|');
                const style = getComputedStyle(document.documentElement);
                const theme = document.documentElement.dataset.theme === 'light' ? 'light' : 'dark';
                const textColor = style.getPropertyValue('--muted').trim();
                const config = {
                    chart: {
                        type: chartRoot.dataset.uiChartType,
                        height: '100%',
                        toolbar: { show: false },
                        fontFamily: style.getPropertyValue('--font-family-default').trim(),
                        foreColor: textColor
                    },
                    series: [{ name: chartRoot.getAttribute('aria-label') || 'Series', data: values }],
                    colors: colors,
                    dataLabels: { enabled: false },
                    grid: { borderColor: style.getPropertyValue('--border').trim(), strokeDashArray: 4 },
                    legend: { show: false },
                    noData: { text: 'Không có dữ liệu' },
                    plotOptions: { bar: { horizontal: false, borderRadius: 5, distributed: true } },
                    theme: { mode: theme },
                    tooltip: { theme: theme },
                    xaxis: {
                        categories: labels,
                        labels: { style: { colors: labels.map(function () { return textColor; }) } }
                    },
                    yaxis: {
                        min: 0,
                        forceNiceScale: true,
                        labels: {
                            style: { colors: [textColor] },
                            formatter: function (value) { return String(Math.round(value)); }
                        }
                    }
                };
                chartRoot._uiChart = new window.ApexCharts(chartRoot, config);
                chartRoot._uiChart.render().catch(function () {
                    if (fallback) fallback.hidden = false;
                });
            } catch (error) {
                if (fallback) {
                    fallback.textContent = 'Không thể hiển thị biểu đồ với cấu hình hiện tại.';
                    fallback.hidden = false;
                }
            }
        });
    }

    function showToast(id) {
        const element = document.getElementById(id);
        if (!element || !window.bootstrap) return;
        window.bootstrap.Toast.getOrCreateInstance(element, { autohide: true, delay: 5000 }).show();
    }

    function showModal(id) {
        const element = document.getElementById(id);
        if (!element || !window.bootstrap) return;
        window.bootstrap.Modal.getOrCreateInstance(element).show();
    }

    window.UIComponents = Object.assign(window.UIComponents || {}, { showToast: showToast, showModal: showModal });

    document.querySelectorAll('[data-ui-toast="true"]').forEach(function (element) {
        showToast(element.id);
    });
    initCharts();
})();
