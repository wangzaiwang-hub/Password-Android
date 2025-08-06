package com.passwordmanager.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.passwordmanager.app.data.database.PasswordManagerDatabase
import com.passwordmanager.app.data.entities.LoginMethod
import com.passwordmanager.app.data.entities.PasswordEntry
import com.passwordmanager.app.services.EncryptionService
import com.passwordmanager.app.utils.AccountSuggestionProvider
import com.passwordmanager.app.utils.PasswordSecurityManager
import com.passwordmanager.app.utils.PlatformIconMatcher
import kotlinx.coroutines.launch

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = PasswordManagerDatabase.getDatabase(application)
    private val encryptionService = EncryptionService()
    private val passwordSecurityManager = PasswordSecurityManager()
    private val accountSuggestionProvider = AccountSuggestionProvider(database.accountHistoryDao())
    private val platformIconMatcher = PlatformIconMatcher()
    
    // 登录方式列表
    private val _loginMethods = MutableLiveData<List<LoginMethod>>()
    val loginMethods: LiveData<List<LoginMethod>> = _loginMethods
    
    // 账号建议列表
    private val _accountSuggestions = MutableLiveData<List<String>>()
    val accountSuggestions: LiveData<List<String>> = _accountSuggestions
    
    // 密码强度
    private val _passwordStrength = MutableLiveData<Int>()
    val passwordStrength: LiveData<Int> = _passwordStrength
    
    // 密码强度文本
    private val _passwordStrengthText = MutableLiveData<String>()
    val passwordStrengthText: LiveData<String> = _passwordStrengthText
    
    // 平台图标资源ID
    private val _platformIconRes = MutableLiveData<Int>()
    val platformIconRes: LiveData<Int> = _platformIconRes
    
    // 保存状态
    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult
    
    init {
        loadLoginMethods()
    }
    
    /**
     * 加载登录方式列表
     */
    private fun loadLoginMethods() {
        // 简化实现，直接提供默认登录方式
        createDefaultLoginMethods()
    }
    
    /**
     * 创建默认登录方式
     */
    private fun createDefaultLoginMethods() {
        val defaultMethods = listOf(
            LoginMethod(name = "手机号", isDefault = true),
            LoginMethod(name = "邮箱", isDefault = true),
            LoginMethod(name = "用户名", isDefault = true),
            LoginMethod(name = "微信", isDefault = true),
            LoginMethod(name = "QQ", isDefault = true),
            LoginMethod(name = "微博", isDefault = true)
        )
        
        _loginMethods.value = defaultMethods
    }
    
    /**
     * 添加自定义登录方式
     */
    fun addCustomLoginMethod(methodName: String) {
        if (methodName.isBlank()) return
        
        viewModelScope.launch {
            val newMethod = LoginMethod(name = methodName, isDefault = false)
            database.loginMethodDao().insertLoginMethod(newMethod)
            loadLoginMethods()
        }
    }
    
    /**
     * 删除自定义登录方式
     */
    fun deleteCustomLoginMethod(method: LoginMethod) {
        if (method.isDefault) return
        
        viewModelScope.launch {
            database.loginMethodDao().deleteLoginMethod(method)
            loadLoginMethods()
        }
    }
    
    /**
     * 根据平台名称获取账号建议
     */
    fun getAccountSuggestions(platformName: String) {
        if (platformName.isBlank()) {
            _accountSuggestions.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            val suggestions = accountSuggestionProvider.getSuggestions(platformName)
            _accountSuggestions.value = suggestions
        }
    }
    
    /**
     * 根据平台名称匹配图标
     */
    fun matchPlatformIcon(platformName: String) {
        val iconRes = platformIconMatcher.matchIcon(platformName)
        _platformIconRes.value = iconRes
    }
    
    /**
     * 检查密码强度
     */
    fun checkPasswordStrength(password: String) {
        val strength = passwordSecurityManager.checkPasswordStrength(password)
        
        // 转换为数值进度
        val progress = when (strength) {
            PasswordSecurityManager.PasswordStrength.WEAK -> 25
            PasswordSecurityManager.PasswordStrength.MEDIUM -> 60
            PasswordSecurityManager.PasswordStrength.STRONG -> 90
        }
        _passwordStrength.value = progress
        
        val strengthText = when (strength) {
            PasswordSecurityManager.PasswordStrength.WEAK -> "弱"
            PasswordSecurityManager.PasswordStrength.MEDIUM -> "中等"
            PasswordSecurityManager.PasswordStrength.STRONG -> "强"
        }
        _passwordStrengthText.value = strengthText
    }
    
    /**
     * 生成安全密码
     */
    fun generateSecurePassword(): String {
        // 简单的密码生成逻辑
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..12)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * 保存密码条目
     */
    fun savePasswordEntry(
        platformName: String,
        loginMethod: String,
        account: String,
        password: String
    ) {
        if (platformName.isBlank() || loginMethod.isBlank() || 
            account.isBlank() || password.isBlank()) {
            _saveResult.value = false
            return
        }
        
        viewModelScope.launch {
            try {
                // 加密密码
                val encryptedPassword = encryptionService.encrypt(password)
                
                val passwordEntry = PasswordEntry(
                    platform = platformName,
                    loginMethod = loginMethod,
                    account = account,
                    encryptedPassword = encryptedPassword,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                database.passwordDao().insertPassword(passwordEntry)
                
                // 记录账号历史用于智能建议
                accountSuggestionProvider.recordAccountUsage(account)
                
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
}