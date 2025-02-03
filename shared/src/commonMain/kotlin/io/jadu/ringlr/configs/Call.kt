package io.jadu.ringlr.configs

/**
 * Represents a call in the system with its associated properties.
 * @property id Unique identifier for the call
 * @property number The phone number associated with the call
 * @property displayName The display name of the contact if available
 * @property state Current state of the call
 * @property createdAt Timestamp when the call was created
 */
data class Call(
    val id: String,
    val number: String,
    val displayName: String,
    val state: CallState,
    val createdAt: Long
)