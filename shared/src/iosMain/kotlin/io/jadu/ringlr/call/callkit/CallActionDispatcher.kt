package io.jadu.ringlr.call.callkit

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CallKit.CXCallController
import platform.CallKit.CXTransaction
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Submits call actions to the system via CXCallController.
 *
 * CallKit validates each action and routes it back through [SystemCallBridge]
 * if approved. This class is the outgoing channel only — it never modifies state.
 */
internal class CallActionDispatcher {

    private val controller = CXCallController()

    suspend fun dispatch(transaction: CXTransaction) {
        suspendCancellableCoroutine { continuation ->
            controller.requestTransaction(transaction) { error ->
                if (!continuation.isActive) return@requestTransaction
                if (error == null) continuation.resume(Unit)
                else continuation.resumeWithException(Exception(error.localizedDescription))
            }
        }
    }
}
