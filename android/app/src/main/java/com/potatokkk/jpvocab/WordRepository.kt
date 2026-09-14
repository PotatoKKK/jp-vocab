package com.potatokkk.jpvocab

import android.content.Context
import org.json.JSONObject
import kotlin.random.Random

data class VocabWord(
    val kana: String,
    val kanji: String,
    val zh: String,
    val mark: String,
    val trans: String,
    val exampleJp: String,
    val exampleZh: String,
    val speech: String,
    val groupName: String,
)

object WordRepository {
    @Volatile private var cache: List<VocabWord>? = null

    fun invalidate() {
        cache = null
    }

    fun pick(ctx: Context): VocabWord? {
        val pool = all(ctx)
        if (pool.isEmpty()) return null
        return pool[Random.nextInt(pool.size)]
    }

    fun all(ctx: Context): List<VocabWord> {
        cache?.let { return it }
        val json = Prefs.vocabJson(ctx) ?: readAsset(ctx)
        val parsed = parse(json)
        cache = parsed
        return parsed
    }

    private fun readAsset(ctx: Context): String {
        return ctx.assets.open("www/vocab/default-groups.json").bufferedReader().use { it.readText() }
    }

    private fun parse(raw: String): List<VocabWord> {
        val out = ArrayList<VocabWord>()
        try {
            val root = JSONObject(raw)
            val groups = root.optJSONArray("groups") ?: return out
            for (i in 0 until groups.length()) {
                val g = groups.optJSONObject(i) ?: continue
                if (!g.optBoolean("active", true)) continue
                val name = g.optString("name")
                val words = g.optJSONArray("words") ?: continue
                for (j in 0 until words.length()) {
                    val w = words.optJSONObject(j) ?: continue
                    val kana = w.optString("kana")
                    val kanji = w.optString("kanji")
                    if (kana.isBlank() && kanji.isBlank()) continue
                    out.add(
                        VocabWord(
                            kana = kana,
                            kanji = kanji,
                            zh = w.optString("zh"),
                            mark = w.optString("mark"),
                            trans = w.optString("trans"),
                            exampleJp = w.optString("exampleJp"),
                            exampleZh = w.optString("exampleZh"),
                            speech = w.optString("speech").ifBlank { kana.ifBlank { kanji } },
                            groupName = name,
                        ),
                    )
                }
            }
        } catch (_: Exception) {
            /* keep empty */
        }
        return out
    }

    fun displayGroup(name: String): String {
        val m = Regex("^(?:int_)?ch(\\d+)$", RegexOption.IGNORE_CASE).find(name)
        return if (m != null) "第${m.groupValues[1].toInt()}課" else name
    }
}
