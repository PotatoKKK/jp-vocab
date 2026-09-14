package com.potatokkk.jpvocab

import android.os.Build
import android.webkit.JavascriptInterface

class WebAppInterface(private val activity: MainActivity) {
    @JavascriptInterface
    fun setUnlockEnabled(enabled: Boolean) {
        Prefs.setUnlockEnabled(activity, enabled)
        activity.runOnUiThread {
            UnlockService.sync(activity)
            if (enabled) activity.startUnlockSetup()
        }
    }

    @JavascriptInterface
    fun isUnlockEnabled(): Boolean = Prefs.unlockEnabled(activity)

    @JavascriptInterface
    fun hasOverlayPermission(): Boolean = Permissions.hasOverlay(activity)

    @JavascriptInterface
    fun hasBatteryExemption(): Boolean = Permissions.hasBatteryExemption(activity)

    @JavascriptInterface
    fun permissionState(): String = Permissions.stateJson(activity)

    @JavascriptInterface
    fun requestOverlayPermission() {
        activity.runOnUiThread {
            if (!Permissions.hasOverlay(activity)) {
                Permissions.openOverlaySettings(activity)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.requestNotifications()
            }
        }
    }

    @JavascriptInterface
    fun requestBatteryExemption() {
        activity.runOnUiThread {
            if (!Permissions.hasBatteryExemption(activity)) {
                Permissions.openBatterySettings(activity)
            }
        }
    }

    @JavascriptInterface
    fun openAutostartSettings() {
        activity.runOnUiThread { Permissions.openAutostartSettings(activity) }
    }

    @JavascriptInterface
    fun startUnlockSetup() {
        activity.runOnUiThread { activity.startUnlockSetup() }
    }

    @JavascriptInterface
    fun syncVocab(json: String) {
        Prefs.setVocabJson(activity, json)
    }

    @JavascriptInterface
    fun previewUnlock() {
        activity.runOnUiThread {
            UnlockService.start(activity)
            (activity.application as VocabApp).overlay.show()
        }
    }
}
