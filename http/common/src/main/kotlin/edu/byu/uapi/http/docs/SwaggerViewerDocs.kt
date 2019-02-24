package edu.byu.uapi.http.docs

import edu.byu.uapi.model.UAPIModel
import org.intellij.lang.annotations.Language
import java.io.ByteArrayInputStream
import java.io.InputStream

class SwaggerViewerDocs(val model: UAPIModel) : DocSource {
    override val name: String = "api.html"
    override val contentType: String = "text/html"

    override fun getInputStream(pretty: Boolean): InputStream {
        return ByteArrayInputStream(html.toByteArray())
    }

    @Language("HTML")
    private val html = """
        <!doctype html>
        <html>
          <head>
            <meta charset="utf-8">
            <script async src="https://cdn.byu.edu/byu-theme-components/1.x.x/byu-theme-components.min.js"></script>
            <link rel="stylesheet" href="https://cdn.byu.edu/byu-theme-components/1.x.x/byu-theme-components.min.css" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <script async src="https://unpkg.com/rapidoc/dist/rapidoc-min.js"></script>
            <title>${model.info.name} ${model.info.version} API Documentation</title>
            <style>
              body {
                display: flex;
                flex-direction: column;
                min-height: 100vh;
              }
              main {
                width: 100%;
                max-width: 1200px;
                flex-grow: 1;
                margin: auto;
              }
            </style>
          </head>
          <body>
            <byu-header>
              <h1 slot="site-title">${model.info.name} ${model.info.version} API Documentation</h1>
            </byu-header>
            <main>
              <rapi-doc spec-url="openapi3.json" header-color="#FFFFFF" primary-color="#0057B8" regular-font="'HCo Ringside Narrow SSm'"></rapi-doc>
            </main>
            <byu-footer></byu-footer>
          </body>
        </html>
    """.trimIndent()
}

