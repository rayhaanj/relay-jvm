package co.fusionx.relay.internal

import co.fusionx.relay.*
import rx.Observable
import rx.subjects.BehaviorSubject

internal class UserImpl(initialNick: String,
                        eventSource: Observable<Event>) : User {

    public override val nick: Observable<String>
    internal override val channels: MutableSet<Channel> = hashSetOf()

    init {
        val behaviourNick = BehaviorSubject.create(initialNick)
        nick = behaviourNick

        eventSource.ofType(javaClass<NickEvent>())
            .filter { it.user == this }
            .map { it.newNick }
            .subscribe { behaviourNick.onNext(it) }

        eventSource.ofType(javaClass<JoinEvent>())
            .filter { it.user == this }
            .subscribe { channels.add(it.channel) }

        eventSource.ofType(javaClass<PartEvent>())
            .filter { it.user == this }
            .subscribe { channels.remove(it.channel) }

        eventSource.ofType(javaClass<QuitEvent>())
            .filter { it.user == this }
            .subscribe { channels.clear() }
    }

    override fun toString(): String = "DefaultUser(nick=${nick.toBlocking().first()})"
}