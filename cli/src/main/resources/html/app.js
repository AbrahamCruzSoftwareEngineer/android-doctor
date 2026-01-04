const savedTheme = localStorage.getItem("androiddoctor-theme") || "light";
document.documentElement.setAttribute("data-theme", savedTheme);

/** Apply theme and persist */
function setTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("androiddoctor-theme", theme);
}

/** Toggle between light/dark */
function toggleTheme() {
    const current = localStorage.getItem("androiddoctor-theme") || "light";
    setTheme(current === "light" ? "dark" : "light");
}

const btn = document.getElementById("themeToggle");

btn.addEventListener("click", () => {

    if (!IS_PREMIUM) {
        alert("✨ Dark mode is a Premium-only feature!");
        return;
    }

    toggleTheme();
});
