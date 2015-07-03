package co.fusionx.relay.rx

import co.fusionx.relay.rx.collection.*
import co.fusionx.relay.test.assertThat
import rx.observers.TestSubscriber
import org.junit.Test as test

public class DelegatingRxListTest {

    private val subscriber = TestSubscriber<RxCollectionStructureEvent<String>>()
    private val internalList = arrayListOf<String>()
    private val list = DelegatingRxList(internalList)

    /* add tests */
    public test fun addGeneratesAdd() {
        list.events.subscribe(subscriber)
        list.add("TestItem")

        assertThat(subscriber.getOnNextEvents())
            .containsExactly(RxCollectionAddEvent("TestItem"))
        assertThat(list.snapshot()).containsExactly("TestItem")
        subscriber.assertNoErrors()
    }

    /* set tests */
    public test fun setGeneratesUpdate() {
        internalList.add("TestItem")
        list.events.subscribe(subscriber)
        list.set(0, "First")

        assertThat(subscriber.getOnNextEvents())
            .containsExactly(RxCollectionUpdateEvent(0, "First"))
        assertThat(list.snapshot()).containsExactly("First")
        subscriber.assertNoErrors()
    }

    /* remove tests */
    public test fun removeNonExistingItemGeneratesNoting() {
        list.events.subscribe(subscriber)
        list.remove("TestItem")

        assertThat(subscriber.getOnNextEvents()).isEmpty()
        assertThat(list.snapshot()).isEmpty()
        subscriber.assertNoErrors()
    }

    public test fun removeExistingItemGeneratesRemove() {
        internalList.add("TestItem")
        list.events.subscribe(subscriber)
        list.remove("TestItem")

        assertThat(subscriber.getOnNextEvents())
            .containsExactly(RxCollectionRemoveEvent("TestItem"))
        assertThat(list.snapshot()).isEmpty()
        subscriber.assertNoErrors()
    }

    /* addAll tests */
    public test fun addAllGeneratesAdds() {
        val testList = listOf("First", "Second", "Third")

        list.events.subscribe(subscriber)
        list.addAll(testList)

        assertThat(subscriber.getOnNextEvents())
            .containsExactly(RxCollectionAddAllEvent(testList))
        assertThat(list.snapshot()).containsExactlyElementsOf(testList)
        subscriber.assertNoErrors()
    }

    /* removeAll tests */
    public test fun removeAllExistingGeneratesRemove() {
        val testList = listOf("First", "Second", "Third")
        internalList.addAll(testList)
        list.events.subscribe(subscriber)
        list.removeAll(testList)

        assertThat(subscriber.getOnNextEvents())
            .containsExactly(RxCollectionRemoveAllEvent(testList))
        assertThat(list.snapshot()).isEmpty()
        subscriber.assertNoErrors()
    }

    public test fun removeSomeExistingGeneratesRemove() {
        val removeList = listOf("First", "Second", "Third")

        internalList.addAll(listOf("First", "Second"))
        list.events.subscribe(subscriber)
        list.removeAll(removeList)

        assertThat(subscriber.getOnNextEvents())
            .containsExactly(RxCollectionRemoveAllEvent(removeList))
        assertThat(list.snapshot()).isEmpty()
        subscriber.assertNoErrors()
    }

    public test fun removeNoExistingGeneratesNothing() {
        val removeList = listOf("First", "Second", "Third")

        list.events.subscribe(subscriber)
        list.removeAll(removeList)

        assertThat(subscriber.getOnNextEvents()).isEmpty()
        assertThat(list.snapshot()).isEmpty()
        subscriber.assertNoErrors()
    }

    /* retainAll tests */
    public test fun retainGeneratesClearAndAddForIntersection() {
        val testList = listOf("First", "Second", "Third")
        val retainedList = listOf("First", "Second")

        internalList.addAll(testList)
        list.events.subscribe(subscriber)
        list.retainAll(retainedList)

        assertThat(subscriber.getOnNextEvents())
            .containsExactly(RxCollectionClearEvent(), RxCollectionAddAllEvent(retainedList))
        assertThat(list.snapshot()).containsExactlyElementsOf(retainedList)
        subscriber.assertNoErrors()
    }
}