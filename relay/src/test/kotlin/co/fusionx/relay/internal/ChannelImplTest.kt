package co.fusionx.relay.internal

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import co.fusionx.relay.test.assertThat
import org.mockito.Mockito.mock
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import java.util.concurrent.Executors
import org.junit.Test as test

public class ChannelImplTest {

    private val executor = Executors.newSingleThreadExecutor()

    private val eventSource: PublishSubject<Event> = PublishSubject.create()
    private val outputSink: PublishSubject<Message> = PublishSubject.create()
    private val channel = ChannelImpl("#relay", eventSource, outputSink, executor)

    private val messageSubscriber = TestSubscriber<Message>()

    public test fun testToStringIsName() {
        assertThat(channel.toString()).isEqualTo("#relay")
    }

    public test fun testJoinAddsToMap() {
        val user = mock<User>()
        eventSource.onNext(JoinEvent(channel, user))

        val map = channel.users
            .toObservable()
            .toBlocking()
            .first()
        assertThat(map[user]).isEqualTo(listOf<UserLevel>())
    }

    public test fun testNamesUpdatesMap() {
        val first = LevelledUser(listOf(UserLevel.OP), mock<User>())
        val second = LevelledUser(listOf(UserLevel.HALFOP), mock<User>())
        val third = LevelledUser(listOf(), mock<User>())

        eventSource.onNext(ChannelNamesReplyEvent(channel, listOf(first, second, third)))

        val map = channel.users
            .toObservable()
            .toBlocking()
            .first()
        assertThat(map[first.user]).isEqualTo(first.levels)
        assertThat(map[second.user]).isEqualTo(second.levels)
        assertThat(map[third.user]).isEqualTo(third.levels)
    }

    public test fun testPrivmsgCommand() {
        val messageString = "Test message"
        val message = ClientMessageGenerator.privmsg("#relay", messageString)

        outputSink.subscribe(messageSubscriber)
        channel.privmsg(messageString)

        messageSubscriber.assertValue(message)
        messageSubscriber.assertNoErrors()
    }
}