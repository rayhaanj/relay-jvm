package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CodeMessage
import co.fusionx.relay.*
import co.fusionx.relay.internal.UserImpl
import co.fusionx.relay.internal.protocol.LevelledNick
import co.fusionx.relay.internal.protocol.ReplyCodes
import rx.Observable

class CoreCodeParser private constructor(private val eventStream: Observable<Event>,
                                         override val channelTracker: ChannelTracker,
                                         override val userTracker: UserTracker) : EventParser<co.fusionx.irc.message.CodeMessage> {

    override fun parse(message: CodeMessage): Observable<Event> = when (message.code) {
        ReplyCodes.RPL_WELCOME -> onWelcome(message)
        ReplyCodes.RPL_ISUPPORT -> onISupport(message)
        353 -> onNames(message)
        else -> Observable.empty()
    }

    fun onWelcome(message: CodeMessage): Observable<Event> {
        val text = message.arguments[0]

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

    private fun onISupport(message: CodeMessage): Observable<Event> {
        return Observable.empty()
    }

    fun onNames(message: CodeMessage): Observable<Event> {
        /* Parse the arguments */
        val channelName = message.arguments[1]
        val nicks = message.arguments[2]

        /* TODO - maybe return an error here? */
        val channel = channelTracker.channel(channelName) ?: return Observable.empty()

        /* For a names reply */
        return Observable.from(nicks.split(' '))
            .concatMap {
                val levelledNick = LevelledNick.parse(it)
                if (levelledNick == null) Observable.empty() else Observable.just(levelledNick)
            }
            /* ...we need to convert each of the nicks in the names to a user or create one... */
            .map { LevelledUser(it.level, userTracker.user(it.nick) ?: UserImpl(it.nick, eventStream)) }
            /* ...collect them up... */
            .toList()
            /* ...and emit an event. */
            .map { ChannelNamesReplyEvent(channel, it) }
    }

    companion object {
        fun create(eventStream: Observable<Event>,
                   channelTracker: ChannelTracker,
                   userTracker: UserTracker): CoreCodeParser = CoreCodeParser(eventStream, channelTracker, userTracker)
    }
}