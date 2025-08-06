package com.passwordmanager.app.utils

import android.app.Activity
import android.view.WindowManager

/**
 * 密码安全管理器
 * 提供应用级别的安全功能
 */
class PasswordSecurityManager {
    
    /**
     * 密码强度枚举
     */
    enum class PasswordStrength {
        WEAK,    // 弱密码
        MEDIUM,  // 中等强度
        STRONG   // 强密码
    }
    
    /**
     * 检测密码强度
     * @param password 要检测的密码
     * @return 密码强度等级
     */
    fun checkPasswordStrength(password: String): PasswordStrength {
        var score = 0
        
        // 长度检查
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        
        // 字符类型检查
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) score++
        
        return when (score) {
            0, 1, 2 -> PasswordStrength.WEAK
            3, 4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
    
    /**
     * 获取密码强度描述
     */
    fun getPasswordStrengthDescription(strength: PasswordStrength): String {
        return when (strength) {
            PasswordStrength.WEAK -> "弱"
            PasswordStrength.MEDIUM -> "中等"
            PasswordStrength.STRONG -> "强"
        }
    }
    
    /**
     * 获取密码强度颜色资源ID
     */
    fun getPasswordStrengthColor(strength: PasswordStrength): Int {
        return when (strength) {
            PasswordStrength.WEAK -> android.R.color.holo_red_light
            PasswordStrength.MEDIUM -> android.R.color.holo_orange_light
            PasswordStrength.STRONG -> android.R.color.holo_green_light
        }
    }
    
    /**
     * 启用屏幕安全（防止截屏和录屏）
     * @param activity 要保护的Activity
     */
    fun enableScreenSecurity(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
    
    /**
     * 禁用屏幕安全
     * @param activity 目标Activity
     */
    fun disableScreenSecurity(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
    
    /**
     * 应用进入后台时隐藏敏感内容
     * @param activity 目标Activity
     */
    fun hideContentInBackground(activity: Activity) {
        // 设置FLAG_SECURE确保应用在最近任务中不显示敏感内容
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
    
    /**
     * 生成密码强度建议
     * @param password 当前密码
     * @return 改进建议列表
     */
    fun getPasswordSuggestions(password: String): List<String> {
        val suggestions = mutableListOf<String>()
        
        if (password.length < 8) {
            suggestions.add("密码长度至少8位")
        }
        
        if (!password.any { it.isUpperCase() }) {
            suggestions.add("添加大写字母")
        }
        
        if (!password.any { it.isLowerCase() }) {
            suggestions.add("添加小写字母")
        }
        
        if (!password.any { it.isDigit() }) {
            suggestions.add("添加数字")
        }
        
        if (!password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) {
            suggestions.add("添加特殊字符")
        }
        
        if (password.length < 12) {
            suggestions.add("建议使用12位以上密码")
        }
        
        return suggestions
    }
    
    /**
     * 检查密码是否包含常见弱密码模式
     * @param password 要检查的密码
     * @return 是否为弱密码
     */
    fun isWeakPassword(password: String): Boolean {
        val weakPatterns = listOf(
            "123456", "password", "123456789", "12345678",
            "12345", "1234567", "admin", "qwerty",
            "abc123", "password123", "123123", "111111"
        )
        
        val lowerPassword = password.lowercase()
        return weakPatterns.any { lowerPassword.contains(it) }
    }
    
    /**
     * 检查密码是否有重复字符模式
     * @param password 要检查的密码
     * @return 是否有重复模式
     */
    fun hasRepeatingPattern(password: String): Boolean {
        if (password.length < 3) return false
        
        // 检查连续重复字符
        for (i in 0 until password.length - 2) {
            if (password[i] == password[i + 1] && password[i + 1] == password[i + 2]) {
                return true
            }
        }
        
        // 检查简单的重复模式
        for (i in 1 until password.length / 2) {
            val pattern = password.substring(0, i)
            val repeated = pattern.repeat(password.length / i)
            if (password.startsWith(repeated)) {
                return true
            }
        }
        
        return false
    }
}