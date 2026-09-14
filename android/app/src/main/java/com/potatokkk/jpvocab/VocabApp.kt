package com.potatokkk.jpvocab

import android.app.Application

class VocabApp : Application() {
    lateinit var overlay: OverlayController
        private set

    override fun onCreate() {
        super.onCreate()
        overlay = OverlayController(this)
        if (Prefs.unlockEnabled(this)) {
            UnlockService.start(this)
        }
    }
}
