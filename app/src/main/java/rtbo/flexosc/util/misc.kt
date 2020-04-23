package rtbo.flexosc.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> liveDataValueDelegate(liveData: MutableLiveData<T>) = object : ReadWriteProperty<Any, T> {
    init {
        assert(liveData.value != null)
    }

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return liveData.value!!
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        liveData.value = value
    }
}

fun <T> liveDataValueDelegate(liveData: LiveData<T>) = object : ReadOnlyProperty<Any, T> {
    init {
        assert(liveData.value != null)
    }

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return liveData.value!!
    }
}

class LiveDataObservatory<T>(private val liveData: LiveData<T>) {
    fun add(owner: LifecycleOwner, observer: Observer<T>) {
        liveData.observe(owner, observer)
    }

    fun remove(observer: Observer<T>) {
        liveData.removeObserver(observer)
    }

    fun removeFrom(owner: LifecycleOwner) {
        liveData.removeObservers(owner)
    }
}

