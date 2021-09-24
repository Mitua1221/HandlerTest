package com.arjental.handlertest

import okhttp3.internal.wait
import java.lang.Exception
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ProducerConsumer<T> {

    private val buffer = 40
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val tasksQueue = LinkedList<T>()
    private val tasksQueueKtl = mutableListOf<T>()

    private fun produce(t: T) {
        lock.withLock {
            try {
                while (tasksQueue.size >= buffer && tasksQueueKtl.size >= buffer) {
                    condition.await()
                }
                tasksQueue.addLast(t)
                tasksQueueKtl.add(t)
                condition.signal()
            } catch (e: Exception) {

            }

        }
    }

    private fun consume(): T {
        lock.withLock {
            while (tasksQueue.size < 1) {
                condition.await()
            }
            val t = tasksQueue.removeFirst()
            val v = tasksQueueKtl.removeFirst()
            return t ?: v
        }

        //        lock.withLock {
        //            val t = try {
        //                tasksQueue.first
        //            } catch (e: NoSuchElementException) {
        //                //logged
        //                null
        //            }
        //            return t
        //        }
    }

}