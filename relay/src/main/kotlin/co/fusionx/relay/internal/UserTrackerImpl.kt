package co.fusionx.relay.internal

import co.fusionx.relay.*
import rx.Observable

internal class UserTrackerImpl(override val self: User,
                               private val eventSource: Observable<Event>,
                               private val userNickMap: MutableMap<String, User>,
                               private val selfNick: String) : UserTracker {

    init {
        userNickMap[selfNick] = self

        eventSource.ofType(javaClass<ChannelNamesReplyEvent>())
            /* concatMap to get a Observable of users */
            .concatMap { Observable.from(it.levelledUsers) }
            /* concatMap here even though we'll only get one nick user pair for each user */
            .concatMap { it.user.nick.first().map { n -> Pair(n, it.user) } }
            /* Add the pair to the map in a thread safe way */
            .subscribe { pair -> userNickMap[pair.first] = pair.second }

        eventSource.ofType(javaClass<NickEvent>())
            .subscribe {
                /* TODO - check that below user is equal to event's user */
                userNickMap.remove(it.oldNick)
                userNickMap[it.newNick] = it.user
            }
    }

    override internal fun user(nick: String): User? = userNickMap[nick]
}