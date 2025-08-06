package com.passwordmanager.app.data.repository

import com.passwordmanager.app.data.dao.PasswordDao
import com.passwordmanager.app.data.dao.AccountHistoryDao
import com.passwordmanager.app.data.entities.PasswordEntry
import com.passwordmanager.app.services.EncryptionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 密码仓库类
 * 整合数据库操作和加密服务，提供密码相关的业务逻辑
 */
class PasswordRepository(
    private val passwordDao: PasswordDao,
    private val accountHistoryDao: AccountHistoryDao,
    private val encryptionService: EncryptionService
) {
    
    /**
     * 保存密码条目
     * @param entry 密码条目（密码将被加密）
     * @return 操作结果
     */
    suspend fun savePassword(entry: PasswordEntry): Result<Unit> {
        return try {
            // 加密密码
            val encryptedPassword = encryptionService.encrypt(entry.encryptedPassword)
            val encryptedEntry = entry.copy(encryptedPassword = encryptedPassword)
            
            // 保存到数据库
            passwordDao.insertPassword(encryptedEntry)
            
            // 记录账号使用历史
            accountHistoryDao.recordAccountUsage(entry.account)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新密码条目
     * @param entry 更新的密码条目
     * @return 操作结果
     */
    suspend fun updatePassword(entry: PasswordEntry): Result<Unit> {
        return try {
            // 如果密码字段不是已加密的，则进行加密
            val encryptedEntry = if (isPasswordEncrypted(entry.encryptedPassword)) {
                entry
            } else {
                val encryptedPassword = encryptionService.encrypt(entry.encryptedPassword)
                entry.copy(
                    encryptedPassword = encryptedPassword,
                    updatedAt = System.currentTimeMillis()
                )
            }
            
            passwordDao.updatePassword(encryptedEntry)
            
            // 更新账号使用历史
            accountHistoryDao.recordAccountUsage(entry.account)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有密码条目
     * @return 密码条目流
     */
    fun getAllPasswords(): Flow<List<PasswordEntry>> {
        return passwordDao.getAllPasswords()
    }
    
    /**
     * 根据ID获取密码条目
     * @param id 密码条目ID
     * @return 密码条目（如果存在）
     */
    suspend fun getPasswordById(id: String): PasswordEntry? {
        return passwordDao.getPasswordById(id)
    }
    
    /**
     * 搜索密码条目
     * @param query 搜索关键词
     * @return 匹配的密码条目流
     */
    fun searchPasswords(query: String): Flow<List<PasswordEntry>> {
        return if (query.isBlank()) {
            getAllPasswords()
        } else {
            passwordDao.searchPasswords(query)
        }
    }
    
    /**
     * 按平台搜索密码
     * @param platform 平台名称
     * @return 匹配的密码条目流
     */
    fun searchByPlatform(platform: String): Flow<List<PasswordEntry>> {
        return passwordDao.searchByPlatform(platform)
    }
    
    /**
     * 按登录方式搜索密码
     * @param loginMethod 登录方式
     * @return 匹配的密码条目流
     */
    fun searchByLoginMethod(loginMethod: String): Flow<List<PasswordEntry>> {
        return passwordDao.searchByLoginMethod(loginMethod)
    }
    
    /**
     * 按账号搜索密码
     * @param account 账号
     * @return 匹配的密码条目流
     */
    fun searchByAccount(account: String): Flow<List<PasswordEntry>> {
        return passwordDao.searchByAccount(account)
    }
    
    /**
     * 获取最近添加的密码条目
     * @param limit 限制数量
     * @return 最近添加的密码条目流
     */
    fun getRecentlyAdded(limit: Int = 10): Flow<List<PasswordEntry>> {
        return passwordDao.getRecentlyAdded(limit)
    }
    
    /**
     * 获取常用的密码条目
     * @param limit 限制数量
     * @return 常用密码条目流
     */
    fun getFrequentlyUsed(limit: Int = 10): Flow<List<PasswordEntry>> {
        return passwordDao.getFrequentlyUsed(limit)
    }
    
    /**
     * 删除密码条目
     * @param entry 要删除的密码条目
     * @return 操作结果
     */
    suspend fun deletePassword(entry: PasswordEntry): Result<Unit> {
        return try {
            passwordDao.deletePassword(entry)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据ID删除密码条目
     * @param id 密码条目ID
     * @return 操作结果
     */
    suspend fun deletePasswordById(id: String): Result<Unit> {
        return try {
            passwordDao.deletePasswordById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取密码条目总数
     * @return 密码条目数量
     */
    suspend fun getPasswordCount(): Int {
        return passwordDao.getPasswordCount()
    }
    
    /**
     * 获取指定平台的密码条目数量
     * @param platform 平台名称
     * @return 该平台的密码条目数量
     */
    suspend fun getPasswordCountByPlatform(platform: String): Int {
        return passwordDao.getPasswordCountByPlatform(platform)
    }
    
    /**
     * 解密密码条目的密码字段
     * @param entry 密码条目
     * @return 解密后的明文密码
     */
    suspend fun decryptPassword(entry: PasswordEntry): Result<String> {
        return try {
            val plainPassword = encryptionService.decrypt(entry.encryptedPassword)
            Result.success(plainPassword)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量导出密码数据（用于备份）
     * @return 所有密码条目的解密版本
     */
    suspend fun exportPasswords(): Result<List<PasswordEntry>> {
        return try {
            val encryptedPasswords = passwordDao.getAllPasswords()
            // 注意：这里返回的是Flow，需要在调用处收集
            Result.success(emptyList()) // 实际实现需要收集Flow数据
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 检查密码是否已加密
     * @param password 密码字符串
     * @return 是否已加密
     */
    private fun isPasswordEncrypted(password: String): Boolean {
        // 简单检查：加密后的密码包含冒号分隔符
        return password.contains(":")
    }
    
    /**
     * 清空所有密码数据
     * @return 操作结果
     */
    suspend fun clearAllPasswords(): Result<Unit> {
        return try {
            passwordDao.deleteAllPasswords()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}