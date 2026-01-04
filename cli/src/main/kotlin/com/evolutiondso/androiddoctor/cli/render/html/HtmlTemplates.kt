package com.evolutiondso.androiddoctor.cli.render.html

object HtmlTemplates {

    val css = """
        body {
            font-family: Inter, system-ui, sans-serif;
            background: #f7f9fc;
            color: #222;
            padding: 2rem;
            line-height: 1.6;
        }
        .card {
            background: white;
            padding: 1.5rem;
            border-radius: 12px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.05);
            margin-bottom: 1.5rem;
        }
        h1 {
            font-size: 2rem;
            margin-bottom: 0.25rem;
        }
        h2 {
            margin-top: 2rem;
        }
        .tag {
            display:inline-block;
            padding: 0.25rem 0.6rem;
            border-radius: 6px;
            font-size: 0.8rem;
            font-weight: bold;
        }
        .tag-high { background: #ffcccc; color: #a40000; }
        .tag-medium { background: #ffe8b0; color: #8a5200; }
        .tag-low { background: #dff7df; color: #1f7a1f; }
        .score-bar-container {
            height: 10px;
            background: #e5e5e5;
            border-radius: 6px;
            margin-top: 4px;
        }
        .score-bar-fill {
            height: 10px;
            background: #4a90e2;
            border-radius: 6px;
        }
    """.trimIndent()


    fun page(title: String, body: String) = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8"/>
            <title>$title</title>
            <style>$css</style>
        </head>
        <body>
            $body
        </body>
        </html>
    """.trimIndent()
}
