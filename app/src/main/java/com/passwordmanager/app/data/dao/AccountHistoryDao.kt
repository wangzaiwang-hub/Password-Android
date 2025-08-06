package com.passwordmanager.app.data.dao

import androidx.room.*
import com.passwordmanager.app.data.entities.AccountHistory
import kotlinx.coroutines.flow.Flow

/**
 * 账号历史记录数据访问对象
 * 提供账号历史相关的数据库操作，用于智能建议功能
 */
@Dao
interface AccountHistoryDao {
    
    /**
     * 获取所有账号历史记录
     */
    @Query("SELECT * FROM account_history ORDER BY usageCount DESC, lastUsed DESC")
    fun getAllAccountHistory(): Flow<List<AccountHistory>>
    
    /**
     * 获取常用账号（按使用次数排序）
     */
    @Query("SELECT * FROM account_history ORDER BY usageCount DESC, lastUsed DESC LIMIT :limit")
    fun getFrequentAccounts(limit: Int = 10): Flow<List<AccountHistory>>
    
    /**
     * 根据输入内容获取匹配的账号建议
     */
    @Query("""
        SELECT * FROM account_history 
        WHERE account LIKE :input || '%' 
        ORDER BY usageCount DESC, lastUsed DESC 
        LIMIT :limit
    """)
    suspend fun getAccountSuggestions(input: String, limit: Int = 5): List<AccountHistory>
    
    /**
     * 根据账号类型获取历史记录
     */
    @Query("SELECT * FROM account_history WHERE accountType = :type ORDER BY usageCount DESC, lastUsed DESC")
    fun getAccountsByType(type: String): Flow<List<AccountHistory>>
    
    /**
     * 获取邮箱类型的账号
     */
    @Query("SELECT * FROM account_history WHERE accountType = 'email' ORDER BY usageCount DESC, lastUsed DESC")
    fun getEmailAccounts(): Flow<List<AccountHistory>>
    
    /**
     * 获取手机号类型的账号
     */
    @Query("SELECT * FROM account_history WHERE accountType = 'phone' ORDER BY usageCount DESC, lastUsed DESC")
    fun getPhoneAccounts(): Flow<List<AccountHistory>>
    
    /**
     * 根据账号获取历史记录
     */
    @Query("SELECT * FROM account_history WHERE account = :account LIMIT 1")
    suspend fun getAccountHistory(account: String): AccountHistory?
    
    /**
     * 插入或更新账号历史记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAccountHistory(accountHistory: AccountHistory)
    
    /**
     * 增加账号使用次数
     */
    @Query("""
        UPDATE account_history 
        SET usageCount = usageCount + 1, lastUsed = :currentTime 
        WHERE account = :account
    """)
    suspend fun incrementUsageCount(account: String, currentTime: Long = System.currentTimeMillis())
    
    /**
     * 删除账号历史记录
     */
    @Delete
    suspend fun deleteAccountHistory(accountHistory: AccountHistory)
    
    /**
     * 根据账号删除历史记录
     */
    @Query("DELETE FROM account_history WHERE account = :account")
    suspend fun deleteAccountHistoryByAccount(account: String)
    
    /**
     * 清理长时间未使用的账号历史（超过指定天数）
     */
    @Query("DELETE FROM account_history WHERE lastUsed < :cutoffTime")
    suspend fun cleanupOldHistory(cutoffTime: Long)
    
    /**
     * 删除所有账号历史记录
     */
    @Query("DELETE FROM account_history")
    suspend fun deleteAllAccountHistory()
    
    /**
     * 获取账号历史记录总数
     */
    @Query("SELECT COUNT(*) FROM account_history")
    suspend fun getAccountHistoryCount(): Int
    
    /**
     * 记录账号使用（如果不存在则创建，存在则增加使用次数）
     */
    @Transaction
    suspend fun recordAccountUsage(account: String) {
        val existing = getAccountHistory(account)
        if (existing != null) {
            incrementUsageCount(account)
        } else {
            insertOrUpdateAccountHistory(AccountHistory(account = account))
        }
    }
}