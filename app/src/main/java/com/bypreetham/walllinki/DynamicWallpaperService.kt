package com.bypreetham.walllinki

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class DynamicWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {
        private val prefs = WallpaperPreferences(applicationContext)
        private var currentBitmap: Bitmap? = null
        private var lastSlotId: String? = null

        private val updateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                draw()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            val filter = IntentFilter("com.bypreetham.walllinki.UPDATE_WALLPAPER")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this@DynamicWallpaperService.registerReceiver(updateReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                this@DynamicWallpaperService.registerReceiver(updateReceiver, filter)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                draw()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            draw()
        }

        override fun onDestroy() {
            super.onDestroy()
            try {
                unregisterReceiver(updateReceiver)
            } catch (e: Exception) {
            }
            currentBitmap?.recycle()
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    val activeSlot = prefs.getActiveSlot()
                    
                    if (activeSlot?.id != lastSlotId || currentBitmap == null) {
                        lastSlotId = activeSlot?.id
                        updateWallpaperBitmap(activeSlot)
                    }

                    currentBitmap?.let { bitmap ->
                        drawBitmapCentered(canvas, bitmap)
                    } ?: run {
                        canvas.drawColor(android.graphics.Color.BLACK)
                    }
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
        }

        private fun updateWallpaperBitmap(slot: TimeSlot?) {
            val images = slot?.imageUris ?: emptyList()
            if (images.isNotEmpty()) {
                val uri = images.random()
                loadBitmap(uri)
            } else {
                currentBitmap?.recycle()
                currentBitmap = null
            }
        }

        private fun loadBitmap(uri: Uri) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val newBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                currentBitmap?.recycle()
                currentBitmap = newBitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun drawBitmapCentered(canvas: Canvas, bitmap: Bitmap) {
            val canvasWidth = canvas.width.toFloat()
            val canvasHeight = canvas.height.toFloat()
            val bitmapWidth = bitmap.width.toFloat()
            val bitmapHeight = bitmap.height.toFloat()

            val scale = Math.max(canvasWidth / bitmapWidth, canvasHeight / bitmapHeight)
            val dx = (canvasWidth - bitmapWidth * scale) / 2f
            val dy = (canvasHeight - bitmapHeight * scale) / 2f

            val paint = Paint().apply {
                isFilterBitmap = true
                isAntiAlias = true
            }

            canvas.save()
            canvas.translate(dx, dy)
            canvas.scale(scale, scale)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            canvas.restore()
        }
    }
}
