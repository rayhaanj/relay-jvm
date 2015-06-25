package co.fusionx.relay

import co.fusionx.irc.message.Message
import co.fusionx.irc.plain.PlainParser
import co.fusionx.irc.plain.PlainStringifier
import co.fusionx.relay.internal.*
import co.fusionx.relay.internal.event.CoreEventHandler
import co.fusionx.relay.internal.parser.DelegatingEventParser
import co.fusionx.relay.internal.tcp.netty.NettyConnection
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject

public class Relay private constructor(configuration: ConnectionConfiguration,
    userConfig: UserConfiguration) {

    public val server: Server
    public val session: Session
    public val channelTracker: ChannelTracker

    private val connection: Connection
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
        val outputStream = PublishSubject.create<Message>()
        val rawOutputStream = generateRawOutputStream(outputStream)

        connection = NettyConnection.create(configuration, rawOutputStream)

        val eventStream = generateEventStream(connection.input, connection.status)

        /* Generate the stateful objects */
        /* TODO - figure out if this is the best way to do this */
        val initialNick = "*"
        val initialUser = UserImpl("*", eventStream)

        channelTracker = ChannelTrackerImpl(eventStream)
        queryTracker = QueryTrackerImpl()
        userTracker = UserTrackerImpl(initialUser, eventStream, hashMapOf(), initialNick)
        session = SessionImpl(eventStream, outputStream)
        server = ServerImpl(eventStream, outputStream)

        /* Initialize the message -> event converter */
        eventParser = DelegatingEventParser.create(session, eventStream, outputStream, channelTracker, userTracker)

        /* Generate the core handler and make it start observing */
        val coreHandler = CoreEventHandler(userConfig, session)
        coreHandler.handle(eventStream, outputStream)
    }

    private fun generateEventStream(rawInputStream: Observable<String>,
        statusStream: Observable<Status>): Observable<Event> {
        /* Create the message generator */
        val stringMessageConverter = PlainParser.create()

        /* Convert each string to a message */
        val inputMessageStream = rawInputStream.asObservable().map { stringMessageConverter.parse(it) }

        /* Convert each message to one or more events */
        val inputEventStream = inputMessageStream.concatMap { eventParser.parse(it) }

        /* Make up the status stream by wrapping statuses as events */
        val statusEventStream = statusStream.map { StatusEvent(it) }

        /* This is our final stream that we use everywhere else in the system */
        return Observable.merge(statusEventStream, inputEventStream).share()
    }

    private fun generateRawOutputStream(outputStream: PublishSubject<Message>): Observable<String> {
        val messageRawConverter = PlainStringifier.create()
        return outputStream.map { messageRawConverter.convert(it) }
    }

    public fun connect() {
        connection.connect()
            .subscribeOn(Schedulers.newThread())
            .subscribe()
    }

    private fun close() {
        /*
        val closed = closed.getAndSet(true)
        if (!closed) {
            /* TODO - do this properly */
            connection.disconnect()
        }
        */
    }
}