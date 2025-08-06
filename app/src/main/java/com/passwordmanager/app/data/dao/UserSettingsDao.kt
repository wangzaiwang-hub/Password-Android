package com.passwordmanager.app.data.dao

import androidx.room.*
import com.passwordmanager.app.data.entities.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * 用户设置数据访问对象
 * 提供用户设置相关的数据库操作
 */
@Dao
interface UserSettingsDao {
    
    /**
     * 获取用户设置
     */
    @Query("SELECT * FROM user_settings WHERE id = 'user_settings' LIMIT 1")
    fun getUserSettings(): Flow<UserSettings?>
    
    /**
     * 获取用户设置（同步方法）
     */
    @Query("SELECT * FROM user_settings WHERE id = 'user_settings' LIMIT 1")
    suspend fun getUserSettingsSync(): UserSettings?
    
    /**
     * 插入或更新用户设置
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserSettings(userSettings: UserSettings)
    
    /**
     * 更新用户昵称
     */
    @Query("UPDATE user_settings SET nickname = :nickname WHERE id = 'user_settings'")
    suspend fun updateNickname(nickname: String)
    
    /**
     * 更新用户头像路径
     */
    @Query("UPDATE user_settings SET avatarPath = :avatarPath WHERE id = 'user_settings'")
    suspend fun updateAvatarPath(avatarPath: String?)
    
    /**
     * 更新背景设置
     */
    @Query("UPDATE user_settings SET backgroundPath = :backgroundPath, backgroundOpacity = :opacity WHERE id = 'user_settings'")
    suspend fun updateBackground(backgroundPath: String?, opacity: Float)
    
    /**
     * 更新主题模式
     */
    @Query("UPDATE user_settings SET themeMode = :themeMode WHERE id = 'user_settings'")
    suspend fun updateThemeMode(themeMode: String)
    
    /**
     * 更新生物识别设置
     */
    @Query("UPDATE user_settings SET biometricEnabled = :enabled WHERE id = 'user_settings'")
    suspend fun updateBiometricEnabled(enabled: Boolean)
    
    /**
     * 更新锁定超时时间
     */
    @Query("UPDATE user_settings SET lockTimeoutMinutes = :minutes WHERE id = 'user_settings'")
    suspend fun updateLockTimeout(minutes: Int)
    
    /**
     * 更新防截屏设置
     */
    @Query("UPDATE user_settings SET screenSecurityEnabled = :enabled WHERE id = 'user_settings'")
    suspend fun updateScreenSecurity(enabled: Boolean)
    
    /**
     * 更新剪贴板清理时间
     */
    @Query("UPDATE user_settings SET clipboardClearSeconds = :seconds WHERE id = 'user_settings'")
    suspend fun updateClipboardClearTime(seconds: Int)
    
    /**
     * 重置用户设置为默认值
     */
    @Query("DELETE FROM user_settings WHERE id = 'user_settings'")
    suspend fun resetUserSettings()
    
    /**
     * 检查用户设置是否存在
     */
    @Query("SELECT COUNT(*) > 0 FROM user_settings WHERE id = 'user_settings'")
    suspend fun isUserSettingsExists(): Boolean
}