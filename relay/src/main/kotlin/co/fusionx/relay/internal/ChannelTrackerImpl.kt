package co.fusionx.relay.internal

import co.fusionx.relay.*
import rx.Observable
import java.util.HashMap

internal class ChannelTrackerImpl(val eventStream: Observable<Event>) : ChannelTracker {

    private val channelMap: MutableMap<String, Channel> = HashMap()

    override val channels: Observable<Channel>
        get() = Observable.defer {
            Observable.create<Channel> { subs ->
                synchronized (channelMap) { channelMap.values().forEach { subs.onNext(it) } }
                subs.onCompleted()
            }
        }

    init {
        eventStream.ofType(javaClass<JoinEvent>())
            .subscribe { synchronized(channelMap) { channelMap[it.channel.name] = it.channel } }
    }

    override internal fun channel(channelName: String): Channel? = synchronized(channelMap) { channelMap[channelName] }
}