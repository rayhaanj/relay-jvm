package co.fusionx.relay.internal.parser

import co.fusionx.irc.Prefix
import co.fusionx.irc.message.CommandMessageData
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
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
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        val capabilities = listOf(Capability("cap-notify"), Capability("sasl"))
        eventSubscriber.assertValueCompletedNoErrors(CapEvent(CapType.LS, true, capabilities))
    }

    public test fun testNonFinalCapMessage() {
        val message = CommandMessageData(command = "CAP", arguments = listOf("*", "LS", "*", "sasl"))
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        val capabilities = listOf(Capability("sasl"))
        eventSubscriber.assertValueCompletedNoErrors(CapEvent(CapType.LS, false, capabilities))
    }

    public test fun testSelfJoin() {
        val (self, channel) = Pair(mock(javaClass<User>()), mock(javaClass<Channel>()))
        `when`(userTracker.self).thenReturn(self)
        `when`(userTracker.user("relay")).thenReturn(self)
        `when`(atomCreationHooks.channel("#relay", eventSource, outputSink, mainExecutor)).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "JOIN",
            arguments = listOf("#relay")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValueCompletedNoErrors(JoinEvent(channel, self))
    }

    public test fun testOtherNewUserJoin() {
        val (other, channel) = Pair(mock(javaClass<User>()), mock(javaClass<Channel>()))
        `when`(atomCreationHooks.user("relay", eventSource)).thenReturn(other)
        `when`(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "JOIN",
            arguments = listOf("#relay")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValueCompletedNoErrors(JoinEvent(channel, other))
    }

    public test fun testOtherOldUserJoin() {
        val (other, channel) = Pair(mock(javaClass<User>()), mock(javaClass<Channel>()))
        `when`(userTracker.user("relay")).thenReturn(other)
        `when`(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "JOIN",
            arguments = listOf("#relay")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValueCompletedNoErrors(JoinEvent(channel, other))
    }

    public test fun testNick() {
        val user = mock(javaClass<User>())
        `when`(userTracker.user("relay")).thenReturn(user)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "NICK",
            arguments = listOf("new-relay")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValueCompletedNoErrors(NickEvent(user, "relay", "new-relay"))
    }

    public test fun testPing() {
        val message = CommandMessageData(
            command = "PING",
            arguments = listOf("test.server.relay")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValueCompletedNoErrors(PingEvent("test.server.relay"))
    }

    public test fun testQuitWithReason() {
        val (user, channel) = Pair(mock(javaClass<User>()), mock(javaClass<Channel>()))
        `when`(userTracker.user("relay")).thenReturn(user)
        `when`(user.channels).thenReturn(setOf(channel))

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "QUIT",
            arguments = listOf("some reason here")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValuesCompletedNoErrors(ChannelQuitEvent(channel, user, "some reason here"),
            QuitEvent(user, "some reason here"))
    }

    public test fun testQuitWithoutReason() {
        val (user, channel) = Pair(mock(javaClass<User>()), mock(javaClass<Channel>()))
        `when`(userTracker.user("relay")).thenReturn(user)
        `when`(user.channels).thenReturn(setOf(channel))

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "QUIT",
            arguments = listOf()
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValuesCompletedNoErrors(ChannelQuitEvent(channel, user), QuitEvent(user))
    }

    public test fun testPartWithReason() {
        val (user, channel) = Pair(mock(javaClass<User>()), mock(javaClass<Channel>()))
        `when`(userTracker.user("relay")).thenReturn(user)
        `when`(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "PART",
            arguments = listOf("#relay", "some reason here")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValuesCompletedNoErrors(PartEvent(channel, user, "some reason here"))
    }

    public test fun testPartWithoutReason() {
        val (user, channel) = Pair(mock(javaClass<User>()), mock(javaClass<Channel>()))
        `when`(userTracker.user("relay")).thenReturn(user)
        `when`(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "PART",
            arguments = listOf("#relay")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValuesCompletedNoErrors(PartEvent(channel, user))
    }

    public test fun testPrivmsgToChannel() {
        val (user, channel) = Pair(mock(javaClass<User>()), mock(javaClass<Channel>()))
        `when`(userTracker.user("relay")).thenReturn(user)
        `when`(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "PRIVMSG",
            arguments = listOf("#relay", "some message here")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValuesCompletedNoErrors(ChannelPrivmsgEvent(channel, user, "relay", "some message here"))
    }

    public test fun testPrivmsgToServer() {
        val user = mock(javaClass<User>())
        `when`(userTracker.user("relay")).thenReturn(user)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "PRIVMSG",
            arguments = listOf("self", "some message here")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValuesCompletedNoErrors(ServerPrivmsgEvent(user, "relay", "some message here"))
    }

    public test fun testNoticeToChannel() {
        val (user, channel) = Pair(mock(javaClass<User>()), mock(javaClass<Channel>()))
        `when`(userTracker.user("relay")).thenReturn(user)
        `when`(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "NOTICE",
            arguments = listOf("#relay", "some message here")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        // TODO(tilal6991) consider changing this to NoticeEvent.
        eventSubscriber.assertValuesCompletedNoErrors(ChannelPrivmsgEvent(channel, user, "relay", "some message here"))
    }

    public test fun testNoticeToServer() {
        val user = mock(javaClass<User>())
        `when`(userTracker.user("relay")).thenReturn(user)

        val message = CommandMessageData(
            prefix = Prefix("relay"),
            command = "NOTICE",
            arguments = listOf("self", "some message here")
        )
        coreCommandParser.parse(message).subscribe(eventSubscriber)

        // TODO(tilal6991) consider changing this to NoticeEvent.
        eventSubscriber.assertValuesCompletedNoErrors(ServerPrivmsgEvent(user, "relay", "some message here"))
    }
}