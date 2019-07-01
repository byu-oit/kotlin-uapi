package edu.byu.uapi.server.util

import com.google.common.base.CaseFormat

private val CAMEL_TO_SNAKE = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE)

fun String.toSnakeCase() = CAMEL_TO_SNAKE.convert(this)!!
