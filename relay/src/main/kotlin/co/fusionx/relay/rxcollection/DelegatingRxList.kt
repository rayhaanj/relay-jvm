package co.fusionx.relay.rxcollection

import rx.subjects.PublishSubject

class DelegatingRxList<T> internal constructor(private val internal: MutableList<T>) : RxList<T>,
    MutableList<T> by internal {

    override val events = PublishSubject.create<RxCollectionStructureEvent<T>>()

    override fun add(e: T): Boolean {
        val result = internal.add(e)
        if (result) {
            events.onNext(RxCollectionAddEvent(e))
        }
        return result
    }

    override fun add(index: Int, element: T) {
        val result = internal.add(index, element)
        events.onNext(RxCollectionAddEvent(element))
        return result
    }

    override fun set(index: Int, element: T): T {
        val result = internal.set(index, element)
        events.onNext(RxCollectionUpdateEvent(index, element))
        return result
    }

    override fun remove(index: Int): T {
        val result = internal.remove(index)
        events.onNext(RxCollectionRemoveEvent(result))
        return result
    }

    override fun remove(o: Any?): Boolean {
        val result = internal.remove(o)
        if (result) {
            events.onNext(RxCollectionRemoveEvent(o as T))
        }
        return result
    }

    override fun addAll(c: Collection<T>): Boolean {
        val result = internal.addAll(c)
        if (result) {
            events.onNext(RxCollectionAddAllEvent(c))
        }
        return result
    }

    override fun addAll(index: Int, c: Collection<T>): Boolean {
        val result = internal.addAll(index, c)
        if (result) {
            events.onNext(RxCollectionAddAllEvent(c))
        }
        return result
    }

    override fun removeAll(c: Collection<Any?>): Boolean {
        val result = internal.removeAll(c)
        if (result) {
            events.onNext(RxCollectionRemoveAllEvent(c as Collection<T>))
        }
        return result
    }

    override fun retainAll(c: Collection<Any?>): Boolean {
        val result = internal.retainAll(c)
        if (result) {
            events.onNext(RxCollectionClearEvent())
            events.onNext(RxCollectionAddAllEvent(internal))
        }
        return result
    }

    override fun clear() {
        internal.clear()
        events.onNext(RxCollectionClearEvent())
    }
}