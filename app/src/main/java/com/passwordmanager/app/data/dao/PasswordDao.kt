package com.passwordmanager.app.data.dao

import androidx.room.*
import com.passwordmanager.app.data.entities.PasswordEntry
import kotlinx.coroutines.flow.Flow

/**
 * 密码数据访问对象
 * 提供密码相关的数据库操作
 */
@Dao
interface PasswordDao {
    
    /**
     * 获取所有密码条目
     */
    @Query("SELECT * FROM password_entries ORDER BY updatedAt DESC")
    fun getAllPasswords(): Flow<List<PasswordEntry>>
    
    /**
     * 根据ID获取密码条目
     */
    @Query("SELECT * FROM password_entries WHERE id = :id")
    suspend fun getPasswordById(id: String): PasswordEntry?
    
    /**
     * 搜索密码条目
     * 支持按平台名称、登录方式、账号搜索
     */
    @Query("""
        SELECT * FROM password_entries 
        WHERE platform LIKE '%' || :query || '%' 
        OR loginMethod LIKE '%' || :query || '%' 
        OR account LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchPasswords(query: String): Flow<List<PasswordEntry>>
    
    /**
     * 按平台搜索
     */
    @Query("SELECT * FROM password_entries WHERE platform LIKE '%' || :platform || '%' ORDER BY updatedAt DESC")
    fun searchByPlatform(platform: String): Flow<List<PasswordEntry>>
    
    /**
     * 按登录方式搜索
     */
    @Query("SELECT * FROM password_entries WHERE loginMethod LIKE '%' || :loginMethod || '%' ORDER BY updatedAt DESC")
    fun searchByLoginMethod(loginMethod: String): Flow<List<PasswordEntry>>
    
    /**
     * 按账号搜索
     */
    @Query("SELECT * FROM password_entries WHERE account LIKE '%' || :account || '%' ORDER BY updatedAt DESC")
    fun searchByAccount(account: String): Flow<List<PasswordEntry>>
    
    /**
     * 获取最近添加的密码条目
     */
    @Query("SELECT * FROM password_entries ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentlyAdded(limit: Int = 10): Flow<List<PasswordEntry>>
    
    /**
     * 获取常用的密码条目（按更新时间排序，表示使用频率）
     */
    @Query("SELECT * FROM password_entries ORDER BY updatedAt DESC LIMIT :limit")
    fun getFrequentlyUsed(limit: Int = 10): Flow<List<PasswordEntry>>
    
    /**
     * 插入密码条目
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: PasswordEntry)
    
    /**
     * 更新密码条目
     */
    @Update
    suspend fun updatePassword(password: PasswordEntry)
    
    /**
     * 删除密码条目
     */
    @Delete
    suspend fun deletePassword(password: PasswordEntry)
    
    /**
     * 根据ID删除密码条目
     */
    @Query("DELETE FROM password_entries WHERE id = :id")
    suspend fun deletePasswordById(id: String)
    
    /**
     * 删除所有密码条目
     */
    @Query("DELETE FROM password_entries")
    suspend fun deleteAllPasswords()
    
    /**
     * 获取密码条目总数
     */
    @Query("SELECT COUNT(*) FROM password_entries")
    suspend fun getPasswordCount(): Int
    
    /**
     * 获取指定平台的密码条目数量
     */
    @Query("SELECT COUNT(*) FROM password_entries WHERE platform = :platform")
    suspend fun getPasswordCountByPlatform(platform: String): Int
}