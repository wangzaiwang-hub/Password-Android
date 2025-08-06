package com.passwordmanager.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 账号历史记录实体类
 * 用于智能建议功能，记录用户常用的账号
 */
@Entity(tableName = "account_history")
data class AccountHistory(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    /** 账号信息 */
    val account: String,
    
    /** 使用次数 */
    val usageCount: Int = 1,
    
    /** 最后使用时间 */
    val lastUsed: Long = System.currentTimeMillis(),
    
    /** 账号类型（邮箱、手机号等） */
    val accountType: String = detectAccountType(account)
) {
    companion object {
        const val TYPE_EMAIL = "email"
        const val TYPE_PHONE = "phone"
        const val TYPE_USERNAME = "username"
        const val TYPE_OTHER = "other"
        
        /**
         * 检测账号类型
         */
        private fun detectAccountType(account: String): String {
            return when {
                account.contains("@") && account.contains(".") -> TYPE_EMAIL
                account.matches(Regex("^1[3-9]\\d{9}$")) -> TYPE_PHONE
                account.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]{2,15}$")) -> TYPE_USERNAME
                else -> TYPE_OTHER
            }
        }
    }
    
    /**
     * 增加使用次数
     */
    fun incrementUsage(): AccountHistory {
        return this.copy(
            usageCount = usageCount + 1,
            lastUsed = System.currentTimeMillis()
        )
    }
    
    /**
     * 检查是否为邮箱账号
     */
    fun isEmail(): Boolean = accountType == TYPE_EMAIL
    
    /**
     * 检查是否为手机号账号
     */
    fun isPhone(): Boolean = accountType == TYPE_PHONE
}