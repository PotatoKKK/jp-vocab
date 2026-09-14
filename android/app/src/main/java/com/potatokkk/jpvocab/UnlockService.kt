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
import androidx.core.app.NotificationCompat

class UnlockService : Service() {
    private val overlay: OverlayController
        get() = (application as VocabApp).overlay
    private val handler = Handler(Looper.getMainLooper())
    private val showRunnable = Runnable {
        if (Prefs.unlockEnabled(this)) overlay.show()
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> scheduleShow()
                Intent.ACTION_SCREEN_ON -> {
                    val km = getSystemService(KeyguardManager::class.java)
                    if (km != null && !km.isKeyguardLocked) scheduleShow()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    handler.removeCallbacks(showRunnable)
                    overlay.hide()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
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
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
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
            scheduleShow()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(showRunnable)
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        overlay.hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun scheduleShow() {
        if (!Prefs.unlockEnabled(this)) return
        handler.removeCallbacks(showRunnable)
        handler.postDelayed(showRunnable, 480)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i)
            else ctx.startService(i)
        }
        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, UnlockService::class.java))
        }
        fun sync(ctx: Context) {
            if (Prefs.unlockEnabled(ctx)) start(ctx) else stop(ctx)
        }
    }
}
