function getCssVar(name) {
    return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

function themeColors() {
    return {
        text: getCssVar("--text"),
        border: getCssVar("--border"),
        primary: getCssVar("--primary"),
        primarySoft: getCssVar("--primary-soft"),
        accent: getCssVar("--accent")
    };
}

function generateTrend(current) {
    if (current === null || current === undefined) return [60, 66, 72, 78];
    return [
        Math.max(0, current - 18),
        Math.max(0, current - 10),
        Math.max(0, current - 4),
        current
    ];
}

function createCharts(data) {
    if (!window.Chart) {
        console.warn("Chart.js not found — charts will not render.");
        return;
    }

    const colors = themeColors();
    const build = data.buildHealth ?? 0;
    const modern = data.modernization ?? 0;
    const composition = data.composition ?? 0;

    const buildTrend = generateTrend(build);
    const modernTrend = generateTrend(modern);

    const impactTotals = data.impactTotals || { buildHealth: 0, modernization: 0 };

    const trendCanvas = document.getElementById("trendChart");
    if (trendCanvas) {
        new Chart(trendCanvas, {
            type: "line",
            data: {
                labels: ["v1", "v2", "v3", "Now"],
                datasets: [
                    {
                        label: "Build Health",
                        data: buildTrend,
                        borderColor: colors.primary,
                        backgroundColor: colors.primarySoft,
                        tension: 0.35,
                        fill: true
                    },
                    {
                        label: "Modernization",
                        data: modernTrend,
                        borderColor: colors.accent,
                        tension: 0.35
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { labels: { color: colors.text } }
                },
                scales: {
                    y: { beginAtZero: true, grid: { color: colors.border }, ticks: { color: colors.text } },
                    x: { grid: { color: colors.border }, ticks: { color: colors.text } }
                }
            }
        });
    }

    const impactCanvas = document.getElementById("impactChart");
    if (impactCanvas) {
        new Chart(impactCanvas, {
            type: "bar",
            data: {
                labels: ["Total Impact"],
                datasets: [
                    {
                        label: "Build Health",
                        data: [impactTotals.buildHealth ?? 0],
                        backgroundColor: colors.primary
                    },
                    {
                        label: "Modernization",
                        data: [impactTotals.modernization ?? 0],
                        backgroundColor: colors.accent
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { labels: { color: colors.text } }
                },
                scales: {
                    y: { beginAtZero: true, grid: { color: colors.border }, ticks: { color: colors.text } },
                    x: { grid: { color: colors.border }, ticks: { color: colors.text } }
                }
            }
        });
    }

    const radarCanvas = document.getElementById("radarChart");
    if (radarCanvas) {
        new Chart(radarCanvas, {
            type: "radar",
            data: {
                labels: ["Build Health", "Modernization", "Composition"],
                datasets: [
                    {
                        label: "Score Breakdown",
                        data: [build, modern, composition],
                        backgroundColor: colors.primarySoft,
                        borderColor: colors.primary,
                        pointBackgroundColor: colors.primary
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { labels: { color: colors.text } }
                },
                scales: {
                    r: {
                        beginAtZero: true,
                        grid: { color: colors.border },
                        pointLabels: { color: colors.text },
                        ticks: { color: colors.text }
                    }
                }
            }
        });
    }
}

if (window.__ANDROID_DOCTOR_DATA__) {
    createCharts(window.__ANDROID_DOCTOR_DATA__);
}
