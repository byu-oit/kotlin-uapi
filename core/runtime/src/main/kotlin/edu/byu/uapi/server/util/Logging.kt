package edu.byu.uapi.server.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T:Any> loggerFor(): Logger = LoggerFactory.getLogger(T::class.java)

fun Logger.error(createMessage: () -> String) {
    if (this.isErrorEnabled) {
        this.error(createMessage())
    }
}

fun Logger.warn(createMessage: () -> String) {
    if (this.isWarnEnabled) {
        this.warn(createMessage())
    }
}

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
