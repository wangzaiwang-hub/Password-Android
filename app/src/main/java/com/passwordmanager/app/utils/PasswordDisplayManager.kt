package com.passwordmanager.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.passwordmanager.app.R
import com.passwordmanager.app.services.EncryptionService
import kotlinx.coroutines.Job

/**
 * 密码显示管理器
 * 管理密码的安全显示、隐藏和复制功能
 */
class PasswordDisplayManager(
    private val context: Context,
    private val encryptionService: EncryptionService
) {
    
    companion object {
        private const val MASKED_PASSWORD = "●●●●●●●●"
        private const val CLIPBOARD_CLEAR_DELAY = 5000L // 5秒后清空剪贴板
    }
    
    private var isPasswordVisible = false
    private var clipboardClearJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())
    private var clipboardClearRunnable: Runnable? = null
    
    /**
     * 切换密码可见性
     * @param passwordTextView 显示密码的TextView
     * @param visibilityIcon 可见性切换图标
     * @param encryptedPassword 加密的密码
     */
    fun togglePasswordVisibility(
        passwordTextView: TextView,
        visibilityIcon: ImageView,
        encryptedPassword: String
    ) {
        if (isPasswordVisible) {
            hidePassword(passwordTextView, visibilityIcon)
        } else {
            showPassword(passwordTextView, visibilityIcon, encryptedPassword)
        }
    }
    
    /**
     * 显示密码明文
     */
    private fun showPassword(
        passwordTextView: TextView,
        visibilityIcon: ImageView,
        encryptedPassword: String
    ) {
        try {
            val plainPassword = encryptionService.decrypt(encryptedPassword)
            passwordTextView.text = plainPassword
            visibilityIcon.setImageResource(R.drawable.ic_visibility)
            isPasswordVisible = true
            
            // 设置长按复制功能
            passwordTextView.setOnLongClickListener {
                copyPasswordToClipboard(plainPassword)
                true
            }
            
        } catch (e: Exception) {
            Toast.makeText(context, "密码解密失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 隐藏密码，显示掩码
     */
    private fun hidePassword(
        passwordTextView: TextView,
        visibilityIcon: ImageView
    ) {
        passwordTextView.text = MASKED_PASSWORD
        visibilityIcon.setImageResource(R.drawable.ic_visibility_off)
        isPasswordVisible = false
        
        // 移除长按复制功能
        passwordTextView.setOnLongClickListener(null)
    }
    
    /**
     * 复制密码到剪贴板
     * @param password 要复制的密码
     */
    private fun copyPasswordToClipboard(password: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("password", password)
        clipboard.setPrimaryClip(clip)
        
        // 显示复制成功提示
        Toast.makeText(context, context.getString(R.string.copy_success), Toast.LENGTH_SHORT).show()
        
        // 安全考虑：延迟清空剪贴板
        scheduleClipboardClear(clipboard)
    }
    
    /**
     * 安排剪贴板清理任务
     */
    private fun scheduleClipboardClear(clipboard: ClipboardManager) {
        // 取消之前的清理任务
        clipboardClearRunnable?.let { handler.removeCallbacks(it) }
        
        // 创建新的清理任务
        clipboardClearRunnable = Runnable {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
        
        // 5秒后执行清理
        clipboardClearRunnable?.let { 
            handler.postDelayed(it, CLIPBOARD_CLEAR_DELAY)
        }
    }
    
    /**
     * 获取掩码密码
     */
    fun getMaskedPassword(): String = MASKED_PASSWORD
    
    /**
     * 检查密码是否可见
     */
    fun isPasswordVisible(): Boolean = isPasswordVisible
    
    /**
     * 强制隐藏密码（用于页面切换等场景）
     */
    fun forceHidePassword(
        passwordTextView: TextView,
        visibilityIcon: ImageView
    ) {
        if (isPasswordVisible) {
            hidePassword(passwordTextView, visibilityIcon)
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        clipboardClearRunnable?.let { handler.removeCallbacks(it) }
        clipboardClearRunnable = null
    }
}