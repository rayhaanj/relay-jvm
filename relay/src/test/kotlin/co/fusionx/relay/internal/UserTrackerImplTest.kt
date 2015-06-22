package co.fusionx.relay.internal

import co.fusionx.relay.Event
import co.fusionx.relay.User
import com.google.common.truth.Truth.assertThat
import org.mockito.Mockito.mock
import rx.subjects.PublishSubject
import org.junit.Test as test

public class UserTrackerImplTest {

    private val user = mock(javaClass<User>())
    private val eventStream = PublishSubject.create<Event>()
    private val userNickMap = hashMapOf<String, User>()

    private val tracker = UserTrackerImpl(user, eventStream, userNickMap, "*")

    test fun selfUserSetupCorrectly() {
        assertThat(tracker.self).isEqualTo(user)
        assertThat(tracker.user("*")).isEqualTo(user)
    }

    test fun test() {

    }
}
