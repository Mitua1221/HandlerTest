package com.arjental.handlertest

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity() {

//    private lateinit var downloader: ImageDownloader<Alpha>

    private lateinit var picturesDownloader: PicturesDownloader<ImageView>
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)

        val responseHandler = Handler(Looper.getMainLooper())

        picturesDownloader = PicturesDownloader(responseHandler) { photoContainer, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            imageView.setImageDrawable(drawable)
        }

        picturesDownloader.setup()

        textView.setOnClickListener {
            picturesDownloader.queueDownload(imageView, "https://live.staticflickr.com//65535//51462695216_55b0fd9810_m.jpg")
        }

//        val responseHandler = Handler(Looper.getMainLooper())
//        downloader = ImageDownloader(responseHandler) { photoHolder, bitmap ->
//            val drawable = BitmapDrawable(resources, bitmap)
//            photoHolder.bindDrawable()
//
//        }
//        lifecycle.addObserver(downloader.actLifecycleObserver)
//        downloader.queueDownload(Alpha, galleryItem.url)

    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
//        lifecycle.addObserver(downloader.viewLifecycleObserver)
        return super.onCreateView(name, context, attrs)
    }




    override fun onDestroy() {
        super.onDestroy()

        picturesDownloader.down()
        picturesDownloader.clearQueue()


//        lifecycle.removeObserver(downloader.viewLifecycleObserver)
//        lifecycle.removeObserver(downloader.actLifecycleObserver)
    }

}

class Alpha