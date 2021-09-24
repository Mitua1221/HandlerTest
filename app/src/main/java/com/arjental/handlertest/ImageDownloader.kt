package com.arjental.handlertest

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Looper.getMainLooper
import android.os.Message
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import org.xmlpull.v1.XmlPullParserFactory
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ImageDownloader"
private const val MESSAGE_TO_DOWNLOAD = 0

class ImageDownloader<in T>(
    private val responseHandler: Handler,
    private val onImageDownload: (T, Bitmap) -> Unit
): HandlerThread(TAG)/*, LifecycleObserver*/ {

    private var hasQuite = false
    private lateinit var requestHandler: Handler
    private var requestMap = ConcurrentHashMap<T, String>()
    private var downloader = Downloader()

    override fun quit(): Boolean {
        hasQuite = true
        return super.quit()
    }

    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler(Looper.getMainLooper()) {
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

    val actLifecycleObserver: LifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setup() {
            start()
            looper //getlooper()
        }
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun down() {
            quit()
        }

    }

    val viewLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue() {
            requestHandler.removeMessages(MESSAGE_TO_DOWNLOAD)
            requestMap.clear()
        }
    }


//    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//    fun setup() {
//        start()
//        looper //getlooper()
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun down() {
//        quit()
//    }

    fun queueDownload(target: T, url: String) {
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_TO_DOWNLOAD, target).sendToTarget()
    }

    private fun handleDownload(target: T) {
        val url = requestMap[target] ?: return
        val bitmap = downloader.download(url) ?: return

        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuite) {
                return@Runnable
            }

            requestMap.remove(target)
            onImageDownload(target, bitmap)

        })

    }

}

class Downloader() {

    fun download(url: String): Bitmap? = TODO()
}