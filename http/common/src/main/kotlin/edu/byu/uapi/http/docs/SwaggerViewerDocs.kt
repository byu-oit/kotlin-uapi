package edu.byu.uapi.http.docs

import java.io.ByteArrayInputStream
import java.io.InputStream

class SwaggerViewerDocs : DocSource {
    override val name: String = "api.html"
    override val contentType: String = "text/html"

    override fun getInputStream(pretty: Boolean): InputStream {
        return ByteArrayInputStream(html.toByteArray())
    }

    private val html = """
        <html>
          <head>
            <meta charset="utf-8">
            <script async src="https://cdn.byu.edu/byu-theme-components/1.x.x/byu-theme-components.min.js"></script>
            <link rel="stylesheet" href="https://cdn.byu.edu/byu-theme-components/1.x.x/byu-theme-components.min.css" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <script async src="https://unpkg.com/rapidoc/dist/rapidoc-min.js"></script>
            <style>
              body {
                display: flex;
                flex-direction: column;
              }
              main {
                width: 100%;
                max-width: 1200px;
                flex-grow: 1;
              }
            </style>
          </head>
          <body>
            <byu-header>
              <h1 slot="site-title">UAPI Documentation</h1>
            </byu-header>
            <main>
              <rapi-doc spec-url="openapi3.json"></rapi-doc>
            </main>
            <byu-footer></byu-footer>
          </body>
        </html>
    """.trimIndent()
}

