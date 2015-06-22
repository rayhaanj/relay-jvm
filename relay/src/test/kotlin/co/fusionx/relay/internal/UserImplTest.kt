package co.fusionx.relay.internal

import co.fusionx.relay.*
import com.google.common.truth.Truth.assertThat
import org.mockito.Mockito.mock
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import org.junit.Test as test

public class UserImplTest {

    private val nick = "initial-nick"
    private val eventStream = PublishSubject.create<Event>()
    private val user = UserImpl(nick, eventStream)

    test fun testInitialNickIsCorrect() {
        val testSubscriber = TestSubscriber<String>()
        user.nick.subscribe(testSubscriber)

        testSubscriber.assertReceivedOnNext(listOf(nick))
        testSubscriber.assertNoErrors()
    }

    test fun testNickEventCausesNickChange() {
        val testSubscriber = TestSubscriber<String>()
        user.nick.subscribe(testSubscriber)

        eventStream.onNext(NickEvent(user, nick, "final-nick"))

        testSubscriber.assertReceivedOnNext(listOf(nick, "final-nick"))
        testSubscriber.assertNoErrors()
    }

    test fun testChannelJoinCausesAdditionToChannels() {
        val channel = mock(javaClass<Channel>())
        eventStream.onNext(JoinEvent(channel, user))

        assertThat<Channel, List<Channel>>(user.channels).contains(channel)
    }

    test fun testChannelPartCausesRemovalFromChannels() {
        val channel = mock(javaClass<Channel>())
        eventStream.onNext(JoinEvent(channel, user))
        eventStream.onNext(PartEvent(channel, user))

        assertThat<Channel, List<Channel>>(user.channels).isEmpty()
    }

    test fun testQuitCausesRemovalOfAllChannels() {
        eventStream.onNext(JoinEvent(mock(javaClass<Channel>()), user))
        eventStream.onNext(JoinEvent(mock(javaClass<Channel>()), user))
        eventStream.onNext(QuitEvent(user))

        assertThat<Channel, List<Channel>>(user.channels).isEmpty()
    }
}