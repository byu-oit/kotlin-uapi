package edu.byu.uapi.server.types

sealed class CreateIdResult<out Id : Any> {
    data class Success<Id : Any>(val id: Id) : CreateIdResult<Id>()
    object Unauthorized : CreateIdResult<Nothing>()
    data class InvalidInput(override val errors: List<InputError>) : CreateIdResult<Nothing>(),
                                                                     InvalidInputResult {
        constructor(
            field: String,
            description: String
        ) : this(listOf(InputError(field, description)))
    }

    data class Error(
        override val code: Int,
        override val errors: List<String>,
        override val cause: Throwable? = null
    ) : CreateIdResult<Nothing>(),
        ErrorResult {
        constructor(
            code: Int,
            error: String
        ) : this(code, listOf(error))
    }
}

sealed class UpdateResult {
    object Success : UpdateResult()
    data class InvalidInput(override val errors: List<InputError>) : UpdateResult(),
                                                                     InvalidInputResult {
        constructor(
            field: String,
            description: String
        ) : this(listOf(InputError(field, description)))
    }

    object Unauthorized : UpdateResult()
    data class CannotBeUpdated(val reason: String) : UpdateResult()

    data class Error(
        override val code: Int,
        override val errors: List<String>,
        override val cause: Throwable? = null
    ) : UpdateResult(),
        ErrorResult {
        constructor(
            code: Int,
            error: String
        ) : this(code, listOf(error))
    }
}

sealed class CreateResult {
    object Success : CreateResult()
    object Unauthorized : CreateResult()
    data class InvalidInput(override val errors: List<InputError>) : CreateResult(),
                                                                     InvalidInputResult {
        constructor(
            field: String,
            description: String
        ) : this(listOf(InputError(field, description)))
    }

    data class Error(
        override val code: Int,
        override val errors: List<String>,
        override val cause: Throwable? = null
    ) : CreateResult(),
        ErrorResult {
        constructor(
            code: Int,
            error: String
        ) : this(code, listOf(error))
    }
}

sealed class DeleteResult {
    object Success : DeleteResult()
    object AlreadyDeleted : DeleteResult()
    object Unauthorized : DeleteResult()
    data class CannotBeDeleted(val reason: String) : DeleteResult()
    data class Error(
        override val code: Int,
        override val errors: List<String>,
        override val cause: Throwable? = null
    ) : DeleteResult(),
        ErrorResult
}

interface InvalidInputResult {
    val errors: List<InputError>
}

interface ErrorResult {
    val code: Int
    val errors: List<String>
    val cause: Throwable?
}

data class InputError(
    val field: String,
    val description: String
)

