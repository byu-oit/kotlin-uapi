package edu.byu.uapi.server.types

sealed class SuccessOrFailure<out Success, out Fail> {
    abstract fun <R> resolve(happy: (Success) -> R, sad: (Fail) -> R): R
}

data class Success<Value>(
    val value: Value
): SuccessOrFailure<Value, Nothing>() {
    override fun <R> resolve(
        happy: (Value) -> R,
        sad: (Nothing) -> R
    ): R = happy(value)
}

data class Failure<Value>(
    val value: Value
): SuccessOrFailure<Nothing, Value>() {
    override fun <R> resolve(
        happy: (Nothing) -> R,
        sad: (Value) -> R
    ): R = sad(value)
}

fun <S, F> SuccessOrFailure<S, F>.onFailure(fn: (F) -> S): S {
    return this.resolve({it}, fn)
}

fun <T: Any> T.asSuccess(): Success<T> = Success(this)
fun <T: Any> T.asFailure(): Failure<T> = Failure(this)
