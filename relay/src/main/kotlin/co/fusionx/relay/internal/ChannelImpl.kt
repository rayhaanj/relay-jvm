package co.fusionx.relay.internal

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable
import rx.Single
import rx.subjects.PublishSubject
import java.util.concurrent.ExecutorService
import kotlin.concurrent.invoke

class ChannelImpl(override val name: String,
                  globalEventSource: Observable<Event>,
                  private val outputSink: PublishSubject<Message>,
                  private val mainExecutor: ExecutorService) : Channel {

    override val eventSource: Observable<ChannelEvent>
    override val users: Single<Map<User, List<UserLevel>>>
        get() = Single.create { s -> mainExecutor.execute { s.onSuccess(userMap) } }

    private val userMap: MutableMap<User, List<UserLevel>> = hashMapOf()

    init {
        eventSource = globalEventSource.ofType(javaClass<ChannelEvent>())
            .filter { it.channel == this }
            .share()

        eventSource.ofType(javaClass<JoinEvent>())
            .subscribe { userMap[it.user] = listOf() }

        eventSource.ofType(javaClass<ChannelNamesReplyEvent>())
            .concatMap {
                Observable.from(it.levelledUsers)
            }
            .subscribe {
                userMap[it.user] = it.levels
            }
    }

    public override fun toString(): String = name

    /* Methods to send messages to the server */
    override fun privmsg(message: String) =
        outputSink.onNext(ClientMessageGenerator.privmsg(name, message))
}