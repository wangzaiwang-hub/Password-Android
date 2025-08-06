package com.passwordmanager.app.ui.dialogs

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.passwordmanager.app.R
import com.passwordmanager.app.data.entities.PasswordEntry
import com.passwordmanager.app.services.EncryptionService
import com.passwordmanager.app.ui.viewmodels.SearchViewModel
import com.passwordmanager.app.utils.PlatformIconMatcher

/**
 * 密码详情弹窗
 * 显示密码的完整信息，支持查看、编辑、删除操作
 */
class PasswordDetailsDialog : DialogFragment() {

    companion object {
        private const val ARG_PASSWORD_ENTRY = "password_entry"
        
        fun newInstance(passwordEntry: PasswordEntry): PasswordDetailsDialog {
            val dialog = PasswordDetailsDialog()
            val args = Bundle()
            args.putSerializable(ARG_PASSWORD_ENTRY, passwordEntry)
            dialog.arguments = args
            return dialog
        }
    }

    private lateinit var passwordEntry: PasswordEntry
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var encryptionService: EncryptionService
    private lateinit var platformIconMatcher: PlatformIconMatcher
    
    // UI组件
    private lateinit var platformIcon: ImageView
    private lateinit var platformName: TextView
    private lateinit var loginMethod: TextView
    private lateinit var accountInfo: TextView
    private lateinit var passwordDisplay: TextView
    private lateinit var visibilityToggle: ImageView
    private lateinit var copyPassword: ImageView
    private lateinit var closeButton: ImageView
    private lateinit var deleteButton: MaterialButton
    private lateinit var editButton: MaterialButton
    private lateinit var confirmButton: MaterialButton
    
    private var isPasswordVisible = false
    private var decryptedPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        passwordEntry = arguments?.getSerializable(ARG_PASSWORD_ENTRY) as? PasswordEntry
            ?: throw IllegalArgumentException("PasswordEntry is required")
            
        searchViewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        encryptionService = EncryptionService()
        platformIconMatcher = PlatformIconMatcher()
        
        // 解密密码
        try {
            decryptedPassword = encryptionService.decrypt(passwordEntry.encryptedPassword)
        } catch (e: Exception) {
            decryptedPassword = "解密失败"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_password_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupData()
        setupListeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    private fun initViews(view: View) {
        platformIcon = view.findViewById(R.id.platformIcon)
        platformName = view.findViewById(R.id.platformName)
        loginMethod = view.findViewById(R.id.loginMethod)
        accountInfo = view.findViewById(R.id.accountInfo)
        passwordDisplay = view.findViewById(R.id.passwordDisplay)
        visibilityToggle = view.findViewById(R.id.visibilityToggle)
        copyPassword = view.findViewById(R.id.copyPassword)
        closeButton = view.findViewById(R.id.closeButton)
        deleteButton = view.findViewById(R.id.deleteButton)
        editButton = view.findViewById(R.id.editButton)
        confirmButton = view.findViewById(R.id.confirmButton)
    }

    private fun setupData() {
        // 设置平台信息
        platformName.text = passwordEntry.platform
        loginMethod.text = passwordEntry.loginMethod
        accountInfo.text = passwordEntry.account
        
        // 设置平台图标
        val iconRes = platformIconMatcher.matchIcon(passwordEntry.platform)
        platformIcon.setImageResource(iconRes)
        
        // 默认显示掩码密码
        passwordDisplay.text = getMaskedPassword()
        visibilityToggle.setImageResource(R.drawable.ic_visibility_off)
    }

    private fun setupListeners() {
        // 关闭按钮
        closeButton.setOnClickListener {
            dismiss()
        }
        
        // 确定按钮
        confirmButton.setOnClickListener {
            dismiss()
        }
        
        // 密码可见性切换
        passwordDisplay.setOnClickListener {
            togglePasswordVisibility()
        }
        
        visibilityToggle.setOnClickListener {
            togglePasswordVisibility()
        }
        
        // 复制密码
        copyPassword.setOnClickListener {
            copyPasswordToClipboard()
        }
        
        // 编辑按钮
        editButton.setOnClickListener {
            // TODO: 实现编辑功能
            Toast.makeText(context, "编辑功能开发中...", Toast.LENGTH_SHORT).show()
        }
        
        // 删除按钮
        deleteButton.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // 隐藏密码
            passwordDisplay.text = getMaskedPassword()
            visibilityToggle.setImageResource(R.drawable.ic_visibility_off)
            isPasswordVisible = false
        } else {
            // 显示密码
            passwordDisplay.text = decryptedPassword
            visibilityToggle.setImageResource(R.drawable.ic_visibility)
            isPasswordVisible = true
        }
    }

    private fun getMaskedPassword(): String {
        return "●".repeat(minOf(decryptedPassword.length, 8))
    }

    private fun copyPasswordToClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("password", decryptedPassword)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(context, "密码已复制到剪贴板", Toast.LENGTH_SHORT).show()
        
        // 安全考虑：5秒后清空剪贴板
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            } catch (e: Exception) {
                // 忽略清空剪贴板的异常
            }
        }, 5000)
    }

    private fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("删除密码")
            .setMessage("确定要删除「${passwordEntry.platform}」的密码记录吗？此操作不可撤销。")
            .setPositiveButton("删除") { _, _ ->
                deletePassword()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deletePassword() {
        try {
            searchViewModel.deletePassword(passwordEntry.id)
            Toast.makeText(context, "密码已删除", Toast.LENGTH_SHORT).show()
            dismiss()
        } catch (e: Exception) {
            Toast.makeText(context, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        // 设置弹窗大小
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}