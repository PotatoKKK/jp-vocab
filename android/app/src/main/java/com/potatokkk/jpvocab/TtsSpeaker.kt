package com.potatokkk.jpvocab

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsSpeaker(ctx: Context) {
    private val appCtx = ctx.applicationContext
    private val audio = appCtx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    @Volatile private var ready = false
    @Volatile private var pending: String? = null
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(appCtx) { status ->
            if (status != TextToSpeech.SUCCESS) {
                ready = false
                return@TextToSpeech
            }
            pickJapanese()
            tts?.setSpeechRate(0.92f)
            tts?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            ready = true
            pending?.let {
                pending = null
                speakNow(it)
            }
        }
    }

    fun speak(text: String) {
        val say = text.trim()
        if (say.isEmpty()) return
        if (!ready || tts == null) {
            pending = say
            return
        }
        speakNow(say)
    }

    private fun speakNow(text: String) {
        val engine = tts ?: return
        requestFocus()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "jp-vocab")
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "jp-vocab")
    }

    private fun pickJapanese() {
        val engine = tts ?: return
        val candidates = listOf(
            Locale.JAPANESE,
            Locale.JAPAN,
            Locale("ja", "JP"),
            Locale("ja"),
        )
        for (loc in candidates) {
            val avail = engine.isLanguageAvailable(loc)
            if (avail >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = loc
                return
            }
        }
        engine.language = Locale.JAPANESE
    }

    private fun requestFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    )
                    .build()
                audio.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                audio.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
                )
            }
        } catch (_: Exception) {
            /* still try to speak */
        }
    }
}
