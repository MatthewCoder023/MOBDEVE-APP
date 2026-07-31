package com.dlsu.unisync

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// Observes a LiveData until it emits, then unsubscribes. Room's LiveData
// queries run on a background executor, so tests have to wait for the value.
@Throws(InterruptedException::class)
fun <T> LiveData<T>.getOrAwaitValue(timeoutSeconds: Long = 5): T {
    var captured: Any? = NOT_SET
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            captured = value
            latch.countDown()
            removeObserver(this)
        }
    }
    androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync {
        observeForever(observer)
    }
    try {
        if (!latch.await(timeoutSeconds, TimeUnit.SECONDS)) {
            throw IllegalStateException("LiveData value was never set")
        }
    } finally {
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync {
            removeObserver(observer)
        }
    }

    @Suppress("UNCHECKED_CAST")
    return captured as T
}

private val NOT_SET = Any()
