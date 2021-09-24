package com.arjental.handlertest

import android.app.Application
import kotlinx.coroutines.ObsoleteCoroutinesApi

class Application: Application() {


    override fun onCreate() {
        super.onCreate()

        val s = Concurrency()

    }

}