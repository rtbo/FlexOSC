package rtbo.flexosc.util

import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

typealias Observer<E> = (E) -> Unit

open class LiveEventSource<E> {

    @MainThread
    fun observe(owner: LifecycleOwner, observer: Observer<E>) {
        assert(isMainThread)

        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) return

        val existing = observers.find { it.observer == observer }
        if (existing != null) {
            if (!existing.isBoundTo(owner)) {
                throw IllegalStateException("Same observer can't be bound to a different lifecycle")
            } else {
                return
            }
        }
        val wrapper = LifecycleBoundWrapper(owner, observer)
        wrapper.attach()
        observers.add(wrapper)
    }

    @MainThread
    fun observeForever(observer: Observer<E>) {
        assert(isMainThread)

        val existing = observers.find { it.observer == observer }
        if (existing != null) {
            if (existing is LifecycleBoundWrapper) {
                throw IllegalStateException("Same observer can't be bound to a different lifecycle")
            } else {
                return
            }
        }
        val wrapper = ForeverWrapper(observer)
        wrapper.attach()
        observers.add(wrapper)
    }

    @MainThread
    fun removeObserver(observer: Observer<E>) {
        assert(isMainThread)

        val ind = observers.indexOfFirst { it.observer == observer }
        if (ind != -1) {
            observers[ind].detach()
            observers.removeAt(ind)
        }
    }

    @MainThread
    fun removeObservers(owner: LifecycleOwner) {
        assert(isMainThread)

        val (toRemove, toKeep) = observers.partition { it.isBoundTo(owner) }
        if (toRemove.isNotEmpty()) {
            toRemove.forEach { it.detach() }
            observers.clear()
            observers.addAll(toKeep)
        }
    }

    val hasObserver: Boolean
        @MainThread
        get() = observers.size > 0

    val hasActiveObserver: Boolean
        @MainThread
        get() = activeCount > 0

    @MainThread
    protected open fun onActive() {
    }

    @MainThread
    protected open fun onInactive() {
    }

    protected open suspend fun postEventNotification(event: E) {
        withContext(Dispatchers.Main) {
            notifyEvent(event)
        }
    }

    @MainThread
    protected open fun notifyEvent(event: E) {
        assert(isMainThread)

        for (obs in observers.filter { it.isActive }) {
            obs.observer(event)
        }
    }

    private val observers = ArrayList<Wrapper>()
    private var activeCount: Int by Delegates.observable(0) { _, old, new ->
        run {
            assert(isMainThread)
            assert(new >= 0)  // can't be negative
            if (old > 0 && new == 0) {
                onInactive()
            }
            if (old == 0 && new >= 0) {
                onActive()
            }
        }
    }

    private abstract inner class Wrapper(val observer: Observer<E>) {
        abstract val isActive: Boolean

        abstract fun isBoundTo(owner: LifecycleOwner): Boolean

        abstract fun attach()
        abstract fun detach()
    }

    private inner class ForeverWrapper(observer: Observer<E>) : Wrapper(observer) {
        override val isActive: Boolean = true

        override fun isBoundTo(owner: LifecycleOwner) = false

        override fun attach() {
            activeCount++
        }

        override fun detach() {
            activeCount--
        }
    }

    private inner class LifecycleBoundWrapper(val owner: LifecycleOwner, observer: Observer<E>) :
        Wrapper(observer), LifecycleObserver {

        override val isActive: Boolean
            get() = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

        override fun isBoundTo(owner: LifecycleOwner): Boolean = this.owner == owner

        override fun attach() {
            owner.lifecycle.addObserver(this)
        }

        override fun detach() {
            owner.lifecycle.removeObserver(this)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onOwnerStart() {
            activeCount++
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onOwnerStop() {
            activeCount--
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onOwnerDestroy() {
            removeObserver(observer)
        }
    }

    private val isMainThread
        get() = Thread.currentThread() == Looper.getMainLooper().thread
}

open class MutableLiveEventSource<E>() : LiveEventSource<E>() {
    public override suspend fun postEventNotification(event: E) {
        super.postEventNotification(event)
    }

    @MainThread
    public override fun notifyEvent(event: E) {
        super.notifyEvent(event)
    }
}

fun <E> LiveEventSource<E>.filter(predicate: (E) -> Boolean): LiveEventSource<E> {
    return FilterLiveEventSource(this, predicate)
}

fun <E, F> LiveEventSource<E>.map(fn: (E) -> F): LiveEventSource<F> {
    return MapLiveEventSource(this, fn)
}

fun <T> LiveEventSource<T>.asLiveData(): LiveData<T> {
    return AsLiveDataLiveEventSource(this)
}

private class FilterLiveEventSource<E>(
    private val source: LiveEventSource<E>,
    private val predicate: (E) -> Boolean
) : LiveEventSource<E>() {
    override fun onActive() {
        source.observeForever(this::onEvent)
    }

    override fun onInactive() {
        source.removeObserver(this::onEvent)
    }

    private fun onEvent(e: E) {
        if (predicate(e)) notifyEvent(e)
    }
}

private class MapLiveEventSource<E, F>(
    private val source: LiveEventSource<E>,
    private val fn: (E) -> F
) : LiveEventSource<F>() {
    override fun onActive() {
        source.observeForever(this::onEvent)
    }

    override fun onInactive() {
        source.removeObserver(this::onEvent)
    }

    private fun onEvent(e: E) {
        notifyEvent(fn(e))
    }
}

private class AsLiveDataLiveEventSource<T>(private val source: LiveEventSource<T>) : LiveData<T>() {
    override fun onActive() {
        source.observeForever(this::onValue)
    }

    override fun onInactive() {
        source.removeObserver(this::onValue)
    }

    private fun onValue(v: T) {
        value = v
    }
}

