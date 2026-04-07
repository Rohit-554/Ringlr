package io.jadu.ringlr.call.callkit

import io.jadu.ringlr.call.Call
import io.jadu.ringlr.call.CallState
import platform.Foundation.NSUUID

/**
 * Tracks all in-flight calls, bridging the domain callId (String) to the
 * system UUID (NSUUID) that CallKit requires for every action.
 */
internal class ActiveCallRegistry {

    private val callsById = mutableMapOf<String, Call>()
    private val uuidToCallId = mutableMapOf<String, String>()

    fun register(call: Call, uuid: NSUUID) {
        callsById[call.id] = call
        uuidToCallId[uuid.UUIDString] = call.id
    }

    fun findById(callId: String): Call? = callsById[callId]

    fun findByUUID(uuid: NSUUID): Call? =
        uuidToCallId[uuid.UUIDString]?.let { callsById[it] }

    fun uuidFor(callId: String): NSUUID? {
        val uuidString = uuidToCallId.entries
            .firstOrNull { it.value == callId }
            ?.key ?: return null
        return NSUUID(uUIDString = uuidString)
    }

    fun transition(callId: String, to: CallState): Call? {
        val updated = callsById[callId]?.copy(state = to) ?: return null
        callsById[callId] = updated
        return updated
    }

    fun remove(callId: String) {
        callsById.remove(callId)
        uuidToCallId.entries.removeAll { it.value == callId }
    }

    fun allActive(): List<Call> = callsById.values.toList()
}
