package edu.byu.uapi.spi.validation

import kotlin.reflect.KClass

interface ValidationEngine {
    fun <T: Any> validatorFor(type: KClass<T>): Validator<T>

    companion object {
        fun noop(): ValidationEngine = NoOp
    }

    private object NoOp: ValidationEngine {
        override fun <T : Any> validatorFor(type: KClass<T>): Validator<T> = Validator.noop()
    }
}

interface Validator<in Type: Any> {
    fun validate(subject: Type): Set<ValidationFailure>
    fun describeConstraints(): Set<ValidationConstraint>

    companion object {
        fun <T: Any> noop(): Validator<T> = NoOp()
    }

    class NoOp<in T: Any>: Validator<T> {
        override fun validate(subject: T): Set<ValidationFailure> = emptySet()

        override fun describeConstraints(): Set<ValidationConstraint> = emptySet()
    }
}

data class ValidationConstraint(
    val path: String,
    val description: String
)

