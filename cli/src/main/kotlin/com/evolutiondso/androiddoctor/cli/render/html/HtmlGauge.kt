package com.evolutiondso.androiddoctor.cli.render.html

object HtmlGauge {

    fun gauge(value: Int?, label: String): String {
        val safeValue = (value ?: 0).coerceIn(0, 100)

        return """
        <div class="gauge-card">
            <div class="gauge-container" data-value="$safeValue">
                <svg viewBox="0 0 100 100" class="gauge">
                    <path class="gauge-bg" d="M10 90 A40 40 0 1 1 90 90" />
                    <path class="gauge-fill" d="M10 90 A40 40 0 1 1 90 90" />
                </svg>
                <div class="gauge-text">
                    <div class="gauge-value">$safeValue</div>
                    <div class="gauge-label">$label</div>
                </div>
            </div>
        </div>
        """
    }
}
