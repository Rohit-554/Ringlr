package io.jadu.ringlr.configs

/**
 * A generic result type that wraps all operations to handle success and failure cases.
 * @param T The type of the success result
 */
sealed class CallResult<out T> {
    data class Success<T>(val data: T) : CallResult<T>()
    data class Error(val error: CallError) : CallResult<Nothing>()
}