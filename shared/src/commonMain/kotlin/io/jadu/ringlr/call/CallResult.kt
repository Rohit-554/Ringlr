package io.jadu.ringlr.call

/**
 * Wraps the outcome of every [CallManagerInterface] operation.
 *
 * Use [Success.data] to access the result on the happy path,
 * or [Error.error] to inspect the typed [CallError] on failure.
 * Avoids unchecked exceptions crossing the API boundary.
 */
sealed class CallResult<out T> {
    data class Success<T>(val data: T) : CallResult<T>()
    data class Error(val error: CallError) : CallResult<Nothing>()
}
