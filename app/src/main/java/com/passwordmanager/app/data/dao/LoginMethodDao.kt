package com.passwordmanager.app.data.dao

import androidx.room.*
import com.passwordmanager.app.data.entities.LoginMethod
import kotlinx.coroutines.flow.Flow

/**
 * 登录方式数据访问对象
 * 提供登录方式相关的数据库操作
 */
@Dao
interface LoginMethodDao {
    
    /**
     * 获取所有登录方式
     */
    @Query("SELECT * FROM login_methods ORDER BY isDefault DESC, createdAt ASC")
    fun getAllLoginMethods(): Flow<List<LoginMethod>>
    
    /**
     * 获取默认登录方式
     */
    @Query("SELECT * FROM login_methods WHERE isDefault = 1 ORDER BY createdAt ASC")
    fun getDefaultLoginMethods(): Flow<List<LoginMethod>>
    
    /**
     * 获取自定义登录方式
     */
    @Query("SELECT * FROM login_methods WHERE isDefault = 0 ORDER BY createdAt DESC")
    fun getCustomLoginMethods(): Flow<List<LoginMethod>>
    
    /**
     * 根据名称搜索登录方式
     */
    @Query("SELECT * FROM login_methods WHERE name LIKE '%' || :name || '%' ORDER BY isDefault DESC, createdAt ASC")
    fun searchLoginMethods(name: String): Flow<List<LoginMethod>>
    
    /**
     * 根据ID获取登录方式
     */
    @Query("SELECT * FROM login_methods WHERE id = :id")
    suspend fun getLoginMethodById(id: String): LoginMethod?
    
    /**
     * 根据名称获取登录方式
     */
    @Query("SELECT * FROM login_methods WHERE name = :name LIMIT 1")
    suspend fun getLoginMethodByName(name: String): LoginMethod?
    
    /**
     * 插入登录方式
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoginMethod(loginMethod: LoginMethod)
    
    /**
     * 批量插入登录方式
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLoginMethods(loginMethods: List<LoginMethod>)
    
    /**
     * 更新登录方式
     */
    @Update
    suspend fun updateLoginMethod(loginMethod: LoginMethod)
    
    /**
     * 删除登录方式
     */
    @Delete
    suspend fun deleteLoginMethod(loginMethod: LoginMethod)
    
    /**
     * 根据ID删除登录方式
     */
    @Query("DELETE FROM login_methods WHERE id = :id")
    suspend fun deleteLoginMethodById(id: String)
    
    /**
     * 删除自定义登录方式（保留默认的）
     */
    @Query("DELETE FROM login_methods WHERE isDefault = 0")
    suspend fun deleteCustomLoginMethods()
    
    /**
     * 检查登录方式是否存在
     */
    @Query("SELECT COUNT(*) > 0 FROM login_methods WHERE name = :name")
    suspend fun isLoginMethodExists(name: String): Boolean
    
    /**
     * 获取登录方式总数
     */
    @Query("SELECT COUNT(*) FROM login_methods")
    suspend fun getLoginMethodCount(): Int
}