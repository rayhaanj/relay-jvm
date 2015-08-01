package co.fusionx.relay.internal.parser.ext

import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.ExecutorService

class ExtendedJoinParser(private val creationHooks: AtomCreationHooks,
                         private val session: Session,
                         private val eventSource: Observable<Event>,
                         private val outputSink: PublishSubject<Message>,
                         private val mainExecutor: ExecutorService,
                         override val channelTracker: ChannelTracker,
                         override val userTracker: UserTracker) : CommandExtParser {

    override val capabilities = setOf("extended-join")
    private val command: String = "JOIN"

    override fun parse(message: CommandMessage): Observable<Event> {
        val nick = message.prefix?.serverNameOrNick ?: return prefixMissing()
        val (channelName) = message.arguments

        val user = userTracker.user(nick) ?: creationHooks.user(nick, eventSource)
        var channel = channelTracker.channel(channelName)

        if (user == userTracker.self) {
            /* TODO - return an error here */
            if (channel != null) return Observable.empty()

            channel = creationHooks.channel(channelName, eventSource, outputSink, mainExecutor)
        } else if (channel == null) return channelMissing()

        return Observable.just(JoinEvent(channel, user))
    }

    /* TODO - return an error here */
    private fun channelMissing(): Observable<Event> = Observable.empty()

    private fun prefixMissing(): Observable<Event> = Observable.empty()

    override fun canParse(message: CommandMessage): Boolean {
        return message.command == command && session.capabilities.intersect(capabilities).isNotEmpty()
    }
}