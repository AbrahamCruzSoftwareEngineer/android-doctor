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
    if (current === 0) return [20, 34, 46, 58];
    return [
        Math.max(0, current - 18),
        Math.max(0, current - 10),
        Math.max(0, current - 4),
        current
    ];
}

function hasRealData(values) {
    return values.some(value => typeof value === "number" && value !== 0);
}

function setNoData(chartId, visible) {
    const emptyEl = document.querySelector(`[data-chart-empty="${chartId}"]`);
    if (!emptyEl) return;
    emptyEl.classList.toggle("is-visible", visible);
}

function createCharts(data) {
    if (!window.Chart) {
        console.warn("Chart.js not found — charts will not render.");
        return;
    }

    if (!window.__ANDROID_DOCTOR_CHARTS__) {
        window.__ANDROID_DOCTOR_CHARTS__ = [];
    }

    window.__ANDROID_DOCTOR_CHARTS__.forEach(chart => chart.destroy());
    window.__ANDROID_DOCTOR_CHARTS__ = [];

    const colors = themeColors();
    Chart.defaults.color = colors.text;
    Chart.defaults.borderColor = colors.border;
    Chart.defaults.font.family = "\"Inter\", \"Roboto\", system-ui, sans-serif";

    const build = data.buildHealth ?? 0;
    const modern = data.modernization ?? 0;
    const composition = data.composition ?? 0;

    const buildTrend = generateTrend(build);
    const modernTrend = generateTrend(modern);

    const impactTotals = data.impactTotals || { buildHealth: 0, modernization: 0 };

    const trendCanvas = document.getElementById("trendChart");
    if (trendCanvas) {
        const trendHasData = hasRealData([build, modern]);
        const trendBuild = trendHasData ? buildTrend : [28, 42, 55, 64];
        const trendModern = trendHasData ? modernTrend : [22, 36, 49, 60];

        setNoData("trendChart", !trendHasData);

        window.__ANDROID_DOCTOR_CHARTS__.push(new Chart(trendCanvas, {
            type: "line",
            data: {
                labels: ["v1", "v2", "v3", "Now"],
                datasets: [
                    {
                        label: "Build Health",
                        data: trendBuild,
                        borderColor: colors.primary,
                        backgroundColor: colors.primarySoft,
                        tension: 0.35,
                        fill: true
                    },
                    {
                        label: "Modernization",
                        data: trendModern,
                        borderColor: colors.accent,
                        tension: 0.35
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 0 },
                transitions: {
                    active: { animation: { duration: 0 } },
                    resize: { animation: { duration: 0 } }
                },
                plugins: {
                    legend: { labels: { color: colors.text } }
                },
                scales: {
                    y: { beginAtZero: true, grid: { color: colors.border }, ticks: { color: colors.text } },
                    x: { grid: { color: colors.border }, ticks: { color: colors.text } }
                }
            }
        }));
    }

    const impactCanvas = document.getElementById("impactChart");
    if (impactCanvas) {
        const impactHasData = hasRealData([impactTotals.buildHealth ?? 0, impactTotals.modernization ?? 0]);
        const impactBuild = impactHasData ? impactTotals.buildHealth ?? 0 : 8;
        const impactModern = impactHasData ? impactTotals.modernization ?? 0 : 4;

        setNoData("impactChart", !impactHasData);

        window.__ANDROID_DOCTOR_CHARTS__.push(new Chart(impactCanvas, {
            type: "bar",
            data: {
                labels: ["Total Impact"],
                datasets: [
                    {
                        label: "Build Health",
                        data: [impactBuild],
                        backgroundColor: colors.primary
                    },
                    {
                        label: "Modernization",
                        data: [impactModern],
                        backgroundColor: colors.accent
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 0 },
                transitions: {
                    active: { animation: { duration: 0 } },
                    resize: { animation: { duration: 0 } }
                },
                plugins: {
                    legend: { labels: { color: colors.text } }
                },
                scales: {
                    y: { beginAtZero: true, grid: { color: colors.border }, ticks: { color: colors.text } },
                    x: { grid: { color: colors.border }, ticks: { color: colors.text } }
                }
            }
        }));
    }

    const buildTimeCanvas = document.getElementById("buildTimeChart");
    if (buildTimeCanvas) {
        const buildTime = data.buildTimeBreakdown || {};
        const configShare = buildTime.configuration ?? 0;
        const executionShare = buildTime.execution ?? 0;
        const annotationShare = buildTime.annotation ?? 0;
        const buildTimeHasData = hasRealData([configShare, executionShare, annotationShare]);

        setNoData("buildTimeChart", !buildTimeHasData);

        window.__ANDROID_DOCTOR_CHARTS__.push(new Chart(buildTimeCanvas, {
            type: "doughnut",
            data: {
                labels: ["Configuration", "Execution", "Annotation Processing"],
                datasets: [
                    {
                        data: buildTimeHasData
                            ? [configShare, executionShare, annotationShare]
                            : [40, 45, 15],
                        backgroundColor: [colors.primary, colors.accent, colors.primarySoft],
                        borderColor: colors.border,
                        borderWidth: 1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 0 },
                transitions: {
                    active: { animation: { duration: 0 } },
                    resize: { animation: { duration: 0 } }
                },
                plugins: {
                    legend: { position: "bottom", labels: { color: colors.text } },
                    tooltip: {
                        callbacks: {
                            label: context => `${context.label}: ${context.parsed}%`
                        }
                    }
                },
                cutout: "58%"
            }
        }));
    }

    const radarCanvas = document.getElementById("radarChart");
    if (radarCanvas) {
        const radarHasData = hasRealData([build, modern, composition]);
        const radarValues = radarHasData ? [build, modern, composition] : [58, 46, 32];

        setNoData("radarChart", !radarHasData);

        window.__ANDROID_DOCTOR_CHARTS__.push(new Chart(radarCanvas, {
            type: "radar",
            data: {
                labels: ["Build Health", "Modernization", "Composition"],
                datasets: [
                    {
                        label: "Score Breakdown",
                        data: radarValues,
                        backgroundColor: colors.primarySoft,
                        borderColor: colors.primary,
                        pointBackgroundColor: colors.primary,
                        borderWidth: 2,
                        pointRadius: 4,
                        pointHoverRadius: 5
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 0 },
                transitions: {
                    active: { animation: { duration: 0 } },
                    resize: { animation: { duration: 0 } }
                },
                plugins: {
                    legend: { labels: { color: colors.text } }
                },
                elements: {
                    line: { tension: 0.15 }
                },
                layout: {
                    padding: 8
                },
                scales: {
                    r: {
                        beginAtZero: true,
                        grid: { color: colors.border },
                        angleLines: { color: colors.border },
                        pointLabels: {
                            color: colors.text,
                            font: { size: 12, weight: "600" }
                        },
                        ticks: {
                            color: colors.text,
                            backdropColor: "transparent"
                        },
                        suggestedMax: 100
                    }
                }
            }
        }));
    }
}

if (window.__ANDROID_DOCTOR_DATA__) {
    createCharts(window.__ANDROID_DOCTOR_DATA__);
}

window.addEventListener("androiddoctor:themechange", () => {
    if (window.__ANDROID_DOCTOR_DATA__) {
        createCharts(window.__ANDROID_DOCTOR_DATA__);
    }
});
