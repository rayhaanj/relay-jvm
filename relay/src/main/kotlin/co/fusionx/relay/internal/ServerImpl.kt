package co.fusionx.relay.internal

import co.fusionx.irc.message.Message
import co.fusionx.relay.Event
import co.fusionx.relay.Server
import co.fusionx.relay.ServerEvent
import rx.Observable
import rx.subjects.PublishSubject

public class ServerImpl(raweventSource: Observable<Event>,
                        private val outputStream: PublishSubject<Message>) : Server {

    override val eventSource: Observable<ServerEvent>

    init {
        eventSource = raweventSource.ofType(javaClass<ServerEvent>()).share()
    }
}