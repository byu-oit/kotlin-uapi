package edu.byu.uapidsl.model.validation


interface Validating {

    fun expect(field: String, should: String, condition: () -> Boolean): Boolean

}

