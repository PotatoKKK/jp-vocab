package com.potatokkk.jpvocab

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import java.util.Locale

class OverlayController(private val appCtx: Context) {
    private val wm = appCtx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: View? = null
    private var tts: TextToSpeech? = null
    private var current: VocabWord? = null
    private var revealed = true

    fun show() {
        if (view != null) {
            bind(WordRepository.pick(appCtx) ?: return)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !android.provider.Settings.canDrawOverlays(appCtx)
        ) {
            return
        }
        val v = LayoutInflater.from(appCtx).inflate(R.layout.overlay_word, null)
        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
            PixelFormat.TRANSLUCENT,
        )
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        try {
            wm.addView(v, params)
            view = v
            tts = TextToSpeech(appCtx) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.JAPANESE
                }
            }
            v.findViewById<Button>(R.id.btnClose).setOnClickListener { hide() }
            v.findViewById<Button>(R.id.btnNext).setOnClickListener {
                bind(WordRepository.pick(appCtx) ?: return@setOnClickListener)
            }
            v.findViewById<Button>(R.id.btnReveal).setOnClickListener {
                revealed = !revealed
                render()
            }
            v.findViewById<View>(R.id.card).setOnClickListener {
                revealed = !revealed
                render()
            }
            v.findViewById<View>(R.id.btnSpeak).setOnClickListener {
                val w = current ?: return@setOnClickListener
                tts?.speak(w.speech.ifBlank { w.kana }, TextToSpeech.QUEUE_FLUSH, null, "w")
            }
            bind(WordRepository.pick(appCtx))
        } catch (_: Exception) {
            view = null
        }
    }

    fun hide() {
        view?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
        }
        view = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun bind(word: VocabWord?) {
        current = word
        revealed = true
        render()
    }

    private fun render() {
        val v = view ?: return
        val w = current
        val mark = v.findViewById<TextView>(R.id.mark)
        val trans = v.findViewById<TextView>(R.id.trans)
        val group = v.findViewById<TextView>(R.id.group)
        val kana = v.findViewById<TextView>(R.id.kana)
        val kanji = v.findViewById<TextView>(R.id.kanji)
        val zh = v.findViewById<TextView>(R.id.zh)
        val exJp = v.findViewById<TextView>(R.id.exJp)
        val exZh = v.findViewById<TextView>(R.id.exZh)
        val reveal = v.findViewById<Button>(R.id.btnReveal)
        if (w == null) {
            kana.text = "到 Groups 勾選至少一課"
            kanji.text = ""
            zh.text = ""
            return
        }
        mark.text = w.mark
        trans.text = w.trans
        group.text = WordRepository.displayGroup(w.groupName)
        kana.text = w.kana.ifBlank { w.kanji }
        val showKanji = w.kanji.isNotBlank() && w.kanji != "-" && w.kanji != w.kana
        kanji.text = if (showKanji) w.kanji else ""
        kanji.visibility = if (showKanji) View.VISIBLE else View.GONE
        zh.text = w.zh
        zh.alpha = if (revealed) 1f else 0.12f
        exJp.text = w.exampleJp
        exZh.text = w.exampleZh
        exZh.alpha = if (revealed) 1f else 0.12f
        reveal.text = if (revealed) "隱藏意思" else "顯示意思"
    }
}
