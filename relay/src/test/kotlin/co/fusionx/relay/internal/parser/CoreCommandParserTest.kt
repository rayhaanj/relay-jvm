package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CommandMessageData
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import org.mockito.Mockito.mock
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import java.util.concurrent.ExecutorService
import org.junit.Test as test

public class CoreCommandParserTest {

    private val eventSource: PublishSubject<Event> = PublishSubject.create()
    private val outputSink: PublishSubject<Message> = PublishSubject.create()
    private val coreCommandParser = CoreCommandParser.create(
        mock(javaClass<AtomCreationHooks>()),
        eventSource,
        outputSink,
        mock(javaClass<ExecutorService>()),
        mock(javaClass<ChannelTracker>()),
        mock(javaClass<UserTracker>())
    )

    private val eventSubscriber = TestSubscriber<Event>()

    public test fun testSingleLineCapMessage() {
        val message = CommandMessageData(command = "CAP", arguments = listOf("*", "LS", "cap-notify sasl"))
        val source = coreCommandParser.parse(message)
        source.subscribe(eventSubscriber)

        val capabilities = listOf(Capability("cap-notify"), Capability("sasl"))
        eventSubscriber.assertValue(CapEvent(CapType.LS, true, capabilities))
        eventSubscriber.assertNoErrors()
        eventSubscriber.assertCompleted()
    }
}