package edu.byu.uapi.server.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T:Any> loggerFor() = LoggerFactory.getLogger(T::class.java)

fun Logger.info(createMessage: () -> String) {
    if (this.isInfoEnabled) {
        this.info(createMessage())
    }
}

fun Logger.debug(createMessage: () -> String) {
    if (this.isDebugEnabled) {
        this.debug(createMessage())
    }
}
