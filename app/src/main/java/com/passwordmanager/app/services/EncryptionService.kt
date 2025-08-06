package com.passwordmanager.app.services

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import android.util.Base64
import java.security.SecureRandom

/**
 * 加密服务类
 * 使用Android Keystore和AES-256加密来保护敏感数据
 * 确保密码永不以明文形式存储或传输
 */
class EncryptionService {
    
    companion object {
        private const val KEYSTORE_ALIAS = "PasswordManagerKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val IV_SEPARATOR = ":"
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    init {
        generateKeyIfNeeded()
    }
    
    /**
     * 加密明文数据
     * @param plainText 要加密的明文
     * @return 加密后的Base64编码字符串（包含IV）
     */
    fun encrypt(plainText: String): String {
        try {
            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // 将IV和加密数据组合，用冒号分隔
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            
            return "$ivBase64$IV_SEPARATOR$encryptedBase64"
        } catch (e: Exception) {
            throw EncryptionException("加密失败", e)
        }
    }
    
    /**
     * 解密数据
     * @param encryptedText 加密的Base64编码字符串（包含IV）
     * @return 解密后的明文
     */
    fun decrypt(encryptedText: String): String {
        try {
            val parts = encryptedText.split(IV_SEPARATOR)
            if (parts.size != 2) {
                throw EncryptionException("加密数据格式错误")
            }
            
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)
            
            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw EncryptionException("解密失败", e)
        }
    }
    
    /**
     * 生成密钥（如果不存在）
     */
    private fun generateKeyIfNeeded() {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            generateKey()
        }
    }
    
    /**
     * 生成新的AES密钥
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    /**
     * 获取存储的密钥
     */
    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            ?: throw EncryptionException("无法获取加密密钥")
    }
    
    /**
     * 检查密钥是否存在
     */
    fun isKeyExists(): Boolean {
        return keyStore.containsAlias(KEYSTORE_ALIAS)
    }
    
    /**
     * 删除密钥（用于重置）
     */
    fun deleteKey() {
        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            keyStore.deleteEntry(KEYSTORE_ALIAS)
        }
    }
    
    /**
     * 生成安全的随机密码
     * @param length 密码长度
     * @param includeSymbols 是否包含特殊字符
     * @return 生成的随机密码
     */
    fun generateSecurePassword(length: Int = 12, includeSymbols: Boolean = true): String {
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val digits = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        
        val charset = buildString {
            append(lowercase)
            append(uppercase)
            append(digits)
            if (includeSymbols) {
                append(symbols)
            }
        }
        
        val random = SecureRandom()
        return (1..length)
            .map { charset[random.nextInt(charset.length)] }
            .joinToString("")
    }
}

/**
 * 加密异常类
 */
class EncryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)