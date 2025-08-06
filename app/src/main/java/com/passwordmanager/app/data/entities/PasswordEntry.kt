package com.passwordmanager.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.UUID

/**
 * 密码条目实体类
 * 存储用户的密码信息，密码字段加密存储
 */
@Entity(tableName = "password_entries")
data class PasswordEntry(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    /** 平台名称 */
    val platform: String,
    
    /** 登录方式 */
    val loginMethod: String,
    
    /** 绑定账号 */
    val account: String,
    
    /** 加密后的密码，绝不明文保存 */
    val encryptedPassword: String,
    
    /** 平台图标路径 */
    val iconPath: String? = null,
    
    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis(),
    
    /** 更新时间 */
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    /**
     * 获取用于UI显示的掩码密码
     * @return 掩码字符串
     */
    fun getMaskedPassword(): String = "●".repeat(8)
    
    /**
     * 检查是否为空密码条目
     */
    fun isEmpty(): Boolean {
        return platform.isBlank() || account.isBlank() || encryptedPassword.isBlank()
    }
}