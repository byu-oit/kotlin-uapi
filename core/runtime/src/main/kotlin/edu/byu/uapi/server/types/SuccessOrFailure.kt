package edu.byu.uapi.server.types

sealed class SuccessOrFailure<Success, Fail> {
}

data class Success<Value>(
    val value: Value
): SuccessOrFailure<Value, Nothing>()

data class Failure<Value>(
    val value: Value
): SuccessOrFailure<Nothing, Value>()

fun <S, F, R> SuccessOrFailure<S, F>.map(
    happy: (S) -> R,
    sad: (F) -> R
) = when(this) {
    is Success<S> -> happy(this.value)
    is Failure<F> -> sad(this.value)
}
