package com.potatokkk.jpvocab

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

object Permissions {
    fun hasOverlay(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(ctx)

    fun hasBatteryExemption(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val pm = ctx.getSystemService(PowerManager::class.java) ?: return true
        return pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }

    fun openOverlaySettings(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}"),
        )
        activity.startActivity(intent)
    }

    fun openBatterySettings(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:${activity.packageName}")
            activity.startActivity(intent)
        } catch (_: Exception) {
            try {
                activity.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (_: Exception) {
                /* ignore */
            }
        }
    }

    fun openAutostartSettings(activity: Activity) {
        val pkg = activity.packageName
        val candidates = listOf(
            Intent("miui.intent.action.APP_PERM_EDITOR").putExtra("extra_pkgname", pkg),
            Intent().setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity",
            ),
            Intent().setClassName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
            ),
            Intent().setClassName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity",
            ),
            Intent().setClassName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity",
            ),
            Intent().setClassName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
            ),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$pkg")
            },
        )
        for (intent in candidates) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                    return
                }
            } catch (_: Exception) {
                /* try next OEM screen */
            }
        }
    }

    fun stateJson(ctx: Context): String {
        val overlay = hasOverlay(ctx)
        val battery = hasBatteryExemption(ctx)
        val unlock = Prefs.unlockEnabled(ctx)
        return """{"overlay":$overlay,"battery":$battery,"unlock":$unlock}"""
    }
}
