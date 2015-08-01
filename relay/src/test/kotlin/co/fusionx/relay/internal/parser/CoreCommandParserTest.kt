package co.fusionx.relay.internal.parser

import co.fusionx.irc.Prefix
import co.fusionx.irc.message.CommandMessageData
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import org.mockito.Mockito.*
import org.mockito.Matchers.*
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import java.util.concurrent.ExecutorService
import org.junit.Test as test

public class CoreCommandParserTest {

    private val eventSource: PublishSubject<Event> = PublishSubject.create()
    private val outputSink: PublishSubject<Message> = PublishSubject.create()

    private val atomCreationHooks = mock(javaClass<AtomCreationHooks>())
    private val mainExecutor = mock(javaClass<ExecutorService>())
    private val channelTracker = mock(javaClass<ChannelTracker>())
    private val userTracker = mock(javaClass<UserTracker>())

    private val coreCommandParser = CoreCommandParser.create(
        atomCreationHooks,
        eventSource,
        outputSink,
        mainExecutor,
        channelTracker,
        userTracker
    )

    private val eventSubscriber = TestSubscriber<Event>()

    public test fun testFinalCapMessage() {
        val message = CommandMessageData(command = "CAP", arguments = listOf("*", "LS", "cap-notify sasl"))
        val source = coreCommandParser.parse(message)
        source.subscribe(eventSubscriber)

        val capabilities = listOf(Capability("cap-notify"), Capability("sasl"))
        eventSubscriber.assertValue(CapEvent(CapType.LS, true, capabilities))
        eventSubscriber.assertNoErrors()
        eventSubscriber.assertCompleted()
    }

    public test fun testNonFinalCapMessage() {
        val message = CommandMessageData(command = "CAP", arguments = listOf("*", "LS", "*", "sasl"))
        val source = coreCommandParser.parse(message)
        source.subscribe(eventSubscriber)

        val capabilities = listOf(Capability("sasl"))
        eventSubscriber.assertValue(CapEvent(CapType.LS, false, capabilities))
        eventSubscriber.assertNoErrors()
        eventSubscriber.assertCompleted()
    }

    public test fun testSelfJoin() {
        val self = mock(javaClass<User>())
        val channel = mock(javaClass<Channel>())

        `when`(userTracker.self).thenReturn(self)
        `when`(userTracker.user("relay")).thenReturn(self)
        `when`(atomCreationHooks.channel("#relay", eventSource, outputSink, mainExecutor)).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "JOIN",
            arguments = listOf("#relay")
        )
        val source = coreCommandParser.parse(message)
        source.subscribe(eventSubscriber)

        eventSubscriber.assertValue(JoinEvent(channel, self))
        eventSubscriber.assertNoErrors()
        eventSubscriber.assertCompleted()
    }

    public test fun testOtherNewUserJoin() {
        val other = mock(javaClass<User>())
        val channel = mock(javaClass<Channel>())

        `when`(atomCreationHooks.user("relay", eventSource)).thenReturn(other)
        `when`(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "JOIN",
            arguments = listOf("#relay")
        )
        val source = coreCommandParser.parse(message)
        source.subscribe(eventSubscriber)

        eventSubscriber.assertValue(JoinEvent(channel, other))
        eventSubscriber.assertNoErrors()
        eventSubscriber.assertCompleted()
    }

    public test fun testOtherOldUserJoin() {
        val other = mock(javaClass<User>())
        val channel = mock(javaClass<Channel>())

        `when`(userTracker.user("relay")).thenReturn(other)
        `when`(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "JOIN",
            arguments = listOf("#relay")
        )
        val source = coreCommandParser.parse(message)
        source.subscribe(eventSubscriber)

        eventSubscriber.assertValue(JoinEvent(channel, other))
        eventSubscriber.assertNoErrors()
        eventSubscriber.assertCompleted()
    }
}