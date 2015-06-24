package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.Message
import co.fusionx.relay.ChannelTracker
import co.fusionx.relay.Event
import co.fusionx.relay.UserTracker

public interface EventParser<T : Message> {
    val channelTracker: ChannelTracker
    val userTracker: UserTracker

    fun parse(message: T): rx.Observable<Event>
}