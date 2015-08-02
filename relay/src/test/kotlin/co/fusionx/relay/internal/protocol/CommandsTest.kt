package co.fusionx.relay.internal.protocol

import co.fusionx.relay.*
import co.fusionx.relay.test.assertThat
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import org.junit.Test as test

public class CommandsTest {

    public test fun testCommands() {
        assertThat(Commands.JOIN).isEqualTo("JOIN")
        assertThat(Commands.NICK).isEqualTo("NICK")
        assertThat(Commands.NOTICE).isEqualTo("NOTICE")
        assertThat(Commands.PING).isEqualTo("PING")
        assertThat(Commands.CAP).isEqualTo("CAP")
        assertThat(Commands.PART).isEqualTo("PART")
        assertThat(Commands.QUIT).isEqualTo("QUIT")
    }
}