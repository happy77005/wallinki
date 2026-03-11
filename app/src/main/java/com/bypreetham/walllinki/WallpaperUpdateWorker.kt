package com.bypreetham.walllinki

import android.app.WallpaperManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WallpaperUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // This is where the "Service Worker" logic lives.
        // It triggers the WallpaperService to update its bitmap.
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        
        // In Android, LiveWallpapers usually update themselves when they become visible.
        // However, we can notify our service to refresh by sending a broadcast 
        // or using another mechanism. For simplicity, we'll use a broadcast.
        
        // Actually, for a LiveWallpaper, the Engine is already running.
        // We can just update the preferences and the next time the Engine draws, it will pick it up.
        // To force an immediate update if the wallpaper is currently visible:
        
        val intent = android.content.Intent("com.bypreetham.walllinki.UPDATE_WALLPAPER")
        applicationContext.sendBroadcast(intent)
        
        return Result.success()
    }
}
