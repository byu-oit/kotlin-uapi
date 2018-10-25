package edu.byu.uapi.spi.functional

@Suppress("unused")
sealed class SuccessOrFailure<out Success, out Fail> {
    abstract val success: Boolean
    val failure: Boolean
        get() = !this.success
}

inline fun <S, F, R> SuccessOrFailure<S, F>.resolve(
    happy: (S) -> R,
    sad: (F) -> R
): R {
    // We could use a type-checking 'when', but this should be slightly faster.
    return if (this.success) {
        happy((this as Success).value)
    } else {
        sad((this as Failure).value)
    }
}

inline fun <S, F> SuccessOrFailure<S, F>.ifSuccess(
    fn: (S) -> Unit
): SuccessOrFailure<S, F> {
    if (this.success) {
        fn((this as Success).value)
    }
    return this
}

inline fun <S, F> SuccessOrFailure<S, F>.ifFailure(
    fn: (F) -> Unit
): SuccessOrFailure<S, F> {
    if (this.failure) {
        fn((this as Failure).value)
    }
    return this
}

data class Success<Value>(
    val value: Value
) : SuccessOrFailure<Value, Nothing>() {
    override val success: Boolean = true
}

data class Failure<Value>(
    val value: Value
) : SuccessOrFailure<Nothing, Value>() {
    override val success: Boolean = false
}

inline fun <S, F> SuccessOrFailure<S, F>.onFailure(fn: (F) -> S): S {
    return this.resolve({ it }, fn)
}

inline fun <S1, S2, F> SuccessOrFailure<S1, F>.map(fn: (S1) -> S2): SuccessOrFailure<S2, F> {
    return this.flatMap { Success(fn(it)) }
}

inline fun <S1, S2, F> SuccessOrFailure<S1, F>.flatMap(fn: (S1) -> SuccessOrFailure<S2, F>): SuccessOrFailure<S2, F> {
    return this.resolve({ fn(it) }, { Failure(it) })
}

fun <T : Any> T.asSuccess(): Success<T> = Success(this)
fun <T : Any> T.asFailure(): Failure<T> = Failure(this)
