package com.passwordmanager.app.utils

import com.passwordmanager.app.R

/**
 * 平台图标匹配器
 * 根据平台名称自动匹配对应的图标
 */
class PlatformIconMatcher {
    
    companion object {
        // 精确匹配的平台图标
        private val exactIconMap = mapOf(
            "微信" to R.drawable.ic_wechat,
            "wechat" to R.drawable.ic_wechat,
            "qq" to R.drawable.ic_qq,
            "淘宝" to R.drawable.ic_taobao,
            "taobao" to R.drawable.ic_taobao,
            "支付宝" to R.drawable.ic_alipay,
            "alipay" to R.drawable.ic_alipay
        )
        
        // 分类图标映射
        private val categoryIconMap = mapOf(
            "email" to R.drawable.ic_email,
            "shopping" to R.drawable.ic_shopping,
            "game" to R.drawable.ic_game,
            "web" to R.drawable.ic_web
        )
        
        // 默认占位图标
        private val defaultIcon = R.drawable.ic_default_platform
    }
    
    /**
     * 根据平台名称匹配图标
     * @param platformName 平台名称
     * @return 图标资源ID，如果没有匹配则返回默认图标
     */
    fun matchIcon(platformName: String): Int {
        if (platformName.isBlank()) {
            return defaultIcon
        }
        
        val lowerName = platformName.lowercase()
        
        // 1. 精确匹配
        exactIconMap[lowerName]?.let { return it }
        exactIconMap[platformName]?.let { return it }
        
        // 2. 关键词匹配
        when {
            lowerName.contains("mail") || lowerName.contains("邮箱") || 
            lowerName.contains("gmail") || lowerName.contains("outlook") -> 
                return categoryIconMap["email"] ?: defaultIcon
                
            lowerName.contains("shop") || lowerName.contains("购物") || 
            lowerName.contains("商城") || lowerName.contains("mall") -> 
                return categoryIconMap["shopping"] ?: defaultIcon
                
            lowerName.contains("game") || lowerName.contains("游戏") || 
            lowerName.contains("steam") || lowerName.contains("play") -> 
                return categoryIconMap["game"] ?: defaultIcon
                
            lowerName.contains("web") || lowerName.contains("网站") || 
            lowerName.contains("site") -> 
                return categoryIconMap["web"] ?: defaultIcon
        }
        
        return defaultIcon
    }
    

    
    /**
     * 获取所有支持的平台列表
     * @return 支持的平台名称列表
     */
    fun getSupportedPlatforms(): List<String> {
        return exactIconMap.keys.toList().sorted()
    }
    
    /**
     * 检查平台是否有对应图标
     * @param platformName 平台名称
     * @return 是否有对应图标
     */
    fun hasIcon(platformName: String): Boolean {
        return matchIcon(platformName) != defaultIcon
    }
    
    /**
     * 获取默认图标
     * @return 默认图标资源ID
     */
    fun getDefaultIcon(): Int {
        return defaultIcon
    }
    
    /**
     * 根据分类获取平台图标
     * @param category 分类名称
     * @return 该分类下的平台图标映射
     */
    fun getIconsByCategory(category: String): Map<String, Int> {
        return when (category.lowercase()) {
            "social", "社交" -> exactIconMap.filterKeys { 
                listOf("微信", "wechat", "qq").any { keyword -> 
                    it.lowercase().contains(keyword.lowercase()) 
                }
            }
            "shopping", "购物" -> exactIconMap.filterKeys { 
                listOf("淘宝").any { keyword -> 
                    it.lowercase().contains(keyword.lowercase()) 
                }
            }
            "payment", "支付" -> exactIconMap.filterKeys { 
                listOf("支付宝").any { keyword -> 
                    it.lowercase().contains(keyword.lowercase()) 
                }
            }
            else -> categoryIconMap
        }
    }
}