package co.fusionx.relay.internal

import co.fusionx.irc.message.Message
import co.fusionx.irc.plain.PlainParser
import co.fusionx.irc.plain.PlainStringifier
import co.fusionx.relay.*
import co.fusionx.relay.internal.event.CoreEventHandler
import co.fusionx.relay.internal.network.NetworkConnection
import co.fusionx.relay.internal.network.TCPSocketConnection
import co.fusionx.relay.internal.parser.DelegatingEventParser
import co.fusionx.relay.internal.sturdy.SturdyConnection
import co.fusionx.relay.internal.sturdy.ThreadedSturdyConnection
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.concurrent.Executors

public class ClientImpl(private val connectionConfiguration: ConnectionConfiguration,
                        private val userConfiguration: UserConfiguration) : Client {

    public override val server: Server
    public override val session: Session
    public override val channelTracker: ChannelTracker

    private val sturdyConnection: SturdyConnection
    private val networkConnection: NetworkConnection
    private val userTracker: UserTracker
    private val queryTracker: QueryTracker
    private val eventParser: DelegatingEventParser

    companion object {
        fun start(connectionConfig: ConnectionConfiguration, userConfig: UserConfiguration): Client =
            ClientImpl(connectionConfig, userConfig)
    }

    init {
        /* These two and the event stream are our main flow of data in the system */
        val messageSink = PublishSubject.create<Message>()

        /* Generate the connections */
        networkConnection = TCPSocketConnection.create(connectionConfiguration, generateRawSink(messageSink))
        sturdyConnection = ThreadedSturdyConnection.create(networkConnection)

        /* Explicitly cross thread boundary here for the source observables */
        val sourceScheduler = Schedulers.from(Executors.newSingleThreadExecutor())
        val rawSource = networkConnection.rawSource.onBackpressureBuffer().observeOn(sourceScheduler)
        val rawStatusSource = networkConnection.rawStatusSource.onBackpressureBuffer().observeOn(sourceScheduler)

        /* Get the final event source */
        val eventSource = generateEventSource(rawSource, rawStatusSource)

        /* Generate the stateful objects */
        /* TODO - figure out if this is the best way to do this */
        val initialNick = "*"
        val initialUser = UserImpl(initialNick, eventSource)

        /* Generate the stateful objects */
        channelTracker = ChannelTrackerImpl(eventSource)
        queryTracker = QueryTrackerImpl()
        userTracker = UserTrackerImpl(initialUser, eventSource, hashMapOf(), initialNick)
        session = SessionImpl(eventSource, messageSink)
        server = ServerImpl(eventSource, messageSink)

        /* Initialize the message -> event converter */
        eventParser = DelegatingEventParser.create(session, eventSource, messageSink, channelTracker, userTracker)

        /* Generate the core handler and make it start observing */
        val coreHandler = CoreEventHandler(userConfiguration, session)
        coreHandler.handle(eventSource, messageSink)

        /* Start the actual connection */
        sturdyConnection.start()
    }

    private fun generateEventSource(rawSource: Observable<String>,
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

    private fun generateRawSink(outputSink: PublishSubject<Message>): Observable<String> {
        val messageRawConverter = PlainStringifier.create()
        return outputSink.map { messageRawConverter.convert(it) }
    }
}