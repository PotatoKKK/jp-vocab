package com.potatokkk.jpvocab

import android.app.Application

class VocabApp : Application() {
    lateinit var overlay: OverlayController
        private set
    lateinit var tts: TtsSpeaker
        private set

    override fun onCreate() {
        super.onCreate()
        tts = TtsSpeaker(this)
        overlay = OverlayController(this)
        if (Prefs.unlockEnabled(this)) {
            UnlockService.start(this)
        }
    }
}
