package com.evolutiondso.androiddoctor.cli.render.html

import java.nio.charset.StandardCharsets

object HtmlAssets {

    private fun load(name: String): String {
        val path = "html/$name"
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: error("Missing asset: $path")

        return stream.readBytes().toString(StandardCharsets.UTF_8)
    }

    fun styleCss(): String = load("style.css")

    fun appJs(isPremium: Boolean): String {
        val raw = load("app.js")
        return raw.replace("/*__PREMIUM_FLAG__*/", "const IS_PREMIUM = $isPremium;")
    }

    fun chartsJs(): String = load("charts.js")
}
