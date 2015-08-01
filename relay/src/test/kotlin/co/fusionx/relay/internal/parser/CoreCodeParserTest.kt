package co.fusionx.relay.internal.parser

import co.fusionx.irc.message.CodeMessageData
import co.fusionx.relay.*
import co.fusionx.relay.internal.protocol.ReplyCodes
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import rx.Observable
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import org.junit.Test as test

public class CoreCodeParserTest {

    private val eventSource: PublishSubject<Event> = PublishSubject.create()

    private val atomCreationHooks = mock(javaClass<AtomCreationHooks>())
    private val channelTracker = mock(javaClass<ChannelTracker>())
    private val userTracker = mock(javaClass<UserTracker>())

    private val coreCodeParser = CoreCodeParser.create(
        atomCreationHooks,
        eventSource,
        channelTracker,
        userTracker
    )

    private val eventSubscriber = TestSubscriber<Event>()

    public test fun testWelcome() {
        val user = mock(javaClass<User>())
        `when`(user.nick).thenReturn(Observable.just("*"))
        `when`(userTracker.self).thenReturn(user)

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
}