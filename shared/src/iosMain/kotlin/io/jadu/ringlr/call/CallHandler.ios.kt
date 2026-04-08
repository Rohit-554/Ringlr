@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.jadu.ringlr.call

import io.jadu.ringlr.call.callkit.ActiveCallRegistry
import io.jadu.ringlr.call.callkit.CallActionDispatcher
import io.jadu.ringlr.call.callkit.CallAudioRouter
import io.jadu.ringlr.call.callkit.SystemCallBridge
import platform.CallKit.CXEndCallAction
import platform.CallKit.CXHandle
import platform.CallKit.CXHandleTypeGeneric
import platform.CallKit.CXHandleTypePhoneNumber
import platform.CallKit.CXSetHeldCallAction
import platform.CallKit.CXSetMutedCallAction
import platform.CallKit.CXStartCallAction
import platform.CallKit.CXTransaction
import platform.Foundation.NSUUID
import platform.posix.time

actual class PlatformConfiguration private constructor() {

    internal val registry = ActiveCallRegistry()
    internal val audioRouter = CallAudioRouter()
    internal val callObservers = mutableListOf<CallStateCallback>()

    internal lateinit var systemBridge: SystemCallBridge
        private set

    internal lateinit var actionDispatcher: CallActionDispatcher
        private set

    actual fun initializeCallConfiguration() {
        systemBridge = SystemCallBridge(registry, callObservers)
        actionDispatcher = CallActionDispatcher()
        audioRouter.activate()
    }

    actual fun initializeCustomCallConfiguration(
        setHighlightColor: Int,
        setDescription: String,
        setSupportedUriSchemes: List<String>
    ) {
        if (!::systemBridge.isInitialized) initializeCallConfiguration()
    }

    actual fun cleanupCallConfiguration() {
        if (::systemBridge.isInitialized) systemBridge.provider.invalidate()
        audioRouter.deactivate()
    }

    actual companion object {
        actual fun create(): PlatformConfiguration = PlatformConfiguration()
    }
}

actual class CallManager actual constructor(
    private val configuration: PlatformConfiguration
) : CallManagerInterface {

    private val registry get() = configuration.registry
    private val audioRouter get() = configuration.audioRouter
    private val observers get() = configuration.callObservers
    private val dispatcher get() = configuration.actionDispatcher
    private var sipProfile: SipProfile? = null

    actual override suspend fun configureSipAccount(profile: SipProfile): CallResult<Unit> {
        sipProfile = profile
        return CallResult.Success(Unit)
    }

    actual override suspend fun startOutgoingCall(
        number: String,
        displayName: String,
        scheme: String
    ): CallResult<Call> = runCatching {
        val uuid = NSUUID()
        val call = newCall(uuid, number, displayName, scheme)
        registry.register(call, uuid)
        observers.forEach { it.onCallAdded(call) }
        dispatcher.dispatch(startTransaction(uuid, number, displayName, scheme))
        call
    }.toCallResult()

    actual override suspend fun endCall(callId: String): CallResult<Unit> = runCatching {
        val uuid = registry.uuidFor(callId) ?: throw CallError.CallNotFound(callId)
        dispatcher.dispatch(CXTransaction(action = CXEndCallAction(callUUID = uuid)))
    }.toCallResult()

    actual override suspend fun muteCall(callId: String, muted: Boolean): CallResult<Unit> = runCatching {
        val uuid = registry.uuidFor(callId) ?: throw CallError.CallNotFound(callId)
        dispatcher.dispatch(CXTransaction(action = CXSetMutedCallAction(callUUID = uuid, muted = muted)))
    }.toCallResult()

    actual override suspend fun holdCall(callId: String, onHold: Boolean): CallResult<Unit> = runCatching {
        val uuid = registry.uuidFor(callId) ?: throw CallError.CallNotFound(callId)
        dispatcher.dispatch(CXTransaction(action = CXSetHeldCallAction(callUUID = uuid, onHold = onHold)))
    }.toCallResult()

    actual override suspend fun getCallState(callId: String): CallResult<CallState> {
        val call = registry.findById(callId) ?: return CallResult.Error(CallError.CallNotFound(callId))
        return CallResult.Success(call.state)
    }

    actual override suspend fun getActiveCalls(): CallResult<List<Call>> =
        CallResult.Success(registry.allActive())

    actual override suspend fun setAudioRoute(route: AudioRoute): CallResult<Unit> {
        val routed = audioRouter.routeTo(route)
        return if (routed) CallResult.Success(Unit)
        else CallResult.Error(CallError.AudioDeviceError("Cannot route audio to $route"))
    }

    actual override suspend fun getCurrentAudioRoute(): CallResult<AudioRoute> =
        CallResult.Success(audioRouter.currentRoute())

    actual override suspend fun checkPermissions(): CallResult<Unit> =
        CallResult.Success(Unit)

    actual override fun registerCallStateCallback(callback: CallStateCallback) {
        observers.add(callback)
    }

    actual override fun unregisterCallStateCallback(callback: CallStateCallback) {
        observers.remove(callback)
    }

    private fun newCall(uuid: NSUUID, number: String, displayName: String, scheme: String = "tel") = Call(
        id = uuid.UUIDString,
        number = number,
        displayName = displayName,
        state = CallState.DIALING,
        createdAt = time(null),
        scheme = scheme
    )

    private fun startTransaction(uuid: NSUUID, number: String, displayName: String, scheme: String): CXTransaction {
        val handleType = if (scheme == "sip") CXHandleTypeGeneric else CXHandleTypePhoneNumber
        val handleValue = if (scheme == "sip") "sip:$number" else number
        val handle = CXHandle(type = handleType, value = handleValue)
        val action = CXStartCallAction(callUUID = uuid, handle = handle).apply {
            contactIdentifier = displayName
        }
        return CXTransaction(action = action)
    }
}

private fun <T> Result<T>.toCallResult(): CallResult<T> = fold(
    onSuccess = { CallResult.Success(it) },
    onFailure = { error ->
        when (error) {
            is CallError -> CallResult.Error(error)
            else -> CallResult.Error(CallError.ServiceError(error.message ?: "Unexpected error", -1))
        }
    }
)
