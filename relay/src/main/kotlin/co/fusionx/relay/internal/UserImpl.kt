package co.fusionx.relay.internal

import co.fusionx.relay.*
import rx.Observable
import rx.subjects.BehaviorSubject

internal class UserImpl(initialNick: String,
                        eventSource: Observable<Event>) : User {

    override val nick: BehaviorSubject<String> = BehaviorSubject.create(initialNick)
    override val channels: MutableSet<Channel> = hashSetOf()

    init {
        val localEventSource = eventSource.ofType(javaClass<UserEvent>())
            .filter { it.user == this }
            .share()

        localEventSource.ofType(javaClass<NickEvent>())
            .map { it.newNick }
            .subscribe { nick.onNext(it) }

        localEventSource.ofType(javaClass<JoinEvent>())
            .subscribe { channels.add(it.channel) }

        localEventSource.ofType(javaClass<PartEvent>())
            .subscribe { channels.remove(it.channel) }

        localEventSource.ofType(javaClass<QuitEvent>())
            .subscribe { channels.clear() }
    }

    override fun toString(): String = "UserImpl(nick=${nick.toBlocking().first()})"
}