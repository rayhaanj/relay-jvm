package co.fusionx.relay.internal.parser.ext

import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import co.fusionx.relay.internal.ChannelImpl
import co.fusionx.relay.internal.UserImpl
import rx.Observable
import rx.subjects.PublishSubject

class ExtendedJoinParser(private val session: Session,
                         private val eventSource: Observable<Event>,
                         private val outputSink: PublishSubject<Message>,
                         override val channelTracker: ChannelTracker,
                         override val userTracker: UserTracker) : CommandExtParser {

    private val capability: String = "extended-join"
    private val command: String = "JOIN"

    override fun parse(message: CommandMessage): Observable<Event> {
        /* TODO - return an error here */
        val prefix = message.prefix ?: return Observable.empty()

        val nick = prefix.serverNameOrNick
        val user = userTracker.user(nick) ?: UserImpl(nick, eventSource)

        /* Parse the arguments */
        val (channelName, accountName, realName) = message.arguments

        /* This is another user */
        val trackerChannel = channelTracker.channel(channelName)
        val channel: Channel

        if (user == userTracker.self) {
            /* This is us - we need to create a new channel for sure if we are getting this */
            /* TODO - assert that channel is not in channelTracker */
            channel = ChannelImpl(channelName, eventSource, outputSink)
        } else if (trackerChannel == null) {
            /* TODO - return an error here */
            return Observable.empty()
        } else {
            channel = trackerChannel
        }
        return Observable.just(JoinEvent(channel, user))
    }

    override fun canParse(message: CommandMessage): Boolean {
        return message.command == command && session.capabilities.contains(capability)
    }
}