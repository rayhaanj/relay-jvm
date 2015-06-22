package co.fusionx.relay.internal.protocol

import co.fusionx.irc.Prefix
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable

public interface EventParser<T : Message> {
    val channelTracker: ChannelTracker
    val userTracker: UserTracker

    fun parse(message: T): Observable<Event>
}