package com.passwordmanager.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 登录方式实体类
 * 存储用户自定义的登录方式
 */
@Entity(tableName = "login_methods")
data class LoginMethod(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    
    /** 登录方式名称 */
    val name: String,
    
    /** 是否为默认登录方式 */
    val isDefault: Boolean = false,
    
    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 获取默认的登录方式列表
         */
        fun getDefaultLoginMethods(): List<LoginMethod> {
            return listOf(
                LoginMethod(name = "用户名密码", isDefault = true),
                LoginMethod(name = "手机号密码", isDefault = true),
                LoginMethod(name = "邮箱密码", isDefault = true),
                LoginMethod(name = "微信登录", isDefault = true),
                LoginMethod(name = "QQ登录", isDefault = true),
                LoginMethod(name = "支付宝登录", isDefault = true),
                LoginMethod(name = "短信验证码", isDefault = true),
                LoginMethod(name = "邮箱验证码", isDefault = true)
            )
        }
    }
}