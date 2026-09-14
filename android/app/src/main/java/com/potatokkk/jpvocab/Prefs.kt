package com.potatokkk.jpvocab

import android.content.Context
import java.io.File

object Prefs {
    private const val FILE = "jp_vocab"
    private const val UNLOCK = "unlock_enabled"
    private const val SETUP = "setup_done"
    private const val VOCAB = "vocab_json"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun unlockEnabled(ctx: Context) = sp(ctx).getBoolean(UNLOCK, true)

    fun setUnlockEnabled(ctx: Context, value: Boolean) {
        sp(ctx).edit().putBoolean(UNLOCK, value).apply()
    }

    fun setupDone(ctx: Context) = sp(ctx).getBoolean(SETUP, false)

    fun setSetupDone(ctx: Context, value: Boolean) {
        sp(ctx).edit().putBoolean(SETUP, value).apply()
    }

    fun vocabFile(ctx: Context) = File(ctx.filesDir, "vocab.json")

    fun vocabJson(ctx: Context): String? {
        val f = vocabFile(ctx)
        if (f.exists() && f.length() > 0L) {
            return runCatching { f.readText() }.getOrNull()
        }
        val legacy = sp(ctx).getString(VOCAB, null)
        if (!legacy.isNullOrBlank()) {
            setVocabJson(ctx, legacy)
            return legacy
        }
        return null
    }

    fun setVocabJson(ctx: Context, json: String) {
        runCatching { vocabFile(ctx).writeText(json) }
        if (sp(ctx).contains(VOCAB)) {
            sp(ctx).edit().remove(VOCAB).apply()
        }
        WordRepository.invalidate()
    }
}
