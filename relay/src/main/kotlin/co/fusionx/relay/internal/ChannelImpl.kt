package co.fusionx.relay.internal

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable
import rx.subjects.PublishSubject

class ChannelImpl(override val name: String,
                  globalEventSource: Observable<Event>,
                  val outputSink: PublishSubject<Message>) : Channel {

    override val eventSource: Observable<ChannelEvent>

    private val userMap: MutableMap<User, List<UserLevel>> = hashMapOf()

    init {
        eventSource = globalEventSource.ofType(javaClass<ChannelEvent>())
            .filter { it.channel == this }
            .share()

        eventSource.ofType(javaClass<JoinEvent>())
            .subscribe { userMap[it.user] = listOf() }

        eventSource.ofType(javaClass<ChannelNamesReplyEvent>())
            .concatMap { Observable.from(it.levelledUsers) }
            .subscribe { userMap[it.user] = it.levels }
    }

    public override fun toString(): String = name

    /* Methods to send messages to the server */
    override fun privmsg(message: String) =
        outputSink.onNext(ClientMessageGenerator.privmsg(name, message))
}