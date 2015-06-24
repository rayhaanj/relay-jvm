package co.fusionx.relay.rx

import rx.Observable
import rx.Subscriber
import rx.exceptions.OnErrorThrowable

internal fun <T : Any> Observable<T?>.filterNotNull(): Observable<T> = lift(OperatorFilterNotNull.instance())

public class OperatorFilterNotNull<T : Any>() : Observable.Operator<T, T?> {
    override fun call(child: Subscriber<in T>): Subscriber<in T?> = object : Subscriber<T?>(child) {
        override fun onCompleted() {
            child.onCompleted()
        }

        override fun onError(e: Throwable) {
            child.onError(e)
        }

        override fun onNext(t: T?) {
            try {
                if (t != null) {
                    child.onNext(t)
                } else {
                    request(1)
                }
            } catch (e: Throwable) {
                child.onError(OnErrorThrowable.addValueAsLastCause(e, t))
            }
        }
    }

    companion object {
        private var filter: OperatorFilterNotNull<out Any>? = null

        public synchronized fun <T> instance(): OperatorFilterNotNull<T> {
            var localFilter = filter as OperatorFilterNotNull<T>?
            if (localFilter == null) {
                localFilter = OperatorFilterNotNull<T>()
                filter = localFilter
            }
            return localFilter
        }
    }
}