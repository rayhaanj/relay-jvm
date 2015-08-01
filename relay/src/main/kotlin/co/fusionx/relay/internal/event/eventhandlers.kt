package co.fusionx.relay.internal.event

import co.fusionx.irc.message.Message
import co.fusionx.relay.Event
import co.fusionx.relay.Session
import co.fusionx.relay.UserConfiguration
import rx.Observable
import rx.subjects.PublishSubject

public interface EventHandler {
    public fun handle(eventSource: Observable<Event>,
                      outputSink: PublishSubject<Message>)
}

public fun eventHandlers(userConfig: UserConfiguration,
                         session: Session): Observable<EventHandler> = Observable.just(
    CoreEventHandler(userConfig),
    CapEventHandler(session)
)