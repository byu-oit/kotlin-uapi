package edu.byu.uapi.server.types

sealed class SuccessOrFailure<out Success, out Fail> {
}

data class Success<out Value>(
    val value: Value
): SuccessOrFailure<Value, Nothing>()

data class Failure<out Value>(
    val value: Value
): SuccessOrFailure<Nothing, Value>()

inline fun <S, F, R> SuccessOrFailure<S, F>.map(
    happy: (S) -> R,
    sad: (F) -> R
) = when(this) {
    is Success<S> -> happy(this.value)
    is Failure<F> -> sad(this.value)
}

fun <T: Any> T.asSuccess(): Success<T> = Success(this)
fun <T: Any> T.asFailure(): Failure<T> = Failure(this)
