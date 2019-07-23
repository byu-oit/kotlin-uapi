package edu.byu.uapi.server.http.http4k._internal

import edu.byu.uapi.server.http.engines.HttpEngine
import edu.byu.uapi.server.http.engines.RequestReader
import edu.byu.uapi.server.http.path.PathFormatter
import edu.byu.uapi.server.http.path.PathFormatters
import org.http4k.core.Request

object Http4kEngine : HttpEngine<Request> {
    override val engineName: String
        get() = "http4k"

    override val requestReader: RequestReader<Request>
        get() = Http4kRequestReader

    override val pathFormatter: PathFormatter = PathFormatters.CURLY_BRACE
}
