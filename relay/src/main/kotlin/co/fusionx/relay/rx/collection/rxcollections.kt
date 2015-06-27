package co.fusionx.relay.rx.collection

import rx.Observable
import rx.functions.Func0

public interface RxCollectionStructureEvent<T>

public data class RxCollectionAddEvent<T>(val item: T) : RxCollectionStructureEvent<T>
public data class RxCollectionRemoveEvent<T>(val item: T) : RxCollectionStructureEvent<T>
public data class RxCollectionUpdateEvent<T>(val index: Int, val item: T) : RxCollectionStructureEvent<T>
public data class RxCollectionClearEvent<T> : RxCollectionStructureEvent<T> {
    public override fun equals(other: Any?): Boolean {
        if (other === null) return false
        if (other === this) return true
        return other.javaClass === javaClass<RxCollectionClearEvent<T>>()
    }

    public override fun hashCode(): Int = 42
}

public data class RxCollectionAddAllEvent<T>(val items: Collection<T>) : RxCollectionStructureEvent<T>
public data class RxCollectionRemoveAllEvent<T>(val items: Collection<T>) : RxCollectionStructureEvent<T>

public interface RxCollection<T> : MutableCollection<T> {
    public val events: Observable<RxCollectionStructureEvent<T>>

    public fun snapshot(): Collection<T>
}

public interface RxList<T> : RxCollection<T>

public object RxCollections {
    public fun listFrom<T>(producer: Func0<MutableList<T>>): RxList<T> = DelegatingRxList(producer.call())
}