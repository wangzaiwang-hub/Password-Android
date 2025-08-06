package com.passwordmanager.app.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.passwordmanager.app.data.entities.PasswordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 密码导出服务
 * 支持导出为CSV和JSON格式
 */
class ExportService(private val context: Context) {
    
    enum class ExportFormat {
        CSV, JSON
    }
    
    /**
     * 导出密码数据
     */
    suspend fun exportPasswords(
        passwords: List<PasswordEntry>,
        format: ExportFormat,
        includePasswords: Boolean = false
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = generateFileName(format)
            val file = File(context.getExternalFilesDir(null), fileName)
            
            when (format) {
                ExportFormat.CSV -> exportToCsv(passwords, file, includePasswords)
                ExportFormat.JSON -> exportToJson(passwords, file, includePasswords)
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 导出为CSV格式
     */
    private fun exportToCsv(passwords: List<PasswordEntry>, file: File, includePasswords: Boolean) {
        FileWriter(file).use { writer ->
            // 写入CSV头部
            val header = if (includePasswords) {
                "平台,登录方式,账号,密码,创建时间,更新时间\n"
            } else {
                "平台,登录方式,账号,创建时间,更新时间\n"
            }
            writer.write(header)
            
            // 写入数据
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            passwords.forEach { password ->
                val createdTime = dateFormat.format(Date(password.createdAt))
                val updatedTime = dateFormat.format(Date(password.updatedAt))
                
                val line = if (includePasswords) {
                    "${escapeCsv(password.platform)},${escapeCsv(password.loginMethod)},${escapeCsv(password.account)},${escapeCsv("******")},${createdTime},${updatedTime}\n"
                } else {
                    "${escapeCsv(password.platform)},${escapeCsv(password.loginMethod)},${escapeCsv(password.account)},${createdTime},${updatedTime}\n"
                }
                writer.write(line)
            }
        }
    }
    
    /**
     * 导出为JSON格式
     */
    private fun exportToJson(passwords: List<PasswordEntry>, file: File, includePasswords: Boolean) {
        FileWriter(file).use { writer ->
            writer.write("{\n")
            writer.write("  \"export_info\": {\n")
            writer.write("    \"app_name\": \"密码管家\",\n")
            writer.write("    \"export_time\": \"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\",\n")
            writer.write("    \"total_count\": ${passwords.size},\n")
            writer.write("    \"include_passwords\": $includePasswords\n")
            writer.write("  },\n")
            writer.write("  \"passwords\": [\n")
            
            passwords.forEachIndexed { index, password ->
                val createdTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(password.createdAt))
                val updatedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(password.updatedAt))
                
                writer.write("    {\n")
                writer.write("      \"platform\": \"${escapeJson(password.platform)}\",\n")
                writer.write("      \"login_method\": \"${escapeJson(password.loginMethod)}\",\n")
                writer.write("      \"account\": \"${escapeJson(password.account)}\",\n")
                if (includePasswords) {
                    writer.write("      \"password\": \"******\",\n")
                }
                writer.write("      \"created_at\": \"$createdTime\",\n")
                writer.write("      \"updated_at\": \"$updatedTime\"\n")
                writer.write("    }")
                
                if (index < passwords.size - 1) {
                    writer.write(",")
                }
                writer.write("\n")
            }
            
            writer.write("  ]\n")
            writer.write("}")
        }
    }
    
    /**
     * 创建分享Intent
     */
    fun createShareIntent(uri: Uri, format: ExportFormat): Intent {
        val mimeType = when (format) {
            ExportFormat.CSV -> "text/csv"
            ExportFormat.JSON -> "application/json"
        }
        
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "密码管家数据导出")
            putExtra(Intent.EXTRA_TEXT, "这是从密码管家导出的数据文件")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    /**
     * 生成文件名
     */
    private fun generateFileName(format: ExportFormat): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val extension = when (format) {
            ExportFormat.CSV -> "csv"
            ExportFormat.JSON -> "json"
        }
        return "passwords_export_$timestamp.$extension"
    }
    
    /**
     * CSV字符串转义
     */
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
    
    /**
     * JSON字符串转义
     */
    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}