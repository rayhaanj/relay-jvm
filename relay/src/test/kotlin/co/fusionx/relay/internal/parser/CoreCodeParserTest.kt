package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CodeMessageData
import co.fusionx.relay.*
import co.fusionx.relay.internal.protocol.ReplyCodes
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import org.junit.Test as test

public class CoreCodeParserTest {

    private val eventSource: PublishSubject<Event> = PublishSubject.create()

    private val atomCreationHooks = mock<AtomCreationHooks>()
    private val channelTracker = mock<ChannelTracker>()
    private val userTracker = mock<UserTracker>()

    private val coreCodeParser = CoreCodeParser.create(
        atomCreationHooks,
        eventSource,
        channelTracker,
        userTracker
    )

    private val eventSubscriber = TestSubscriber<Event>()

    public test fun testWelcome() {
        val user = mock<User>()
        on(userTracker.self).thenReturn(user)
        on(userTracker.self.nick).thenReturn(Observable.just("*"))

        val message = CodeMessageData(
            code = ReplyCodes.RPL_WELCOME,
            target = "relay",
            arguments = listOf("some message here")
        )
        coreCodeParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertValuesCompletedNoErrors(
            NickEvent(user, "*", "relay"),
            ServerGenericCodeEvent(ReplyCodes.RPL_WELCOME, "some message here"),
            StatusEvent(Status.CONNECTED)
        )
    }

    public test fun testIsupport() {
        val message = CodeMessageData(
            code = ReplyCodes.RPL_ISUPPORT,
            target = "relay",
            arguments = listOf("CHANTYPES=#", "are supported by this server.")
        )
        coreCodeParser.parse(message).subscribe(eventSubscriber)

        eventSubscriber.assertNoValues()
        eventSubscriber.assertCompleted()
        eventSubscriber.assertNoErrors()
    }

    public test fun test2812Names() {
        val (voice, op, channel) = Triple(mock<User>(), mock<User>(), mock<Channel>())
        on(userTracker.user("relay-voice")).thenReturn(voice)
        on(atomCreationHooks.user("relay-op", eventSource)).thenReturn(op)
        on(channelTracker.channel("#relay")).thenReturn(channel)

        val message = CodeMessageData(
            code = ReplyCodes.RPL_NAMES,
            target = "relay",
            arguments = listOf("=", "#relay", "+relay-voice @+relay-op")
        )
        coreCodeParser.parse(message).subscribe(eventSubscriber)

        val userList = listOf(
            LevelledUser(listOf(UserLevel.VOICE), voice),
            LevelledUser(listOf(UserLevel.OP, UserLevel.VOICE), op)
        )
        eventSubscriber.assertValuesCompletedNoErrors(ChannelNamesReplyEvent(channel, userList))
    }
}