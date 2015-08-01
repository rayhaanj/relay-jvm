package co.fusionx.relay.internal.event

import co.fusionx.irc.message.ClientMessageGenerator
import co.fusionx.irc.message.Message
import co.fusionx.relay.*
import rx.Observable
import rx.subjects.PublishSubject

public class CoreEventHandler(private val userConfig: UserConfiguration): EventHandler {

    public override fun handle(eventSource: Observable<Event>,
                               outputSink: PublishSubject<Message>) {
        /* Auto respond to pings with pongs */
        eventSource.ofType(javaClass<PingEvent>())
            .map { ClientMessageGenerator.pong(it.server) }
            .subscribe { outputSink.onNext(it) }

        /* Messages sent on initial connection */
        eventSource.ofType(javaClass<StatusEvent>())
            .filter { it.status == Status.SOCKET_CONNECTED }
            .concatMap {
                /* Send CAP LS, USER and NICK */
                Observable.just(
                    ClientMessageGenerator.cap(CapType.LS.asString),
                    ClientMessageGenerator.user(userConfig.username, userConfig.realName),
                    ClientMessageGenerator.nick(userConfig.nick)
                )
            }
            .subscribe { outputSink.onNext(it) }
    }
}