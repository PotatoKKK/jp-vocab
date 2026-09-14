package com.potatokkk.jpvocab

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private lateinit var setup: View
    private var pendingOverlay = false
    private var pendingBattery = false

    private val notifyPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* ignore */ }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        web = findViewById(R.id.web)
        setup = findViewById(R.id.setup)
        val loader = WebViewAssetLoader.Builder()
            .setDomain("appassets.androidplatform.net")
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                loader.shouldInterceptRequest(request.url)
        }
        web.addJavascriptInterface(WebAppInterface(this), "JpVocabAndroid")
        web.loadUrl("https://appassets.androidplatform.net/assets/www/index.html")

        findViewById<Button>(R.id.btnEnableUnlock).setOnClickListener { startUnlockSetup() }
        findViewById<Button>(R.id.btnSkipSetup).setOnClickListener { skipSetup() }
        findViewById<Button>(R.id.btnOemAutostart).setOnClickListener {
            Permissions.openAutostartSettings(this)
        }

        if (Permissions.hasOverlay(this) && Prefs.unlockEnabled(this)) {
            Prefs.setSetupDone(this, true)
        }
        setup.visibility = if (Prefs.setupDone(this)) View.GONE else View.VISIBLE
        refreshSetupUi()
        UnlockService.sync(this)
        if (Prefs.unlockEnabled(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifications()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSetupUi()
        if (pendingOverlay && Permissions.hasOverlay(this)) {
            pendingOverlay = false
            continueUnlockSetup()
            return
        }
        if (pendingBattery) {
            pendingBattery = false
            finishUnlockSetup(showPreview = true)
            return
        }
        UnlockService.sync(this)
    }

    fun startUnlockSetup() {
        Prefs.setUnlockEnabled(this, true)
        requestNotifications()
        continueUnlockSetup()
    }

    private fun continueUnlockSetup() {
        if (!Permissions.hasOverlay(this)) {
            pendingOverlay = true
            Permissions.openOverlaySettings(this)
            return
        }
        if (!Permissions.hasBatteryExemption(this)) {
            pendingBattery = true
            Permissions.openBatterySettings(this)
            return
        }
        finishUnlockSetup(showPreview = true)
    }

    private fun finishUnlockSetup(showPreview: Boolean) {
        Prefs.setUnlockEnabled(this, true)
        Prefs.setSetupDone(this, true)
        UnlockService.start(this)
        setup.visibility = View.GONE
        if (showPreview && Permissions.hasOverlay(this)) {
            web.postDelayed({
                (application as VocabApp).overlay.show()
            }, 280)
        }
    }

    private fun skipSetup() {
        Prefs.setSetupDone(this, true)
        setup.visibility = View.GONE
    }

    private fun refreshSetupUi() {
        if (!this::setup.isInitialized || setup.visibility != View.VISIBLE) return
        val overlayOk = Permissions.hasOverlay(this)
        val batteryOk = Permissions.hasBatteryExemption(this)
        findViewById<TextView>(R.id.setupOverlay).text =
            getString(if (overlayOk) R.string.perm_overlay_ok else R.string.perm_overlay_need)
        findViewById<TextView>(R.id.setupBattery).text =
            getString(if (batteryOk) R.string.perm_battery_ok else R.string.perm_battery_need)
    }

    fun requestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifyPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (setup.visibility == View.VISIBLE) {
            skipSetup()
            return
        }
        if (this::web.isInitialized && web.canGoBack()) web.goBack()
        else super.onBackPressed()
    }
}
