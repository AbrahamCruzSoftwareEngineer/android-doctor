package com.evolutiondso.androiddoctor.cli.render.html

object HtmlAssets {

    private fun load(path: String): String {
        val stream = HtmlAssets::class.java.getResourceAsStream("/html/$path")
            ?: error("Missing resource: /html/$path")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    fun styleCss(): String = load("style.css")
}
