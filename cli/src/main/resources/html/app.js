const initialTheme = "light";
const savedTheme = localStorage.getItem("androiddoctor-theme");

function setTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("androiddoctor-theme", theme);
}

function applySavedTheme() {
    if (!IS_PREMIUM) return;
    if (!savedTheme) return;
    setTheme(savedTheme);
}

function toggleTheme() {
    const current = localStorage.getItem("androiddoctor-theme") || initialTheme;
    setTheme(current === "light" ? "dark" : "light");
}

const btn = document.getElementById("themeToggle");

if (btn) {
    btn.addEventListener("click", () => {
        if (!IS_PREMIUM) {
            return;
        }
        toggleTheme();
    });
}

document.documentElement.setAttribute("data-theme", initialTheme);

window.addEventListener("DOMContentLoaded", () => {
    setTimeout(applySavedTheme, 0);
});
