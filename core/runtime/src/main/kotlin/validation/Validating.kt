package edu.byu.uapi.server.validation


interface Validating {

    fun expect(field: String, should: String, condition: () -> Boolean): Boolean

}

