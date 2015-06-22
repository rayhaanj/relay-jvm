package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import co.fusionx.relay.internal.*
import rx.Observable
import rx.subjects.PublishSubject

internal class CoreCommandParser private constructor(private val eventStream: Observable<Event>,
                                                     private val outputStream: PublishSubject<Message>,
                                                     override val channelTracker: ChannelTracker,
                                                     override val userTracker: UserTracker) : EventParser<CommandMessage> {

    override fun parse(message: CommandMessage): Observable<Event> = when (message.command) {
        "JOIN" -> onJoin(message)
        "NICK" -> onNick(message)
        "PRIVMSG" -> onPrivmsg(message)
        "NOTICE" -> onNotice(message)
        "CAP" -> onCap(message)
        "PING" -> onPing(message)
        "PART" -> onPart(message)
        "QUIT" -> onQuit(message)
        else -> Observable.empty()
    }

    private fun onCap(message: CommandMessage): Observable<Event> {
        /* TODO - return an error here */
        val subCommand = CapType.parse(message.arguments[1]) ?: return Observable.empty()

        /* We need to check if we have a multi-line cap here */
        val capsIndex = if (message.arguments[2] == "*") 3 else 2
        val caps = message.arguments[capsIndex]
            .split(' ')
            .map { Capability.parse(it) }
            .filterNotNull()

        return Observable.just(CapEvent(subCommand, caps))
    }

    private fun onPrivmsg(message: CommandMessage): Observable<Event> = onMessage(
        message,
        { c, s, n, t -> ChannelPrivmsgEvent(c, s, n, t) },
        { s, n, t -> ServerPrivmsgEvent(s, n, t) }
    )

    private fun onNotice(message: CommandMessage): Observable<Event> = onMessage(
        message,
        { c, s, n, t -> ChannelNoticeEvent(c, s, n, t) },
        { s, n, t -> ServerNoticeEvent(s, n, t) }
    )

    private fun onJoin(message: CommandMessage): Observable<Event> {
        /* TODO - return an error here */
        val prefix = message.prefix ?: return Observable.empty()

        val nick = prefix.serverNameOrNick
        val user = userTracker.user(nick) ?: UserImpl(nick, eventStream)

        /* Parse the arguments */
        val channelName = message.arguments[0]

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

    private fun onNick(message: CommandMessage): Observable<Event> {
        /* TODO - return an error here */
        val prefix = message.prefix ?: return Observable.empty()
        val oldNick = prefix.serverNameOrNick

        /* TODO - return an error here */
        val user = userTracker.user(oldNick) ?: return Observable.empty()

        /* Parse the arguments */
        val newNick = message.arguments[0]

        return Observable.from(user.channels)
            .map<Event> { ChannelNickEvent(it, user, oldNick, newNick) }
            .startWith(NickEvent(user, oldNick, newNick))
    }

    private fun onPing(message: CommandMessage): Observable<Event> {
        val server = message.arguments[0]
        return Observable.just(PingEvent(server))
    }

    private fun onQuit(message: CommandMessage): Observable<Event> {
        /* TODO - return an error here */
        val prefix = message.prefix ?: return Observable.empty()
        val nick = prefix.serverNameOrNick

        /* TODO - return an error here */
        val user = userTracker.user(nick) ?: return Observable.empty()

        /* Parse the arguments */
        val reason = message.arguments.getOrNull(0)

        return Observable.from(user.channels)
            .map<Event> { ChannelQuitEvent(it, user, reason) }
            .concatWith(Observable.just(QuitEvent(user, reason)))
    }

    private fun onPart(message: CommandMessage): Observable<Event> {
        /* TODO - return an error here */
        val prefix = message.prefix ?: return Observable.empty()
        val nick = prefix.serverNameOrNick

        /* TODO - return an error here */
        val user = userTracker.user(nick) ?: return Observable.empty()

        /* Parse the arguments */
        val channelName = message.arguments[0]
        val reason = message.arguments[1]

        val trackerChannel = channelTracker.channel(channelName)
        val channel: Channel

        /* Figure out who the message refers to and act as appropriate */
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
        return Observable.just(PartEvent(channel, user, reason))
    }

    private fun onMessage(message: CommandMessage,
                          channelProducer: (Channel, User?, String, String) -> Event,
                          serverProducer: (User?, String, String) -> Event): Observable<Event> {
        /* TODO - return an error here */
        val prefix = message.prefix ?: return Observable.empty()

        /* Get the sender of the message */
        val nick = prefix.serverNameOrNick
        val sender = userTracker.user(nick)

        /* Parse the arguments */
        val target = message.arguments[0]
        val text = message.arguments[1]

        if (target.isChannel()) {
            /* TODO - return an error here */
            val channel = channelTracker.channel(target) ?: return Observable.empty()
            return Observable.just(channelProducer(channel, sender, nick, text))
        }
        return Observable.just(serverProducer(sender, nick, text))
    }

    companion object {
        fun create(eventStream: Observable<Event>,
                   outputStream: PublishSubject<Message>,
                   channelTracker: ChannelTracker,
                   userTracker: UserTracker): CoreCommandParser =
            CoreCommandParser(eventStream, outputStream, channelTracker, userTracker)
    }
}