package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.HttpRoute
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor

fun HttpRoute.toInterceptor(): PipelineInterceptor<Unit, ApplicationCall> {
    return {

    }
}
