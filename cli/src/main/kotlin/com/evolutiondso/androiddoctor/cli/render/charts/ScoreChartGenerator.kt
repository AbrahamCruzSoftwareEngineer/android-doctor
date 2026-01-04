package com.evolutiondso.androiddoctor.cli.render.charts

object ScoreChartGenerator {

    fun generateDualScoreChart(build: Int, modern: Int): String {

        fun bar(label: String, value: Int): String = """
            <div style="margin-bottom: 1rem;">
                <strong>$label: $value / 100</strong>
                <div style="height:10px; background:#ddd; border-radius:4px;">
                    <div style="width:${value}%; height:10px; background:#4a90e2; border-radius:4px;"></div>
                </div>
            </div>
        """

        return """
            <div>
                ${bar("Build Health", build)}
                ${bar("Modernization", modern)}
            </div>
        """
    }
}
