package com.passwordmanager.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.graphics.drawable.toDrawable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 背景管理器
 * 处理自定义背景的选择、保存和应用
 */
class BackgroundManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "background_settings"
        private const val KEY_BACKGROUND_PATH = "background_path"
        private const val KEY_BACKGROUND_OPACITY = "background_opacity"
        private const val BACKGROUND_FILE_NAME = "custom_background.jpg"
        private const val DEFAULT_OPACITY = 0.3f
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * 保存自定义背景
     */
    fun saveCustomBackground(imageUri: Uri, opacity: Float = DEFAULT_OPACITY): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                // 压缩并保存图片
                val backgroundFile = File(context.filesDir, BACKGROUND_FILE_NAME)
                val outputStream = FileOutputStream(backgroundFile)
                
                // 压缩图片以节省空间
                val scaledBitmap = scaleBitmap(bitmap, 1080, 1920)
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.close()
                
                // 保存设置
                prefs.edit()
                    .putString(KEY_BACKGROUND_PATH, backgroundFile.absolutePath)
                    .putFloat(KEY_BACKGROUND_OPACITY, opacity)
                    .apply()
                
                bitmap.recycle()
                if (scaledBitmap != bitmap) {
                    scaledBitmap.recycle()
                }
                
                true
            } else {
                false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取自定义背景
     */
    fun getCustomBackground(): Drawable? {
        val backgroundPath = prefs.getString(KEY_BACKGROUND_PATH, null) ?: return null
        val backgroundFile = File(backgroundPath)
        
        return if (backgroundFile.exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(backgroundPath)
                val opacity = prefs.getFloat(KEY_BACKGROUND_OPACITY, DEFAULT_OPACITY)
                
                val drawable = BitmapDrawable(context.resources, bitmap)
                drawable.alpha = (opacity * 255).toInt()
                drawable
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
    
    /**
     * 获取背景透明度
     */
    fun getBackgroundOpacity(): Float {
        return prefs.getFloat(KEY_BACKGROUND_OPACITY, DEFAULT_OPACITY)
    }
    
    /**
     * 设置背景透明度
     */
    fun setBackgroundOpacity(opacity: Float) {
        prefs.edit()
            .putFloat(KEY_BACKGROUND_OPACITY, opacity.coerceIn(0.1f, 1.0f))
            .apply()
    }
    
    /**
     * 是否有自定义背景
     */
    fun hasCustomBackground(): Boolean {
        val backgroundPath = prefs.getString(KEY_BACKGROUND_PATH, null)
        return backgroundPath != null && File(backgroundPath).exists()
    }
    
    /**
     * 清除自定义背景
     */
    fun clearCustomBackground(): Boolean {
        return try {
            val backgroundPath = prefs.getString(KEY_BACKGROUND_PATH, null)
            if (backgroundPath != null) {
                val backgroundFile = File(backgroundPath)
                if (backgroundFile.exists()) {
                    backgroundFile.delete()
                }
            }
            
            prefs.edit()
                .remove(KEY_BACKGROUND_PATH)
                .remove(KEY_BACKGROUND_OPACITY)
                .apply()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 缩放位图
     */
    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = minOf(scaleWidth, scaleHeight)
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * 获取背景预览
     */
    fun getBackgroundPreview(): Bitmap? {
        val backgroundPath = prefs.getString(KEY_BACKGROUND_PATH, null) ?: return null
        val backgroundFile = File(backgroundPath)
        
        return if (backgroundFile.exists()) {
            try {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 4 // 缩小4倍用于预览
                }
                BitmapFactory.decodeFile(backgroundPath, options)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
}