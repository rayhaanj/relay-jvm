package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CodeMessage
import co.fusionx.relay.*
import co.fusionx.relay.internal.isChannel
import co.fusionx.relay.internal.protocol.LevelledNick
import co.fusionx.relay.internal.protocol.ReplyCodes
import co.fusionx.relay.rx.filterNotNull
import rx.Observable

class CoreCodeParser private constructor(private val creationHooks: AtomCreationHooks,
                                         private val eventSource: Observable<Event>,
                                         override val channelTracker: ChannelTracker,
                                         override val userTracker: UserTracker) : EventParser<CodeMessage> {

    override fun parse(message: CodeMessage): Observable<Event> = when (message.code) {
        ReplyCodes.RPL_WELCOME -> onWelcome(message)
        ReplyCodes.RPL_ISUPPORT -> onISupport(message)
        ReplyCodes.RPL_NAMES -> onNames(message)
        else -> Observable.empty()
    }

    fun onWelcome(message: CodeMessage): Observable<Event> {
        val (text) = message.arguments

        /* We concatMap here even though we are actually guaranteed to get back only one value */
        return userTracker.self.nick.first()
            .concatMap {
                Observable.just(
                    NickEvent(userTracker.self, it, message.target),
                    ServerGenericCodeEvent(message.code, text),
                    StatusEvent(Status.CONNECTED)
                )
            }
    }

    // TODO(tilal6991) implement ISUPPORT
    private fun onISupport(message: CodeMessage): Observable<Event> = Observable.empty()

    fun onNames(message: CodeMessage): Observable<Event> {
        /* RFC1459 and RFC2812 vary here. We try to account for both cases. */
        val (firstArg) = message.arguments
        val offset = if (firstArg.isChannel()) 0 else 1

        val channelName = message.arguments[offset]
        val nicks = message.arguments[1 + offset]

        val nickList = nicks.split(' ')

        /* TODO - maybe return an error here? */
        val channel = channelTracker.channel(channelName) ?: return Observable.empty()

        return Observable.from(nickList)
            .map { LevelledNick.parse(it) }
            .filterNotNull()
            .map { LevelledUser(it.level, userTracker.user(it.nick) ?: creationHooks.user(it.nick, eventSource)) }
            .toList()
            .map { ChannelNamesReplyEvent(channel, it) }
    }

    companion object {
        fun create(creationHooks: AtomCreationHooks,
                   eventSource: Observable<Event>,
                   channelTracker: ChannelTracker,
                   userTracker: UserTracker): CoreCodeParser =
            CoreCodeParser(creationHooks, eventSource, channelTracker, userTracker)
    }
}