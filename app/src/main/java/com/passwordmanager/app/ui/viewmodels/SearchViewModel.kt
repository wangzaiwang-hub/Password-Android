package com.passwordmanager.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.passwordmanager.app.data.database.PasswordManagerDatabase
import com.passwordmanager.app.data.entities.PasswordEntry
import com.passwordmanager.app.services.EncryptionService
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = PasswordManagerDatabase.getDatabase(application)
    private val encryptionService = EncryptionService()
    
    // 密码列表
    private val _passwordList = MutableLiveData<List<PasswordEntry>>()
    val passwordList: LiveData<List<PasswordEntry>> = _passwordList
    
    // 搜索结果
    private val _searchResults = MutableLiveData<List<PasswordEntry>>()
    val searchResults: LiveData<List<PasswordEntry>> = _searchResults
    
    // 是否显示空状态
    private val _showEmptyState = MutableLiveData<Boolean>()
    val showEmptyState: LiveData<Boolean> = _showEmptyState
    
    // 当前筛选类型
    private val _currentFilter = MutableLiveData<FilterType>()
    val currentFilter: LiveData<FilterType> = _currentFilter
    
    enum class FilterType {
        ALL, FREQUENT, RECENT
    }
    
    init {
        _currentFilter.value = FilterType.ALL
        loadAllPasswords()
    }
    
    /**
     * 加载所有密码
     */
    fun loadAllPasswords() {
        viewModelScope.launch {
            try {
                database.passwordDao().getAllPasswords().collect { passwords ->
                    _passwordList.value = passwords
                    applyCurrentFilter()
                }
            } catch (e: Exception) {
                _passwordList.value = emptyList()
                _showEmptyState.value = true
            }
        }
    }
    
    /**
     * 搜索密码
     */
    fun searchPasswords(query: String) {
        if (query.isBlank()) {
            applyCurrentFilter()
            return
        }
        
        viewModelScope.launch {
            try {
                val allPasswords = _passwordList.value ?: emptyList()
                val filteredPasswords = allPasswords.filter { password ->
                    password.platform.contains(query, ignoreCase = true) ||
                    password.account.contains(query, ignoreCase = true) ||
                    password.loginMethod.contains(query, ignoreCase = true)
                }
                
                _searchResults.value = filteredPasswords
                _showEmptyState.value = filteredPasswords.isEmpty()
            } catch (e: Exception) {
                _searchResults.value = emptyList()
                _showEmptyState.value = true
            }
        }
    }
    
    /**
     * 应用筛选
     */
    fun applyFilter(filterType: FilterType) {
        _currentFilter.value = filterType
        applyCurrentFilter()
    }
    
    private fun applyCurrentFilter() {
        val allPasswords = _passwordList.value ?: emptyList()
        
        val filteredPasswords = when (_currentFilter.value) {
            FilterType.ALL -> allPasswords
            FilterType.FREQUENT -> {
                // 简化实现：按使用频率排序（这里用创建时间模拟）
                allPasswords.sortedByDescending { it.createdAt }
            }
            FilterType.RECENT -> {
                // 按最近添加排序
                allPasswords.sortedByDescending { it.createdAt }.take(10)
            }
            null -> allPasswords
        }
        
        _searchResults.value = filteredPasswords
        _showEmptyState.value = filteredPasswords.isEmpty()
    }
    
    /**
     * 解密密码
     */
    fun decryptPassword(encryptedPassword: String): String {
        return try {
            encryptionService.decrypt(encryptedPassword)
        } catch (e: Exception) {
            "解密失败"
        }
    }
    
    /**
     * 删除密码
     */
    fun deletePassword(password: PasswordEntry) {
        viewModelScope.launch {
            try {
                database.passwordDao().deletePassword(password)
                loadAllPasswords()
            } catch (e: Exception) {
                // 处理删除失败
            }
        }
    }
    
    /**
     * 通过ID删除密码
     */
    fun deletePassword(passwordId: String) {
        viewModelScope.launch {
            try {
                val password = database.passwordDao().getPasswordById(passwordId)
                if (password != null) {
                    database.passwordDao().deletePassword(password)
                    loadAllPasswords()
                }
            } catch (e: Exception) {
                // 处理删除失败
            }
        }
    }
}