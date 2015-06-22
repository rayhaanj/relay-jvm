package co.fusionx.relay.internal.protocol.ext

import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import co.fusionx.relay.internal.ChannelImpl
import co.fusionx.relay.internal.UserImpl
import rx.Observable
import rx.subjects.PublishSubject

class ExtendedJoinParser(private val eventStream: Observable<Event>,
                         private val outputStream: PublishSubject<Message>,
                         override val channelTracker: ChannelTracker,
                         override val userTracker: UserTracker) : CommandExtParser {

    override val capability: String = "extended-join"
    override val command: String = "JOIN"

    override fun parse(message: CommandMessage): Observable<Event> {
        /* TODO - return an error here */
        val prefix = message.prefix ?: return Observable.empty()

        val nick = prefix.serverNameOrNick
        val user = userTracker.user(nick) ?: UserImpl(nick, eventStream)

        /* Parse the arguments */
        val channelName = message.arguments[0]
        val accountName = message.arguments[1]
        val realName = message.arguments[2]

        /* This is another user */
        val trackerChannel = channelTracker.channel(channelName)
        val channel: Channel

        if (user == userTracker.self) {
            /* This is us - we need to create a new channel for sure if we are getting this */
            /* TODO - assert that channel is not in channelTracker */
            channel = ChannelImpl(channelName, eventStream, outputStream)
        } else if (trackerChannel == null) {
            /* TODO - return an error here */
            return Observable.empty()
        } else {
            channel = trackerChannel
        }
        return Observable.just(JoinEvent(channel, user))
    }
}