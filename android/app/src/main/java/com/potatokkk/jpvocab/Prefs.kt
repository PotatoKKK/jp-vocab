package com.potatokkk.jpvocab

import android.content.Context

object Prefs {
    private const val FILE = "jp_vocab"
    private const val UNLOCK = "unlock_enabled"
    private const val VOCAB = "vocab_json"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun unlockEnabled(ctx: Context) = sp(ctx).getBoolean(UNLOCK, true)

    fun setUnlockEnabled(ctx: Context, value: Boolean) {
        sp(ctx).edit().putBoolean(UNLOCK, value).apply()
    }

    fun vocabJson(ctx: Context): String? = sp(ctx).getString(VOCAB, null)

    fun setVocabJson(ctx: Context, json: String) {
        sp(ctx).edit().putString(VOCAB, json).apply()
    }
}
