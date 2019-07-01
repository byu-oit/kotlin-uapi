package edu.byu.uapi.server.types

sealed class CreateResult<out Model: Any> {
    data class Success<Model: Any>(
        val model: Model
    ) : CreateResult<Model>()
    object Unauthorized : CreateResult<Nothing>()
    data class InvalidInput(override val errors: List<InputError>) : CreateResult<Nothing>(),
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
    ) : CreateResult<Nothing>(),
        ErrorResult {
        constructor(
            code: Int,
            error: String
        ) : this(code, listOf(error))
    }
}

sealed class UpdateResult<out Model: Any> {
    data class Success<Model: Any>(
        val model: Model
    ): UpdateResult<Model>()
    data class InvalidInput(override val errors: List<InputError>) : UpdateResult<Nothing>(),
                                                                     InvalidInputResult {
        constructor(
            field: String,
            description: String
        ) : this(listOf(InputError(field, description)))
    }

    object Unauthorized : UpdateResult<Nothing>()
    data class CannotBeUpdated(val reason: String) : UpdateResult<Nothing>()

    data class Error(
        override val code: Int,
        override val errors: List<String>,
        override val cause: Throwable? = null
    ) : UpdateResult<Nothing>(),
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

