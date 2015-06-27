package co.fusionx.relay.internal

import co.fusionx.relay.Channel
import co.fusionx.relay.ChannelTracker
import co.fusionx.relay.Event
import co.fusionx.relay.JoinEvent
import rx.Observable
import java.util.HashMap

internal class ChannelTrackerImpl(private val eventSource: Observable<Event>) : ChannelTracker {

    private val channelMap: MutableMap<String, Channel> = HashMap()

    init {
        eventSource.ofType(javaClass<JoinEvent>())
            .subscribe { channelMap[it.channel.name] = it.channel }
    }

    override internal fun channel(channelName: String): Channel? = channelMap[channelName]
}