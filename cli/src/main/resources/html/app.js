// Always start in LIGHT mode
document.documentElement.setAttribute("data-theme", "light");

// Injected from Kotlin
const IS_PREMIUM = {{IS_PREMIUM}};

// Theme setter
function setTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("androiddoctor-theme", theme);
}

// Toggle theme
function toggleTheme() {
    const curr = localStorage.getItem("androiddoctor-theme") || "light";
    setTheme(curr === "light" ? "dark" : "light");
}

// Handle button
const btn = document.getElementById("themeToggle");

btn.addEventListener("click", () => {

    if (!IS_PREMIUM) {
        alert("✨ Dark mode is a Premium feature!");
        return;
    }

    toggleTheme();
});
