package co.fusionx.relay.internal

import co.fusionx.relay.*
import rx.Observable
import rx.subjects.BehaviorSubject

internal class UserImpl(initialNick: String,
    eventStream: Observable<Event>) : User {

    public override val nick: Observable<String>
    internal override val channels: MutableSet<Channel> = hashSetOf()

    init {
        val behaviourNick = BehaviorSubject.create(initialNick)
        nick = behaviourNick

        eventStream.ofType(javaClass<NickEvent>())
            .filter { it.user == this }
            .map { it.newNick }
            .subscribe { behaviourNick.onNext(it) }

        eventStream.ofType(javaClass<JoinEvent>())
            .filter { it.user == this }
            .subscribe { channels.add(it.channel) }

        eventStream.ofType(javaClass<PartEvent>())
            .filter { it.user == this }
            .subscribe { channels.remove(it.channel) }

        eventStream.ofType(javaClass<QuitEvent>())
            .filter { it.user == this }
            .subscribe { channels.clear() }
    }

    override fun toString(): String = "DefaultUser(nick=${nick.toBlocking().first()})"
}