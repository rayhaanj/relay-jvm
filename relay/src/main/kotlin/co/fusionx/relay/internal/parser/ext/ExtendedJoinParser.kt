package co.fusionx.relay.internal.parser.ext

import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable
import rx.subjects.PublishSubject

class ExtendedJoinParser(private val creationHooks: AtomCreationHooks,
                         private val session: Session,
                         private val eventSource: Observable<Event>,
                         private val outputSink: PublishSubject<Message>,
                         override val channelTracker: ChannelTracker,
                         override val userTracker: UserTracker) : CommandExtParser {

    private val capability: String = "extended-join"
    private val command: String = "JOIN"

    override fun parse(message: CommandMessage): Observable<Event> {
        /* Parse the arguments */
        val nick = message.prefix?.serverNameOrNick ?: return prefixMissing()
        val (channelName) = message.arguments

        /* Get the user and the channel */
        val user = userTracker.user(nick) ?: creationHooks.user(nick, eventSource)
        var channel = channelTracker.channel(channelName)

        if (user == userTracker.self) {
            /* TODO - return an error here */
            if (channel != null) return Observable.empty()

            /* This is us - we need to create a new channel for sure if we are getting this */
            channel = creationHooks.channel(channelName, eventSource, outputSink)
        } else if (channel == null) return channelMissing()

        return Observable.just(JoinEvent(channel, user))
    }

    /* TODO - return an error here */
    private fun channelMissing(): Observable<Event> = Observable.empty()

    private fun prefixMissing(): Observable<Event> = Observable.empty()

    override fun canParse(message: CommandMessage): Boolean {
        return message.command == command && session.capabilities.contains(capability)
    }
}