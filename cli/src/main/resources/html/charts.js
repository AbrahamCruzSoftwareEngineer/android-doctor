// ------------------------------
// Theme-aware color helpers
// ------------------------------
function getCssVar(name) {
    return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

function themeColors() {
    return {
        fg: getCssVar('--fg'),
        bg: getCssVar('--bg'),
        primary: getCssVar('--primary'),
        primarySoft: getCssVar('--primary-soft'),
        accent: getCssVar('--accent'),
        grid: getCssVar('--chart-grid'),
    };
}

// ------------------------------
// Fake Trend Data Generator
// ------------------------------
function generateFakeTrend(current) {
    if (!current) return [60, 65, 70, 75];
    return [
        Math.max(0, current - 15),
        Math.max(0, current - 8),
        Math.max(0, current - 4),
        current
    ];
}

// ------------------------------
// Chart Initializer
// ------------------------------
function createCharts(data) {
    if (!window.Chart) {
        console.warn("Chart.js not found — charts will not render.");
        return;
    }

    const colors = themeColors();
    const build = data.buildHealth ?? 0;
    const modern = data.modernization ?? 0;

    const buildTrend = generateFakeTrend(build);
    const modernTrend = generateFakeTrend(modern);

    const actions = data.actions ?? [];
    const labels = actions.map(a => a.title);
    const impactBuild = actions.map(a => a.impact?.buildHealthDelta ?? 0);
    const impactModern = actions.map(a => a.impact?.modernizationDelta ?? 0);

    const radarValues = [
        build,
        modern,
        data.usesKapt ? 30 : 80,
        data.moduleCount > 1 ? 80 : 40
    ];

    // Trend Chart
    new Chart(document.getElementById("trendChart"), {
        type: "line",
        data: {
            labels: ["v1", "v2", "v3", "Now"],
            datasets: [
                {
                    label: "Build Health",
                    data: buildTrend,
                    borderColor: colors.primary,
                    backgroundColor: colors.primarySoft,
                    tension: 0.3,
                    fill: true
                },
                {
                    label: "Modernization",
                    data: modernTrend,
                    borderColor: colors.accent,
                    tension: 0.3
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                y: { beginAtZero: true, grid: { color: colors.grid } },
                x: { grid: { color: colors.grid } }
            }
        }
    });

    // Impact Chart
    new Chart(document.getElementById("impactChart"), {
        type: "bar",
        data: {
            labels,
            datasets: [
                {
                    label: "Build Health Impact",
                    data: impactBuild,
                    backgroundColor: colors.primary
                },
                {
                    label: "Modernization Impact",
                    data: impactModern,
                    backgroundColor: colors.accent
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                y: { beginAtZero: true, grid: { color: colors.grid } }
            }
        }
    });

    // Radar Chart
    new Chart(document.getElementById("radarChart"), {
        type: "radar",
        data: {
            labels: ["Build Health", "Modernization", "Kapt Usage", "Modularity"],
            datasets: [
                {
                    label: "Score Breakdown",
                    data: radarValues,
                    backgroundColor: colors.primarySoft,
                    borderColor: colors.primary,
                    pointBackgroundColor: colors.primary
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                r: {
                    beginAtZero: true,
                    grid: { color: colors.grid },
                    pointLabels: { color: colors.fg }
                }
            }
        }
    });
}

// ------------------------------
// Initialize when data exists
// ------------------------------
if (window.__ANDROID_DOCTOR_DATA__) {
    createCharts(window.__ANDROID_DOCTOR_DATA__);
}
