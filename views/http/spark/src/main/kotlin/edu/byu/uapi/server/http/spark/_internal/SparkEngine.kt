package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.engines.HttpEngine
import edu.byu.uapi.server.http.engines.RequestReader
import edu.byu.uapi.server.http.path.PathFormatter
import edu.byu.uapi.server.http.path.PathFormatters
import spark.Request

object SparkEngine: HttpEngine<Request> {
    override val engineName = "Spark"
    override val requestReader: RequestReader<Request>
        get() = SparkRequestReader
    override val pathFormatter: PathFormatter
        get() = PathFormatters.FLAT_COLON
}
