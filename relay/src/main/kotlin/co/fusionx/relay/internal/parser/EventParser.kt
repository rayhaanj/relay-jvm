package co.fusionx.relay.internal.parser

import co.fusionx.irc.Prefix
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable

public interface EventParser<T : co.fusionx.irc.message.Message> {
    val channelTracker: co.fusionx.relay.ChannelTracker
    val userTracker: co.fusionx.relay.UserTracker

    fun parse(message: T): rx.Observable<co.fusionx.relay.Event>
}