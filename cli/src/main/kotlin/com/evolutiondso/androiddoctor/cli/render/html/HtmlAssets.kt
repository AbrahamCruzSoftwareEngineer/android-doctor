package com.evolutiondso.androiddoctor.cli.render.html

object HtmlAssets {

    private fun load(path: String): String =
        javaClass.classLoader.getResource(path)?.readText()
            ?: error("Missing asset: $path")

    fun styleCss(): String = load("html/style.css")

    fun appJs(premium: Boolean): String =
        load("html/app.js")
            .replace("{{IS_PREMIUM}}", premium.toString())
}
