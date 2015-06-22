package co.fusionx.relay.internal

import co.fusionx.relay.Status
import rx.Observable

public interface Connection {
    val input: Observable<String>
    val status: Observable<Status>

    fun connect(): Observable<Connection>
    fun disconnect()
}