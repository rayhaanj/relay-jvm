package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CodeMessage
import co.fusionx.relay.*
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
        353 -> onNames(message)
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

    private fun onISupport(message: CodeMessage): Observable<Event> {
        return Observable.empty()
    }

    fun onNames(message: CodeMessage): Observable<Event> {
        /* Parse the arguments */
        val (_, channelName, nicks) = message.arguments
        val nickList = nicks.split(' ')

        /* TODO - maybe return an error here? */
        val channel = channelTracker.channel(channelName) ?: return Observable.empty()

        /* For a names reply */
        return Observable.from(nickList)
            /* Get the levelled nick out */
            .map { LevelledNick.parse(it) }
            /* Filter out the nulls */
            .filterNotNull()
            /* ...we need to convert each of the nicks in the names to a user or create one... */
            .map { LevelledUser(it.level, userTracker.user(it.nick) ?: creationHooks.user(it.nick, eventSource)) }
            /* ...collect them up... */
            .toList()
            /* ...and emit an event. */
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