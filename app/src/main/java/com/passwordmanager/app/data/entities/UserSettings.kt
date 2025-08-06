package com.passwordmanager.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户设置实体类
 * 存储用户的个性化设置信息
 */
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey 
    val id: String = "user_settings",
    
    /** 用户昵称 */
    val nickname: String = "用户",
    
    /** 用户头像路径 */
    val avatarPath: String? = null,
    
    /** 自定义背景图片路径 */
    val backgroundPath: String? = null,
    
    /** 背景透明度 (0.0 - 1.0) */
    val backgroundOpacity: Float = 0.3f,
    
    /** 主题模式 */
    val themeMode: String = "light",
    
    /** 是否启用生物识别 */
    val biometricEnabled: Boolean = false,
    
    /** 应用锁定超时时间（分钟） */
    val lockTimeoutMinutes: Int = 5,
    
    /** 是否启用防截屏 */
    val screenSecurityEnabled: Boolean = true,
    
    /** 剪贴板自动清理时间（秒） */
    val clipboardClearSeconds: Int = 5
) {
    companion object {
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
    }
    
    /**
     * 检查背景透明度是否在有效范围内
     */
    fun isValidOpacity(): Boolean {
        return backgroundOpacity in 0.0f..1.0f
    }
}