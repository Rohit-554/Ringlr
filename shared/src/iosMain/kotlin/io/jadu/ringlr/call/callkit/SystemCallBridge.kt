@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.jadu.ringlr.call.callkit

import io.jadu.ringlr.call.Call
import io.jadu.ringlr.call.CallState
import io.jadu.ringlr.call.CallStateCallback
import platform.CallKit.CXAnswerCallAction
import platform.CallKit.CXCallUpdate
import platform.CallKit.CXEndCallAction
import platform.CallKit.CXHandle
import platform.CallKit.CXHandleTypeGeneric
import platform.CallKit.CXHandleTypePhoneNumber
import platform.CallKit.CXProvider
import platform.CallKit.CXProviderConfiguration
import platform.CallKit.CXProviderDelegateProtocol
import platform.CallKit.CXSetHeldCallAction
import platform.CallKit.CXSetMutedCallAction
import platform.CallKit.CXStartCallAction
import platform.Foundation.NSUUID
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

/**
 * Bridges CallKit system events into domain [CallStateCallback] notifications.
 *
 * Owns the single [CXProvider] instance (CallKit requires one per app).
 * Translates each system action into a [CallState] transition and
 * notifies all registered observers.
 */
internal class SystemCallBridge(
    private val registry: ActiveCallRegistry,
    private val observers: MutableList<CallStateCallback>,
    appName: String = "Ringlr"
) : NSObject(), CXProviderDelegateProtocol {

    val provider: CXProvider = buildProvider(appName)

    init {
        provider.setDelegate(this, queue = dispatch_get_main_queue())
    }

    fun reportIncomingCall(uuid: NSUUID, call: Call) {
        registry.register(call, uuid)
        provider.reportNewIncomingCallWithUUID(uuid, update = buildCallUpdate(call)) { error ->
            if (error != null) return@reportNewIncomingCallWithUUID
            val ringing = registry.transition(call.id, CallState.RINGING) ?: return@reportNewIncomingCallWithUUID
            observers.forEach { it.onCallAdded(ringing) }
            observers.forEach { it.onCallStateChanged(ringing) }
        }
    }

    override fun providerDidReset(provider: CXProvider) {
        registry.allActive().forEach { call ->
            val ended = registry.transition(call.id, CallState.ENDED) ?: return@forEach
            notifyEnded(ended)
            registry.remove(call.id)
        }
    }

    override fun provider(provider: CXProvider, performStartCallAction: CXStartCallAction) {
        val call = registry.findByUUID(performStartCallAction.callUUID)
        if (call != null) {
            val dialing = registry.transition(call.id, CallState.DIALING)
            dialing?.let { observers.forEach { cb -> cb.onCallStateChanged(it) } }
            provider.reportOutgoingCallWithUUID(performStartCallAction.callUUID, startedConnectingAtDate = null)
        }
        performStartCallAction.fulfill()
    }

    override fun provider(provider: CXProvider, performAnswerCallAction: CXAnswerCallAction) {
        transitionAndNotify(performAnswerCallAction.callUUID, CallState.ACTIVE)
        performAnswerCallAction.fulfill()
    }

    override fun provider(provider: CXProvider, performEndCallAction: CXEndCallAction) {
        val call = registry.findByUUID(performEndCallAction.callUUID)
        if (call != null) {
            val ended = registry.transition(call.id, CallState.ENDED)
            ended?.let { notifyEnded(it) }
            registry.remove(call.id)
        }
        performEndCallAction.fulfill()
    }

    override fun provider(provider: CXProvider, performSetMutedCallAction: CXSetMutedCallAction) {
        performSetMutedCallAction.fulfill()
    }

    override fun provider(provider: CXProvider, performSetHeldCallAction: CXSetHeldCallAction) {
        val nextState = if (performSetHeldCallAction.onHold) CallState.HOLDING else CallState.ACTIVE
        transitionAndNotify(performSetHeldCallAction.callUUID, nextState)
        performSetHeldCallAction.fulfill()
    }

    private fun transitionAndNotify(uuid: NSUUID, state: CallState) {
        val call = registry.findByUUID(uuid) ?: return
        val updated = registry.transition(call.id, state) ?: return
        observers.forEach { it.onCallStateChanged(updated) }
    }

    private fun notifyEnded(call: Call) {
        observers.forEach { it.onCallStateChanged(call) }
        observers.forEach { it.onCallRemoved(call) }
    }

    private fun buildCallUpdate(call: Call): CXCallUpdate =
        CXCallUpdate().apply {
            val isVoip = call.scheme == "sip"
            val handleType = if (isVoip) CXHandleTypeGeneric else CXHandleTypePhoneNumber
            val handleValue = if (isVoip) "sip:${call.number}" else call.number
            remoteHandle = CXHandle(type = handleType, value = handleValue)
            localizedCallerName = call.displayName
            supportsHolding = true
            supportsDTMF = !isVoip
            supportsGrouping = false
            supportsUngrouping = false
        }

    private fun buildProvider(appName: String): CXProvider {
        val config = CXProviderConfiguration(localizedName = appName).apply {
            supportsVideo = false
            maximumCallGroups = 1u
            maximumCallsPerCallGroup = 1u
        }
        return CXProvider(configuration = config)
    }
}
