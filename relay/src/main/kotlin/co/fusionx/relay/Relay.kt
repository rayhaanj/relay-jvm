package co.fusionx.relay

import co.fusionx.irc.message.Message
import co.fusionx.irc.plain.PlainParser
import co.fusionx.irc.plain.PlainStringifier
import co.fusionx.relay.internal.*
import co.fusionx.relay.internal.event.CoreEventHandler
import co.fusionx.relay.internal.network.NetworkConnection
import co.fusionx.relay.internal.network.TCPNettyConnection
import co.fusionx.relay.internal.parser.DelegatingEventParser
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject

public class Relay private constructor(configuration: ConnectionConfiguration,
                                       userConfig: UserConfiguration) {

    public val server: Server
    public val session: Session
    public val channelTracker: ChannelTracker

    private val networkConnection: NetworkConnection
    private val userTracker: UserTracker
    private val queryTracker: QueryTracker
    private val eventParser: DelegatingEventParser

    companion object {
        public fun create(configuration: ConnectionConfiguration,
                          userConfig: UserConfiguration): Relay {
            return Relay(configuration, userConfig)
        }
    }

    init {
        /* These two and the event stream are our main flow of data in the system */
        val outputSubject = PublishSubject.create<Message>()
        val rawOutputObservable = generateRawOutputStream(outputSubject)

        networkConnection = TCPNettyConnection.create(configuration, rawOutputObservable)

        val eventObservable = generateEventObservable(networkConnection.rawSource, networkConnection.rawStatusSource)

        /* Generate the stateful objects */
        /* TODO - figure out if this is the best way to do this */
        val initialNick = "*"
        val initialUser = UserImpl("*", eventObservable)

        channelTracker = ChannelTrackerImpl(eventObservable)
        queryTracker = QueryTrackerImpl()
        userTracker = UserTrackerImpl(initialUser, eventObservable, hashMapOf(), initialNick)
        session = SessionImpl(eventObservable, outputSubject)
        server = ServerImpl(eventObservable, outputSubject)

        /* Initialize the message -> event converter */
        eventParser = DelegatingEventParser.create(session, eventObservable, outputSubject, channelTracker, userTracker)

        /* Generate the core handler and make it start observing */
        val coreHandler = CoreEventHandler(userConfig, session)
        coreHandler.handle(eventObservable, outputSubject)
    }

    private fun generateEventObservable(rawSource: Observable<String>,
                                        rawStatusSource: Observable<Status>): Observable<Event> {
        /* Create the message generator */
        val stringMessageConverter = PlainParser.create()

        /* Convert each string to a message */
        val inputMessages = rawSource.map { stringMessageConverter.parse(it) }

        /* Convert each message to one or more events */
        val inputEvents = inputMessages.concatMap { eventParser.parse(it) }

        /* Make up the status stream by wrapping statuses as events */
        val statusEvents = rawStatusSource.map { StatusEvent(it) }

        /* This is our final stream that we use everywhere else in the system */
        return Observable.merge(statusEvents, inputEvents).share()
    }

    private fun generateRawOutputStream(outputStream: PublishSubject<Message>): Observable<String> {
        val messageRawConverter = PlainStringifier.create()
        return outputStream.map { messageRawConverter.convert(it) }
    }

    public fun connect() {
        networkConnection.connect()
            .subscribeOn(Schedulers.newThread())
            .subscribe()
    }

    private fun close() {

    }
}