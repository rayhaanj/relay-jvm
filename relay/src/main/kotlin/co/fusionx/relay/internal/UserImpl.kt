package co.fusionx.relay.internal

import co.fusionx.relay.*
import rx.Observable
import rx.subjects.BehaviorSubject
import java.util.HashSet

internal class UserImpl(initialNick: String,
                        eventStream: Observable<Event>) : User {

    override val nick: Observable<String>
    override val channels: Set<Channel>
        get() = synchronized(channelSet) { HashSet(channelSet) }

    private val channelSet: MutableSet<Channel> = hashSetOf()

    init {
        val behaviourNick = BehaviorSubject.create(initialNick)
        nick = behaviourNick

        eventStream.ofType(javaClass<NickEvent>())
            .filter { it.user == this }
            .map { it.newNick }
            .subscribe { behaviourNick.onNext(it) }

        eventStream.ofType(javaClass<JoinEvent>())
            .filter { it.user == this }
            .subscribe { synchronized(channelSet) { channelSet.add(it.channel) } }

        eventStream.ofType(javaClass<PartEvent>())
            .filter { it.user == this }
            .subscribe { synchronized(channelSet) { channelSet.remove(it.channel) } }

        eventStream.ofType(javaClass<QuitEvent>())
            .filter { it.user == this }
            .subscribe { synchronized(channelSet) { channelSet.clear() } }
    }

    override fun toString(): String = "DefaultUser(nick=${nick.toBlocking().first()})"
}