package co.fusionx.relay.internal.protocol.ext

import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.ChannelTracker
import co.fusionx.relay.Event
import co.fusionx.relay.Session
import co.fusionx.relay.UserTracker
import rx.Observable
import rx.subjects.PublishSubject

class AccountNotifyParser(private val eventStream: Observable<Event>,
                          private val outputStream: PublishSubject<Message>,
                          override val channelTracker: ChannelTracker,
                          override val userTracker: UserTracker) : CommandExtParser {

    override val capability: String = "account-notify"
    override val command: String = "ACCOUNT"

    override fun parse(message: CommandMessage): Observable<Event> {
        val account = message.arguments[0]
        return Observable.empty()
    }
}