package co.fusionx.relay.internal

import co.fusionx.irc.message.Message
import co.fusionx.irc.plain.PlainParser
import co.fusionx.irc.plain.PlainStringifier
import co.fusionx.relay.*
import co.fusionx.relay.internal.event.eventHandlers
import co.fusionx.relay.internal.network.NetworkConnection
import co.fusionx.relay.internal.network.TCPSocketConnection
import co.fusionx.relay.internal.parser.DelegatingEventParser
import co.fusionx.relay.internal.parser.ext.ExtensionParsers
import co.fusionx.relay.internal.sturdy.SturdyConnection
import co.fusionx.relay.internal.sturdy.ThreadedSturdyConnection
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.concurrent.Executors

public class ClientImpl(private val hooks: Hooks,
                        private val connectionConfiguration: ConnectionConfiguration,
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
        fun start(hooks: Hooks,
                  connectionConfig: ConnectionConfiguration,
                  userConfig: UserConfiguration): Client {
            val client = ClientImpl(hooks, connectionConfig, userConfig)
            client.start()
            return client
        }
    }

    init {
        val messageSink = PublishSubject.create<Message>()

        networkConnection = TCPSocketConnection.create(connectionConfiguration, generateRawSink(messageSink))
        sturdyConnection = ThreadedSturdyConnection.create(networkConnection)

        /* Explicitly cross thread boundary here for the source observables */
        val mainExecutor = Executors.newSingleThreadExecutor()
        val mainScheduler = Schedulers.from(mainExecutor)
        val rawSource = networkConnection.rawSource.onBackpressureBuffer().observeOn(mainScheduler)
        val rawStatusSource = networkConnection.rawStatusSource.onBackpressureBuffer().observeOn(mainScheduler)

        val eventSource = generateEventSource(rawSource, rawStatusSource)

        /* TODO - figure out if this is the best way to do this */
        val initialNick = "*"
        val initialUser = hooks.atomCreation.user(initialNick, eventSource)

        channelTracker = ChannelTrackerImpl(eventSource)
        queryTracker = QueryTrackerImpl()
        userTracker = UserTrackerImpl(initialUser, eventSource, hashMapOf(), initialNick)
        session = hooks.atomCreation.session(eventSource, messageSink)
        server = hooks.atomCreation.server(eventSource, messageSink)

        val extCommands = ExtensionParsers.commandParsers(hooks.atomCreation, session, eventSource,
            messageSink, mainExecutor, channelTracker, userTracker)
        eventParser = DelegatingEventParser.create(hooks.atomCreation, extCommands, eventSource, messageSink,
            mainExecutor, channelTracker, userTracker)

        val coreHandler = eventHandlers(userConfiguration, session)
        coreHandler.subscribe { it.handle(eventSource, messageSink) }
    }

    private fun start() {
        sturdyConnection.start()
    }

    private fun generateEventSource(rawSource: Observable<String>,
                                    rawStatusSource: Observable<Status>): Observable<Event> {
        val stringMessageConverter = PlainParser.create()
        val inputMessages = rawSource.map { stringMessageConverter.parse(it) }
        val inputEvents = inputMessages.concatMap { eventParser.parse(it) }
        val statusEvents = rawStatusSource.map { StatusEvent(it) }
        return Observable.merge(statusEvents, inputEvents).share()
    }

    private fun generateRawSink(outputSink: PublishSubject<Message>): Observable<String> {
        val messageRawConverter = PlainStringifier.create()
        return outputSink.map { messageRawConverter.convert(it) }
    }
}