package com.passwordmanager.app.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.passwordmanager.app.R
import com.passwordmanager.app.data.database.PasswordManagerDatabase
import com.passwordmanager.app.services.ExportService
import com.passwordmanager.app.utils.BackgroundManager
import kotlinx.coroutines.launch

/**
 * 个人中心Fragment
 * 用于显示用户设置和应用信息
 */
class ProfileFragment : Fragment() {

    // UI组件
    private lateinit var appVersion: TextView
    private lateinit var exportBtn: MaterialButton
    private lateinit var csvFormatCard: View
    private lateinit var jsonFormatCard: View
    private lateinit var xmlFormatCard: View
    
    // 背景管理器
    private lateinit var backgroundManager: BackgroundManager
    
    // 图片选择器
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedImage(uri)
            }
        }
    }
    
    private var currentBackgroundDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            inflater.inflate(R.layout.fragment_profile, container, false)
        } catch (e: Exception) {
            e.printStackTrace()
            inflater.inflate(R.layout.fragment_profile_simple, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            backgroundManager = BackgroundManager(requireContext())
            initViews(view)
            setupListeners()
            loadUserStats()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "个人中心初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun initViews(view: View) {
        appVersion = view.findViewById(R.id.appVersion)
        exportBtn = view.findViewById(R.id.exportBtn)
        csvFormatCard = view.findViewById(R.id.csvFormatCard)
        jsonFormatCard = view.findViewById(R.id.jsonFormatCard)
        xmlFormatCard = view.findViewById(R.id.xmlFormatCard)
    }
    
    private fun setupListeners() {
        var selectedFormat = ExportService.ExportFormat.CSV
        
        // 格式选择卡片
        csvFormatCard.setOnClickListener {
            selectedFormat = ExportService.ExportFormat.CSV
            updateFormatSelection()
        }
        
        jsonFormatCard.setOnClickListener {
            selectedFormat = ExportService.ExportFormat.JSON
            updateFormatSelection()
        }
        
        xmlFormatCard.setOnClickListener {
            // XML格式暂未实现
            Toast.makeText(context, "XML格式暂未支持", Toast.LENGTH_SHORT).show()
        }
        
        // 导出按钮
        exportBtn.setOnClickListener {
            performExport(selectedFormat)
        }
    }
    
    private fun updateFormatSelection() {
        // 这里可以添加视觉反馈来显示选中的格式
        // 由于布局已经设置了默认选中CSV，这里暂时不做额外处理
    }
    
    private fun loadUserStats() {
        // 设置应用版本显示
        appVersion.text = "版本 1.0.0"
    }
    
    private fun showHelpDialog() {
        val helpText = """
            密码管家使用帮助：
            
            1. 记录页面：添加新的密码记录
            2. 查询页面：搜索和查看已保存的密码
            3. 我的页面：管理应用设置和数据
            
            安全提示：
            • 所有密码都经过加密存储
            • 支持密码强度检测
            • 复制密码后5秒自动清空剪贴板
            
            如有问题，请联系开发者。
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("帮助与反馈")
            .setMessage(helpText)
            .setPositiveButton("确定", null)
            .show()
    }
    
    private fun showAboutDialog() {
        val aboutText = """
            密码管家 v1.0.0
            
            一款安全、简洁的密码管理应用
            
            主要功能：
            • 密码安全存储
            • 智能图标匹配
            • 账号建议功能
            • 密码强度检测
            • 自定义背景
            
            开发者：密码管家团队
            版权所有 © 2025
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("关于密码管家")
            .setMessage(aboutText)
            .setPositiveButton("确定", null)
            .show()
    }
    
    private fun showExportDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_export_options, null)
        
        val csvRadioButton = dialogView.findViewById<RadioButton>(R.id.csvRadioButton)
        val jsonRadioButton = dialogView.findViewById<RadioButton>(R.id.jsonRadioButton)
        val exportStatsText = dialogView.findViewById<TextView>(R.id.exportStatsText)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.cancelButton)
        val exportButton = dialogView.findViewById<MaterialButton>(R.id.exportButton)
        
        // 加载密码统计
        lifecycleScope.launch {
            try {
                val database = PasswordManagerDatabase.getDatabase(requireContext())
                database.passwordDao().getAllPasswords().collect { passwords ->
                    exportStatsText.text = "准备导出 ${passwords.size} 条密码记录"
                }
            } catch (e: Exception) {
                exportStatsText.text = "准备导出 0 条密码记录"
            }
        }
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        exportButton.setOnClickListener {
            val format = if (csvRadioButton.isChecked) {
                ExportService.ExportFormat.CSV
            } else {
                ExportService.ExportFormat.JSON
            }
            
            dialog.dismiss()
            performExport(format)
        }
        
        dialog.show()
    }
    
    private fun performExport(format: ExportService.ExportFormat) {
        lifecycleScope.launch {
            try {
                // 显示进度提示
                val progressDialog = AlertDialog.Builder(requireContext())
                    .setTitle("导出中...")
                    .setMessage("正在准备导出文件，请稍候...")
                    .setCancelable(false)
                    .create()
                progressDialog.show()
                
                val database = PasswordManagerDatabase.getDatabase(requireContext())
                val exportService = ExportService(requireContext())
                
                // 获取所有密码
                database.passwordDao().getAllPasswords().collect { passwords ->
                    if (passwords.isEmpty()) {
                        progressDialog.dismiss()
                        Toast.makeText(context, "没有密码数据可导出", Toast.LENGTH_SHORT).show()
                        return@collect
                    }
                    
                    // 执行导出
                    val result = exportService.exportPasswords(passwords, format, false)
                    progressDialog.dismiss()
                    
                    result.fold(
                        onSuccess = { uri ->
                            // 创建分享Intent
                            val shareIntent = exportService.createShareIntent(uri, format)
                            val chooserIntent = Intent.createChooser(shareIntent, "导出密码数据")
                            startActivity(chooserIntent)
                            
                            Toast.makeText(context, "导出成功！", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(context, "导出失败：${error.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(context, "导出失败：${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showBackgroundSettingsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_background_settings, null)
        
        val backgroundPreview = dialogView.findViewById<ImageView>(R.id.backgroundPreview)
        val noBackgroundText = dialogView.findViewById<TextView>(R.id.noBackgroundText)
        val opacitySlider = dialogView.findViewById<Slider>(R.id.opacitySlider)
        val opacityValueText = dialogView.findViewById<TextView>(R.id.opacityValueText)
        val selectImageButton = dialogView.findViewById<MaterialButton>(R.id.selectImageButton)
        val clearBackgroundButton = dialogView.findViewById<MaterialButton>(R.id.clearBackgroundButton)
        val applyButton = dialogView.findViewById<MaterialButton>(R.id.applyButton)
        
        // 初始化UI状态
        updateBackgroundPreview(backgroundPreview, noBackgroundText)
        val currentOpacity = backgroundManager.getBackgroundOpacity()
        opacitySlider.value = currentOpacity
        opacityValueText.text = "透明度: ${(currentOpacity * 100).toInt()}%"
        
        // 透明度滑块监听
        opacitySlider.addOnChangeListener { _, value, _ ->
            opacityValueText.text = "透明度: ${(value * 100).toInt()}%"
        }
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        currentBackgroundDialog = dialog
        
        // 选择图片按钮
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            imagePickerLauncher.launch(Intent.createChooser(intent, "选择背景图片"))
        }
        
        // 清除背景按钮
        clearBackgroundButton.setOnClickListener {
            if (backgroundManager.clearCustomBackground()) {
                updateBackgroundPreview(backgroundPreview, noBackgroundText)
                Toast.makeText(context, "背景已清除", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "清除背景失败", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 应用按钮
        applyButton.setOnClickListener {
            backgroundManager.setBackgroundOpacity(opacitySlider.value)
            Toast.makeText(context, "背景设置已应用", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            
            // 通知MainActivity更新背景
            (activity as? com.passwordmanager.app.MainActivity)?.applyCustomBackground()
        }
        
        dialog.show()
    }
    
    private fun updateBackgroundPreview(imageView: ImageView, textView: TextView) {
        val preview = backgroundManager.getBackgroundPreview()
        if (preview != null) {
            imageView.setImageBitmap(preview)
            imageView.visibility = View.VISIBLE
            textView.visibility = View.GONE
        } else {
            imageView.visibility = View.GONE
            textView.visibility = View.VISIBLE
        }
    }
    
    private fun handleSelectedImage(uri: android.net.Uri) {
        lifecycleScope.launch {
            try {
                val success = backgroundManager.saveCustomBackground(uri)
                if (success) {
                    Toast.makeText(context, "背景图片已保存", Toast.LENGTH_SHORT).show()
                    
                    // 更新对话框中的预览
                    currentBackgroundDialog?.let { dialog ->
                        val dialogView = dialog.findViewById<View>(android.R.id.content)
                        dialogView?.let { view ->
                            val backgroundPreview = view.findViewById<ImageView>(R.id.backgroundPreview)
                            val noBackgroundText = view.findViewById<TextView>(R.id.noBackgroundText)
                            updateBackgroundPreview(backgroundPreview, noBackgroundText)
                        }
                    }
                } else {
                    Toast.makeText(context, "保存背景图片失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "处理图片失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 每次回到页面时刷新统计数据
        loadUserStats()
    }
}