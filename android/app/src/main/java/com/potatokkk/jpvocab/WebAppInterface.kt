package com.potatokkk.jpvocab

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.webkit.JavascriptInterface

class WebAppInterface(private val activity: MainActivity) {
    @JavascriptInterface
    fun setUnlockEnabled(enabled: Boolean) {
        Prefs.setUnlockEnabled(activity, enabled)
        activity.runOnUiThread { UnlockService.sync(activity) }
        if (enabled) requestOverlayPermission()
    }

    @JavascriptInterface
    fun isUnlockEnabled(): Boolean = Prefs.unlockEnabled(activity)

    @JavascriptInterface
    fun hasOverlayPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(activity)
    }

    @JavascriptInterface
    fun requestOverlayPermission() {
        if (hasOverlayPermission()) return
        activity.runOnUiThread {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}"),
            )
            activity.startActivity(intent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.requestNotifications()
        }
    }

    @JavascriptInterface
    fun syncVocab(json: String) {
        Prefs.setVocabJson(activity, json)
        WordRepository.invalidate()
    }

    @JavascriptInterface
    fun previewUnlock() {
        activity.runOnUiThread {
            UnlockService.start(activity)
            (activity.application as VocabApp).overlay.show()
        }
    }
}
