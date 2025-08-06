package com.passwordmanager.app.utils

import com.passwordmanager.app.data.dao.AccountHistoryDao
import com.passwordmanager.app.data.entities.AccountHistory

/**
 * 账号建议提供器
 * 基于历史使用记录提供智能账号建议
 */
class AccountSuggestionProvider(
    private val accountHistoryDao: AccountHistoryDao
) {
    
    /**
     * 根据输入内容获取账号建议
     * @param input 用户输入的内容
     * @param limit 建议数量限制
     * @return 建议的账号列表
     */
    suspend fun getSuggestions(input: String, limit: Int = 5): List<String> {
        if (input.isBlank()) {
            return getFrequentAccounts(limit)
        }
        
        val suggestions = accountHistoryDao.getAccountSuggestions(input, limit)
        return suggestions.map { it.account }
    }
    
    /**
     * 获取常用账号
     * @param limit 数量限制
     * @return 常用账号列表
     */
    private suspend fun getFrequentAccounts(limit: Int): List<String> {
        val frequentAccounts = mutableListOf<String>()
        accountHistoryDao.getFrequentAccounts(limit).collect { histories ->
            frequentAccounts.clear()
            frequentAccounts.addAll(histories.map { it.account })
        }
        return frequentAccounts
    }
    
    /**
     * 根据账号类型获取建议
     * @param accountType 账号类型（email, phone, username等）
     * @param limit 数量限制
     * @return 该类型的账号建议
     */
    suspend fun getSuggestionsByType(accountType: String, limit: Int = 5): List<String> {
        val suggestions = mutableListOf<String>()
        accountHistoryDao.getAccountsByType(accountType).collect { histories ->
            suggestions.clear()
            suggestions.addAll(
                histories.take(limit).map { it.account }
            )
        }
        return suggestions
    }
    
    /**
     * 获取邮箱类型的账号建议
     * @param limit 数量限制
     * @return 邮箱账号列表
     */
    suspend fun getEmailSuggestions(limit: Int = 5): List<String> {
        val suggestions = mutableListOf<String>()
        accountHistoryDao.getEmailAccounts().collect { histories ->
            suggestions.clear()
            suggestions.addAll(
                histories.take(limit).map { it.account }
            )
        }
        return suggestions
    }
    
    /**
     * 获取手机号类型的账号建议
     * @param limit 数量限制
     * @return 手机号账号列表
     */
    suspend fun getPhoneSuggestions(limit: Int = 5): List<String> {
        val suggestions = mutableListOf<String>()
        accountHistoryDao.getPhoneAccounts().collect { histories ->
            suggestions.clear()
            suggestions.addAll(
                histories.take(limit).map { it.account }
            )
        }
        return suggestions
    }
    
    /**
     * 智能建议：根据输入内容的格式推测账号类型并提供相应建议
     * @param input 用户输入
     * @param limit 数量限制
     * @return 智能建议列表
     */
    suspend fun getSmartSuggestions(input: String, limit: Int = 5): List<String> {
        if (input.isBlank()) {
            return getSuggestions(input, limit)
        }
        
        // 根据输入格式判断可能的账号类型
        val accountType = detectAccountType(input)
        
        return when (accountType) {
            AccountHistory.TYPE_EMAIL -> {
                // 如果输入看起来像邮箱，优先推荐邮箱账号
                val emailSuggestions = getEmailSuggestions(limit)
                if (emailSuggestions.isNotEmpty()) {
                    emailSuggestions.filter { it.startsWith(input, ignoreCase = true) }
                        .ifEmpty { emailSuggestions }
                } else {
                    getSuggestions(input, limit)
                }
            }
            AccountHistory.TYPE_PHONE -> {
                // 如果输入看起来像手机号，优先推荐手机号账号
                val phoneSuggestions = getPhoneSuggestions(limit)
                if (phoneSuggestions.isNotEmpty()) {
                    phoneSuggestions.filter { it.startsWith(input) }
                        .ifEmpty { phoneSuggestions }
                } else {
                    getSuggestions(input, limit)
                }
            }
            else -> {
                // 默认按输入内容匹配
                getSuggestions(input, limit)
            }
        }
    }
    
    /**
     * 检测账号类型
     * @param input 输入内容
     * @return 账号类型
     */
    private fun detectAccountType(input: String): String {
        return when {
            // 检测邮箱格式
            input.contains("@") -> AccountHistory.TYPE_EMAIL
            // 检测手机号格式（中国手机号）
            input.matches(Regex("^1[3-9]\\d*$")) -> AccountHistory.TYPE_PHONE
            // 检测用户名格式
            input.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]*$")) -> AccountHistory.TYPE_USERNAME
            else -> AccountHistory.TYPE_OTHER
        }
    }
    
    /**
     * 记录账号使用
     * @param account 使用的账号
     */
    suspend fun recordAccountUsage(account: String) {
        accountHistoryDao.recordAccountUsage(account)
    }
    
    /**
     * 获取账号使用统计
     * @param account 账号
     * @return 使用统计信息
     */
    suspend fun getAccountStats(account: String): AccountHistory? {
        return accountHistoryDao.getAccountHistory(account)
    }
    
    /**
     * 清理长时间未使用的账号历史
     * @param daysAgo 多少天前的记录
     */
    suspend fun cleanupOldHistory(daysAgo: Int = 90) {
        val cutoffTime = System.currentTimeMillis() - (daysAgo * 24 * 60 * 60 * 1000L)
        accountHistoryDao.cleanupOldHistory(cutoffTime)
    }
    
    /**
     * 获取建议的完整信息（包含使用次数等）
     * @param input 输入内容
     * @param limit 数量限制
     * @return 账号历史记录列表
     */
    suspend fun getSuggestionsWithStats(input: String, limit: Int = 5): List<AccountHistory> {
        return if (input.isBlank()) {
            val suggestions = mutableListOf<AccountHistory>()
            accountHistoryDao.getFrequentAccounts(limit).collect { histories ->
                suggestions.clear()
                suggestions.addAll(histories)
            }
            suggestions
        } else {
            accountHistoryDao.getAccountSuggestions(input, limit)
        }
    }
}