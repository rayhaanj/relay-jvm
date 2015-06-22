package co.fusionx.relay.parser

import co.fusionx.relay.EventListener
import co.fusionx.relay.protocol.ReplyCodes
import co.fusionx.relay.util.StringUtils
import co.fusionx.relay.util.isChannel

class CoreCodeParser(private val listener: EventListener) : CodeParser {

    override fun parseCode(tags: List<String>?, prefix: String?, code: Int, target: String,
            arguments: List<String>) {
        when (code) {
            ReplyCodes.RPL_WELCOME -> onWelcome(code, target, arguments)
            ReplyCodes.RPL_NAMES -> onNames(arguments)
        }
    }

    fun onWelcome(code: Int, target: String, arguments: List<String>) {
        listener.onWelcome(code, arguments[0])
    }

    fun onNames(arguments: List<String>) {
        /* RFC1459 and RFC2812 vary here. We try to account for both cases. */
        val firstArg = arguments[0]
        val offset = if (firstArg.isChannel()) 0 else 1

        val channelName = arguments[offset]
        val nicks = arguments[1 + offset]

        val nickList = StringUtils.tokenizeOn(nicks, ' ', true, false)
        listener.onNames(channelName, nickList)
    }
}