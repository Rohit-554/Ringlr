package io.jadu.ringlr.call

/**
 * Credentials and server details needed to register a SIP account.
 *
 * Pass to [CallManagerInterface.configureSipAccount] before placing
 * any call with [scheme] = "sip".
 *
 * [sipAddress] produces the canonical "username@server" form.
 * [sipUri]     produces the full "sip:username@server:port" URI.
 */
data class SipProfile(
    val username: String,
    val server: String,
    val password: String,
    val port: Int = DEFAULT_SIP_PORT,
    val displayName: String = username
) {
    val sipAddress: String get() = "$username@$server"
    val sipUri: String get() = "sip:$sipAddress:$port"

    companion object {
        const val DEFAULT_SIP_PORT = 5060
    }
}
