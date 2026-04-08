package io.jadu.ringlr.call

/**
 * Immutable snapshot of a call at a point in time.
 *
 * Every state change produces a new [Call] instance. The [id] maps to the
 * platform call handle — NSUUID string on iOS, connection tag on Android.
 */
data class Call(
    val id: String,
    val number: String,
    val displayName: String,
    val state: CallState,
    val createdAt: Long,
    val scheme: String = "tel"
)
