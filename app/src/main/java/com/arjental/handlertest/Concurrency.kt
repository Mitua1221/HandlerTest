package com.arjental.handlertest

import android.util.Log
import kotlinx.coroutines.*

import java.lang.IllegalStateException
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock


class Incrementor {

    init {
        Log.d(
            "TAG",
            "Incrementor created"
        )
    }



    @Volatile private var foo = true

    private var usualCounter = 0

    private var atomicCounter = AtomicInteger(0)

    private var synchronizedCounter = 0

    private val lockedCounterLock = ReentrantLock()
    private var lockedCounter = 0

    private val semaphoreCounterLock = Semaphore(1)
    private var semaphoreCounter = 0

    fun updateUsualCounter() {
        usualCounter++
    }

    fun updateSynchronizedCounter() {
        synchronized(this) {
            synchronizedCounter++
        }
    }

    fun updateAtomicCounter() {
        atomicCounter.getAndIncrement()
    }

    fun getAtomicCounter(): String {
        return atomicCounter.get().toString()
    }

    fun getSynchronizedCounter(): String {
        return synchronizedCounter.toString()
    }

    fun getUsualCounter(): String {
        return usualCounter.toString()
    }

    fun getSemaphoreCounter(): String {
        return semaphoreCounter.toString()
    }

    fun updateLockedCounter() {
            try {
                lockedCounterLock.lock()
                lockedCounter++
            } finally {
                lockedCounterLock.unlock()
            }
    }

    suspend fun updateSemaphoreCounter() {
        try {
            semaphoreCounterLock.acquire()
            semaphoreCounter++
        } finally {
            semaphoreCounterLock.release()
        }
    }


    fun getLockedCounter(): String {
        return lockedCounter.toString()
    }

    companion object {

        @Volatile
        private var incrementor: Incrementor? = null

        fun getIncrementor(): Incrementor {
            if (incrementor == null) {
                synchronized(this) {
                    if (incrementor == null) incrementor = Incrementor()
                }
            }
            return incrementor
                ?: throw IllegalStateException("Incrementor.getIncrementor() return null. Incrementor is not created.")
        }

    }

}

@ObsoleteCoroutinesApi
class Concurrency {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val incrementor by lazy { Incrementor.getIncrementor() }

    //private lateinit var incrementor: Incrementor

    init {

        scope.launch {


            val scope = CoroutineScope(newFixedThreadPoolContext(4, "synchronizationPool"))
            scope.launch {
                val coroutines = (1..1000).map {
                    launch {
                        for (i in 1..1000) {
//                            incrementor = Incrementor.getIncrementor()
                            incrementor.updateAtomicCounter()
                            incrementor.updateSynchronizedCounter()
                            incrementor.updateUsualCounter()
                            incrementor.updateLockedCounter()
                            incrementor.updateSemaphoreCounter()
                        }
                    }
                }

                coroutines.forEach { corotuine ->
                    corotuine.join()
                }
            }.join()

            Log.d(
                "TAG",
                "The number of Atomic counter should be 10000000, but actually is ${incrementor.getAtomicCounter()}\n" +
                        "The number of Synchronized counter should be 10000000, but actually is ${incrementor.getSynchronizedCounter()}\n" +
                        "The number of Usual counter should be 10000000, but actually is ${incrementor.getUsualCounter()}\n" +
                        "The number of Locked counter should be 10000000, but actually is ${incrementor.getLockedCounter()}\n" +
                        "The number of Locked counter should be 10000000, but actually is ${incrementor.getSemaphoreCounter()}\n"
            )

//            Log.d(
//                "TAG",
//                "${incrementor.retLocksCount()}"
//            )

        }

    }


}