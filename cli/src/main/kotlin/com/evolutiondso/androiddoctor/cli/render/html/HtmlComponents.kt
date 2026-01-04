package com.evolutiondso.androiddoctor.cli.render.html

object HtmlComponents {

    fun gradientHeader(title: String, extraRightContent: String = ""): String = """
        <header class="header">
            <div class="header-left">$title</div>
            <div class="header-right">$extraRightContent</div>
        </header>
    """.trimIndent()

    fun premiumThemeToggle(): String = """
        <div class="theme-toggle">
            <button onclick="setTheme('light')" id="lightBtn">Light</button>
            <button onclick="setTheme('dark')" id="darkBtn">Dark</button>
        </div>
    """.trimIndent()

    fun freeThemeToggle(): String = """
        <div class="theme-toggle disabled">
            <button disabled> Light</button>
            <button disabled> Dark</button>
            <span class="upsell">Upgrade to Premium to enable themes</span>
        </div>
    """.trimIndent()

    fun css(): String = """
        <style>
            body {
                margin: 0;
                font-family: 'Roboto', sans-serif;
                background: var(--bg);
                color: var(--text);
                transition: background 0.3s, color 0.3s;
            }

            :root.light {
                --bg: #ffffff;
                --text: #222222;
                --card-bg: #f7f9fc;
                --border: #e5e8ec;
            }

            :root.dark {
                --bg: #111827;
                --text: #f3f4f6;
                --card-bg: #1f2937;
                --border: #374151;
            }

            .header {
                padding: 16px 24px;
                background: linear-gradient(135deg, #85c1ff 0%, #4a90e2 100%);
                color: white;
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-size: 22px;
                font-weight: 600;
            }

            .theme-toggle button {
                margin-left: 8px;
                padding: 6px 12px;
                border-radius: 6px;
                border: none;
                cursor: pointer;
            }
            .theme-toggle.disabled button {
                opacity: 0.5;
                cursor: not-allowed;
            }
            .theme-toggle .upsell {
                margin-left: 12px;
                font-size: 13px;
                color: #e4f1ff;
            }

            .container {
                padding: 28px;
                max-width: 900px;
                margin: auto;
            }

            .card {
                background: var(--card-bg);
                padding: 20px;
                border-radius: 10px;
                border: 1px solid var(--border);
                margin-bottom: 20px;
            }

            .section-title {
                font-size: 20px;
                font-weight: 600;
                margin-bottom: 12px;
            }

            .action-item {
                margin-bottom: 18px;
            }
            .impact {
                font-size: 13px;
                opacity: 0.8;
                margin-top: 4px;
            }
        </style>
    """.trimIndent()

    fun themeScript(): String = """
        <script>
            function setTheme(mode) {
                document.documentElement.className = mode;
            }
        </script>
    """.trimIndent()
}
