package co.fusionx.relay.rx

import rx.Observable
import rx.Subscriber
import rx.exceptions.OnErrorThrowable

internal fun <T : Any> Observable<T?>.filterNotNull(): Observable<T> = lift(OperatorFilterNotNull.instance())

public class OperatorFilterNotNull<T : Any>() : Observable.Operator<T, T?> {
    override fun call(child: Subscriber<in T>): Subscriber<in T?> = object : Subscriber<T?>(child) {
        override fun onCompleted() = child.onCompleted()

        override fun onError(e: Throwable) = child.onError(e)

        override fun onNext(t: T?) = try {
            if (t == null) {
                request(1)
            } else {
                child.onNext(t)
            }
        } catch (e: Throwable) {
            child.onError(OnErrorThrowable.addValueAsLastCause(e, t))
        }
    }

    companion object {
        private var filter: OperatorFilterNotNull<out Any>? = null

        public synchronized fun <T> instance(): OperatorFilterNotNull<T> {
            if (filter == null) {
                filter = OperatorFilterNotNull<T>()
            }
            return filter as OperatorFilterNotNull<T>
        }
    }
}