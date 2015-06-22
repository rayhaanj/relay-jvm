package co.fusionx.relay.internal.protocol.ext

import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.ChannelTracker
import co.fusionx.relay.Event
import co.fusionx.relay.Session
import co.fusionx.relay.UserTracker
import co.fusionx.relay.internal.getOrNull
import rx.Observable
import rx.subjects.PublishSubject

class AwayNotifyParser(private val eventStream: Observable<Event>,
                       private val outputStream: PublishSubject<Message>,
                       override val channelTracker: ChannelTracker,
                       override val userTracker: UserTracker) : CommandExtParser {

    override val capability = "away-notify"
    override val command = "AWAY"

    override fun parse(message: CommandMessage): Observable<Event> {
        val awayMessage = message.arguments.getOrNull(0)
        return Observable.empty()
    }
}