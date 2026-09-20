package com.potatokkk.jpvocab

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat

class UnlockService : Service() {
    private val overlay: OverlayController
        get() = (application as VocabApp).overlay
    private val handler = Handler(Looper.getMainLooper())
    private var lastPresentAt = 0L
    private var consumed = false
    private val delays = longArrayOf(200L, 700L, 1400L, 2400L)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT,
                Intent.ACTION_USER_UNLOCKED -> scheduleShow()
                Intent.ACTION_SCREEN_ON -> {
                    UnlockService.start(this@UnlockService)
                    val km = getSystemService(KeyguardManager::class.java)
                    if (km != null && !km.isKeyguardLocked) scheduleShow()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    val recentUnlock = SystemClock.elapsedRealtime() - lastPresentAt < 2500
                    if (recentUnlock) return
                    consumed = false
                    cancelShows()
                    overlay.hide()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        overlay.onShown = {
            consumed = true
            cancelShows()
        }
        overlay.onUserDismiss = {
            consumed = true
            cancelShows()
        }
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val n: Notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_app)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.unlock_running))
            .setContentIntent(open)
            .setOngoing(true)
            .setSilent(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
        startForeground(42, n)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_USER_UNLOCKED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Prefs.unlockEnabled(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.getBooleanExtra(EXTRA_SHOW_NOW, false) == true) {
            consumed = false
            scheduleShow()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        cancelShows()
        overlay.onShown = null
        overlay.onUserDismiss = null
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        overlay.hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun scheduleShow() {
        if (!Prefs.unlockEnabled(this)) return
        if (overlay.isShowing() || consumed) return
        lastPresentAt = SystemClock.elapsedRealtime()
        cancelShows()
        for (delay in delays) {
            handler.postDelayed({
                if (consumed || overlay.isShowing()) return@postDelayed
                if (!Prefs.unlockEnabled(this)) return@postDelayed
                overlay.show()
            }, delay)
        }
    }

    private fun cancelShows() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.unlock_channel), NotificationManager.IMPORTANCE_LOW),
        )
    }

    companion object {
        private const val CHANNEL = "unlock"
        private const val EXTRA_SHOW_NOW = "show_now"
        fun start(ctx: Context, showNow: Boolean = false) {
            val i = Intent(ctx, UnlockService::class.java).putExtra(EXTRA_SHOW_NOW, showNow)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i)
                else ctx.startService(i)
            } catch (_: Exception) {
                /* OEM may block background start */
            }
        }
        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, UnlockService::class.java))
        }
        fun sync(ctx: Context) {
            if (Prefs.unlockEnabled(ctx)) start(ctx) else stop(ctx)
        }
    }
}
