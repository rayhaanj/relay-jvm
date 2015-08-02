package co.fusionx.relay.internal.parser.ext

import co.fusionx.irc.Prefix
import co.fusionx.irc.message.CommandMessageData
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import java.util.concurrent.ExecutorService

public class ExtendedJoinParserTest {

    private val eventSource: PublishSubject<Event> = PublishSubject.create()
    private val outputSink: PublishSubject<Message> = PublishSubject.create()

    private val session = mock<Session>()
    private val atomCreationHooks = mock<AtomCreationHooks>()
    private val mainExecutor = mock<ExecutorService>()
    private val channelTracker = mock<ChannelTracker>()
    private val userTracker = mock<UserTracker>()

    private val extendedJoinParser = ExtendedJoinParser(
        atomCreationHooks,
        session,
        eventSource,
        outputSink,
        mainExecutor,
        channelTracker,
        userTracker
    )

    private val eventSubscriber = TestSubscriber<Event>()

    public Test fun testSelfJoin() {
        val (self, channel) = Pair(mock<User>(), mock<Channel>())
        `when`(userTracker.self).thenReturn(self)
        `when`(userTracker.user("relay")).thenReturn(self)
        `when`(atomCreationHooks.channel("#relay", eventSource, outputSink, mainExecutor)).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "JOIN",
            arguments = listOf("#relay", "*", "Real Name")
        )
        extendedJoinParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValuesCompletedNoErrors(JoinEvent(channel, self))
    }

    public Test fun testOtherNewUserJoin() {
        val (other, channel) = Pair(mock<User>(), mock<Channel>())
        `when`(atomCreationHooks.user("relay", eventSource)).thenReturn(other)
        `when`(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "JOIN",
            arguments = listOf("#relay", "*", "Real Name")
        )
        extendedJoinParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValuesCompletedNoErrors(JoinEvent(channel, other))
    }

    public Test fun testOtherOldUserJoin() {
        val (other, channel) = Pair(mock<User>(), mock<Channel>())
        `when`(userTracker.user("relay")).thenReturn(other)
        `when`(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "JOIN",
            arguments = listOf("#relay", "*", "Real Name")
        )
        extendedJoinParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValuesCompletedNoErrors(JoinEvent(channel, other))
    }
}