package co.fusionx.relay

import co.fusionx.irc.message.Message
import co.fusionx.relay.internal.ChannelImpl
import co.fusionx.relay.internal.ServerImpl
import co.fusionx.relay.internal.SessionImpl
import co.fusionx.relay.internal.UserImpl
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

public interface Hooks {
    public val atomCreation: AtomCreationHooks
}

public interface AbstractHooks : Hooks {
    override val atomCreation: AtomCreationHooks
        get() = DefaultAtomCreationHooks
}

public object DefaultHooks : AbstractHooks

/**
 * Hook methods invoked when an atom is created.
 */
public interface AtomCreationHooks {
    /* Notify methods */
    protected fun onSession(session: Session): Session = session

    protected fun onServer(server: Server): Server = server

    protected fun onChannel(channel: Channel): Channel = channel

    protected fun onQuery(query: Query): Query = query

    protected fun onUser(user: User): User = user

    /* Creation methods */
    public fun session(eventSource: Observable<Event>, messageSink: PublishSubject<Message>): Session

    public fun server(eventSource: Observable<Event>, messageSink: PublishSubject<Message>): Server

    public fun channel(channelName: String,
                       eventSource: Observable<Event>,
                       messageSink: PublishSubject<Message>,
                       mainExecutor: ExecutorService): Channel

    public fun query(eventSource: Observable<Event>, messageSink: PublishSubject<Message>): Query

    public fun user(initialNick: String, eventSource: Observable<Event>): User
}

public interface AbstractAtomCreationHooks : AtomCreationHooks {
    override final fun session(eventSource: Observable<Event>, messageSink: PublishSubject<Message>): Session =
        onSession(SessionImpl(eventSource, messageSink))

    override final fun server(eventSource: Observable<Event>, messageSink: PublishSubject<Message>): Server =
        onServer(ServerImpl(eventSource, messageSink))

    override final fun channel(channelName: String,
                               eventSource: Observable<Event>,
                               messageSink: PublishSubject<Message>,
                               mainExecutor: ExecutorService): Channel =
        onChannel(ChannelImpl(channelName, eventSource, messageSink, mainExecutor))

    override final fun query(eventSource: Observable<Event>, messageSink: PublishSubject<Message>): Query =
        onQuery(object : Query {})

    override final fun user(initialNick: String, eventSource: Observable<Event>): User =
        onUser(UserImpl(initialNick, eventSource))
}

public object DefaultAtomCreationHooks : AbstractAtomCreationHooks