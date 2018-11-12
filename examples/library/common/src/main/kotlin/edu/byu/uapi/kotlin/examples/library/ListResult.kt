package edu.byu.uapi.kotlin.examples.library

data class ListResult<T>(
    val list: List<T>,
    val totalItems: Int
)
