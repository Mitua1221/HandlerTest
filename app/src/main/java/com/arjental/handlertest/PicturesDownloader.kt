package com.arjental.handlertest

import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "PicturesDownloader"
private const val MESSAGE_TO_DOWNLOAD = 0


class PicturesDownloader<in T> (
    private val responseHandler: Handler,
    private val onImageDownload: (T, Bitmap) -> Unit,
    ): HandlerThread(TAG) {

    private var hasQuite = false
    private lateinit var requestHandler: Handler
    private var requestMap = ConcurrentHashMap<T, String>()
    private var downloader = FlickrFetchr()

    override fun quit(): Boolean {
        hasQuite = true
        return super.quit()
    }

    override fun onLooperPrepared() {
        requestHandler = object : Handler(looper) {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_TO_DOWNLOAD) {
                    val target = msg.obj as T
                    handleDownload(target)
                }
                super.handleMessage(msg)
            }
        }
        super.onLooperPrepared()
    }

    fun setup() {
        start()
        looper //getlooper()
    }

    fun down() {
        quit()
        clearQueue()
    }

    fun clearQueue() {
        requestHandler.removeMessages(MESSAGE_TO_DOWNLOAD)
        requestMap.clear()
    }

    fun queueDownload(target: T, url: String) {
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_TO_DOWNLOAD, target).sendToTarget()

        Log.d(TAG, Thread.currentThread().name.toString() + "3")

    }

    private fun handleDownload(target: T) {
        val url = requestMap[target] ?: return
        val bitmap = downloader.fetchPhoto(url) ?: return

        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuite) {
                return@Runnable
            }

            requestMap.remove(target)
            onImageDownload(target, bitmap)

        })

    }




}

