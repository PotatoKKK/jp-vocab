package com.potatokkk.jpvocab

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

class OverlayController(private val appCtx: Context) {
    private val wm = appCtx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var view: View? = null
    private var current: VocabWord? = null
    private var revealed = true
    var onShown: (() -> Unit)? = null
    var onUserDismiss: (() -> Unit)? = null

    private val retryRunnable = Runnable { showNow() }
    private val autoHideRunnable = Runnable { hideInternal(fromUser = true) }

    fun isShowing(): Boolean = view != null

    fun show() {
        handler.post { showNow() }
    }

    fun hide() {
        hideInternal(fromUser = false)
    }

    private fun hideInternal(fromUser: Boolean) {
        handler.removeCallbacks(retryRunnable)
        handler.removeCallbacks(autoHideRunnable)
        handler.post {
            view?.let {
                try { wm.removeView(it) } catch (_: Exception) {}
            }
            view = null
            if (fromUser) onUserDismiss?.invoke()
        }
    }

    private fun armAutoHide() {
        handler.removeCallbacks(autoHideRunnable)
        handler.postDelayed(autoHideRunnable, AUTO_HIDE_MS)
    }

    private fun speaker(): TtsSpeaker = (appCtx.applicationContext as VocabApp).tts

    private fun showNow() {
        if (!Permissions.hasOverlay(appCtx)) return
        if (view != null) {
            onShown?.invoke()
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
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT,
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        try {
            wm.addView(v, params)
            view = v
            handler.removeCallbacks(retryRunnable)
            v.findViewById<Button>(R.id.btnClose).setOnClickListener { hideInternal(fromUser = true) }
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
            v.findViewById<View>(R.id.btnSpeak).setOnClickListener { speakCurrent() }
            bind(WordRepository.pick(appCtx))
            armAutoHide()
            onShown?.invoke()
        } catch (_: Exception) {
            view = null
            handler.removeCallbacks(retryRunnable)
            handler.postDelayed(retryRunnable, 800)
        }
    }

    private fun speakCurrent() {
        val w = current ?: return
        speaker().speak(w.speech.ifBlank { w.kana.ifBlank { w.kanji } })
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
            kana.text = appCtx.getString(R.string.empty_pool)
            kanji.text = ""
            kanji.visibility = View.GONE
            zh.text = ""
            exJp.text = ""
            exZh.text = ""
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
        reveal.text = if (revealed) appCtx.getString(R.string.hide_meaning) else appCtx.getString(R.string.show_meaning)
    }

    companion object {
        private const val AUTO_HIDE_MS = 30_000L
    }
}
