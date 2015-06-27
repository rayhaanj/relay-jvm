package co.fusionx.relay.internal.parser.ext

import co.fusionx.irc.message.CommandMessage
import co.fusionx.relay.ChannelTracker
import co.fusionx.relay.Event
import co.fusionx.relay.Session
import co.fusionx.relay.UserTracker
import rx.Observable

class AccountNotifyParser(private val session: Session,
                          override val channelTracker: ChannelTracker,
                          override val userTracker: UserTracker) : CommandExtParser {

    private val capability: String = "account-notify"
    private val command: String = "ACCOUNT"

    override fun parse(message: CommandMessage): Observable<Event> {
        val account = message.arguments[0]
        return Observable.empty()
    }

    override fun canParse(message: CommandMessage): Boolean =
        message.command == command && session.capabilities.contains(capability)
}