package co.fusionx.relay.internal

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable
import rx.subjects.PublishSubject

class ChannelImpl(override val name: String,
                  rawEventStream: Observable<Event>,
                  val outputStream: PublishSubject<Message>) : Channel {

    override val eventStream: Observable<ChannelEvent>

    private val userMap: MutableMap<User, List<UserLevel>> = hashMapOf()

    init {
        /* Generate our channel specific event stream */
        eventStream = rawEventStream.ofType(javaClass<ChannelEvent>())
            .filter { it.channel == this }
            .share()

        /* Setup subscription for new users joining the channel */
        eventStream.ofType(javaClass<JoinEvent>())
            .subscribe { userMap[it.user] = listOf() }

        /* Setup subscription for the upcoming names event */
        eventStream.ofType(javaClass<ChannelNamesReplyEvent>())
            .concatMap { Observable.from(it.levelledUsers) }
            .subscribe { userMap[it.user] = it.levels }
    }

    public override fun toString(): String = name

    /* Methods to send messages to the server */
    override fun privmsg(message: String) =
        outputStream.onNext(ClientMessageGenerator.privmsg(name, message))
}