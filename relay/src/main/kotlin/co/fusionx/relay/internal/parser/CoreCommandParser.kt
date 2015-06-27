package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CommandMessage
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import co.fusionx.relay.internal.*
import co.fusionx.relay.internal.protocol.Commands
import rx.Observable
import rx.subjects.PublishSubject

internal class CoreCommandParser private constructor(private val eventSource: Observable<Event>,
                                                     private val outputSink: PublishSubject<Message>,
                                                     override val channelTracker: ChannelTracker,
                                                     override val userTracker: UserTracker) : EventParser<CommandMessage> {

    override fun parse(message: CommandMessage): Observable<Event> = when (message.command) {
        Commands.JOIN -> onJoin(message)
        Commands.NICK -> onNick(message)
        Commands.PRIVMSG -> onPrivmsg(message)
        Commands.NOTICE -> onNotice(message)
        Commands.CAP -> onCap(message)
        Commands.PING -> onPing(message)
        Commands.PART -> onPart(message)
        Commands.QUIT -> onQuit(message)
        else -> Observable.empty()
    }

    private fun onCap(message: CommandMessage): Observable<Event> {
        val (_, subCommandString, third) = message.arguments

        /* TODO - return an error here */
        val subCommand = CapType.parse(subCommandString) ?: return Observable.empty()

        /* We need to check if we have a multi-line cap here */
        val capsIndex = if (third == "*") 3 else 2
        val caps = message.arguments[capsIndex]
            .split(' ')
            .map { Capability.parse(it) }
            .filterNotNull()

        return Observable.just(CapEvent(subCommand, caps))
    }

    private fun onJoin(message: CommandMessage): Observable<Event> {
        /* Parse the arguments */
        val nick = message.prefix?.serverNameOrNick ?: return prefixMissing()
        val (channelName) = message.arguments

        /* Get the user and the channel */
        val user = userTracker.user(nick) ?: UserImpl(nick, eventSource)
        var channel = channelTracker.channel(channelName)

        if (user == userTracker.self) {
            /* TODO - return an error here */
            if (channel != null) return Observable.empty()

            /* This is us - we need to create a new channel for sure if we are getting this */
            channel = ChannelImpl(channelName, eventSource, outputSink)
        } else if (channel == null) return channelMissing()

        return Observable.just(JoinEvent(channel, user))
    }

    private fun onNick(message: CommandMessage): Observable<Event> {
        /* Parse the arguments */
        val oldNick = message.prefix?.serverNameOrNick ?: return prefixMissing()
        val (newNick) = message.arguments

        /* Get the user in the tracker */
        val user = userTracker.user(oldNick) ?: return userMissing()

        return Observable.from(user.channels)
            .map<Event> { ChannelNickEvent(it, user, oldNick, newNick) }
            .startWith(NickEvent(user, oldNick, newNick))
    }

    private fun onPing(message: CommandMessage): Observable<Event> {
        val (server) = message.arguments
        return Observable.just(PingEvent(server))
    }

    private fun onQuit(message: CommandMessage): Observable<Event> {
        /* Parse the arguments */
        val nick = message.prefix?.serverNameOrNick ?: return prefixMissing()
        val user = userTracker.user(nick) ?: return userMissing()

        /* Parse the arguments */
        val reason = message.arguments.getOrNull(0)

        return Observable.from(user.channels)
            .map<Event> { ChannelQuitEvent(it, user, reason) }
            .concatWith(Observable.just(QuitEvent(user, reason)))
    }

    private fun onPart(message: CommandMessage): Observable<Event> {
        /* Parse the arguments */
        val nick = message.prefix?.serverNameOrNick ?: return prefixMissing()
        val (channelName) = message.arguments
        val reason = message.arguments.getOrNull(1)

        /* Get the user and the channel */
        val user = userTracker.user(nick) ?: return userMissing()
        val channel = channelTracker.channel(channelName) ?: return channelMissing()

        return Observable.just(PartEvent(channel, user, reason))
    }

    private fun onPrivmsg(message: CommandMessage): Observable<Event> = onMessage(message)

    private fun onNotice(message: CommandMessage): Observable<Event> = onMessage(message)

    private fun onMessage(message: CommandMessage): Observable<Event> {
        /* Parse the arguments */
        val nick = message.prefix?.serverNameOrNick ?: return prefixMissing()
        val (target, text) = message.arguments

        /* Get the sender of the message */
        val sender = userTracker.user(nick)

        if (target.isChannel()) {
            val channel = channelTracker.channel(target) ?: return channelMissing()
            return Observable.just(ChannelPrivmsgEvent(channel, sender, nick, text))
        }
        return Observable.just(ServerPrivmsgEvent(sender, nick, text))
    }

    /* TODO - return an error here */
    private fun channelMissing(): Observable<Event> = Observable.empty()

    private fun prefixMissing(): Observable<Event> = Observable.empty()

    private fun userMissing(): Observable<Event> = Observable.empty()

    companion object {
        fun create(eventSource: Observable<Event>,
                   outputSink: PublishSubject<Message>,
                   channelTracker: ChannelTracker,
                   userTracker: UserTracker): CoreCommandParser =
            CoreCommandParser(eventSource, outputSink, channelTracker, userTracker)
    }
}