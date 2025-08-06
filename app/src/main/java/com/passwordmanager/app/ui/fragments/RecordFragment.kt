package com.passwordmanager.app.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.passwordmanager.app.R
import com.passwordmanager.app.ui.viewmodels.RecordViewModel

/**
 * 记录密码Fragment
 * 用于添加和编辑密码
 */
class RecordFragment : Fragment() {

    private lateinit var viewModel: RecordViewModel
    
    // UI组件 - 使用可空类型以防布局中不存在
    private var platformNameEdit: TextInputEditText? = null
    private var loginMethodEdit: AutoCompleteTextView? = null
    private var accountEdit: TextInputEditText? = null
    private var passwordEdit: TextInputEditText? = null
    private var passwordVisibilityToggle: ImageView? = null
    private var saveBtn: MaterialButton? = null
    
    // 平台图标卡片
    private var googleCard: View? = null
    private var facebookCard: View? = null
    private var wechatCard: View? = null
    private var qqCard: View? = null
    private var alipayCard: View? = null
    private var taobaoCard: View? = null
    private var twitterCard: View? = null
    private var addCustomCard: View? = null
    
    // 当前选择的平台
    private var selectedPlatform: String = ""
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            viewModel = ViewModelProvider(this)[RecordViewModel::class.java]
            initViews(view)
            setupObservers()
            setupListeners()
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果初始化失败，显示错误信息
            Toast.makeText(context, "页面初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun initViews(view: View) {
        try {
            // 安全地查找UI元素，如果不存在则为null
            platformNameEdit = view.findViewById(R.id.platformNameEdit)
            loginMethodEdit = view.findViewById(R.id.loginMethodEdit)
            accountEdit = view.findViewById(R.id.accountEdit)
            passwordEdit = view.findViewById(R.id.passwordEdit)
            passwordVisibilityToggle = view.findViewById(R.id.passwordVisibilityToggle)
            saveBtn = view.findViewById(R.id.saveBtn)
            
            // 平台图标卡片
            googleCard = view.findViewById(R.id.googleCard)
            facebookCard = view.findViewById(R.id.facebookCard)
            wechatCard = view.findViewById(R.id.wechatCard)
            qqCard = view.findViewById(R.id.qqCard)
            alipayCard = view.findViewById(R.id.alipayCard)
            taobaoCard = view.findViewById(R.id.taobaoCard)
            twitterCard = view.findViewById(R.id.twitterCard)
            addCustomCard = view.findViewById(R.id.addCustomCard)
            
            // 检查关键UI元素是否存在
            if (platformNameEdit == null || saveBtn == null) {
                Toast.makeText(context, "使用简化界面", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "UI初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupObservers() {
        // 观察登录方式列表
        viewModel.loginMethods.observe(viewLifecycleOwner) { methods ->
            loginMethodEdit?.let { editText ->
                val methodNames = methods.map { it.name }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    methodNames
                )
                editText.setAdapter(adapter)
            }
        }
        
        // 观察账号建议
        viewModel.accountSuggestions.observe(viewLifecycleOwner) { suggestions ->
            // 账号建议功能暂时简化，后续可以扩展
        }
        
        // 密码强度检测已移除，保持简洁的UI
        
        // 观察平台图标选择
        viewModel.platformIconRes.observe(viewLifecycleOwner) { iconRes ->
            // 图标选择逻辑已移到平台卡片点击处理中
        }
        
        // 观察保存结果
        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "密码保存成功", Toast.LENGTH_SHORT).show()
                clearForm()
            } else {
                Toast.makeText(context, "保存失败，请检查输入", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupListeners() {
        // 平台图标卡片点击监听
        googleCard?.setOnClickListener { selectPlatform("Google", "Google") }
        facebookCard?.setOnClickListener { selectPlatform("Facebook", "Facebook") }
        wechatCard?.setOnClickListener { selectPlatform("微信", "WeChat") }
        qqCard?.setOnClickListener { selectPlatform("QQ", "QQ") }
        alipayCard?.setOnClickListener { selectPlatform("支付宝", "Alipay") }
        taobaoCard?.setOnClickListener { selectPlatform("淘宝", "Taobao") }
        twitterCard?.setOnClickListener { selectPlatform("Twitter", "Twitter") }
        addCustomCard?.setOnClickListener {
            Toast.makeText(context, "自定义图标功能开发中...", Toast.LENGTH_SHORT).show()
        }
        
        // 平台名称输入监听
        platformNameEdit?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val platformName = s.toString().trim()
                if (platformName.isNotEmpty()) {
                    viewModel.matchPlatformIcon(platformName)
                    viewModel.getAccountSuggestions(platformName)
                }
            }
        })
        
        // 密码输入监听
        passwordEdit?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                viewModel.checkPasswordStrength(password)
            }
        })
        
        // 密码可见性切换
        passwordVisibilityToggle?.setOnClickListener {
            togglePasswordVisibility()
        }
        
        // 保存按钮
        saveBtn?.setOnClickListener {
            savePassword()
        }
    }
    
    private fun savePassword() {
        // 检查UI元素是否存在
        val platformNameEditText = platformNameEdit
        val loginMethodEditText = loginMethodEdit
        val accountEditText = accountEdit
        val passwordEditText = passwordEdit
        
        if (platformNameEditText == null || loginMethodEditText == null || 
            accountEditText == null || passwordEditText == null) {
            Toast.makeText(context, "当前使用简化界面，保存功能不可用", Toast.LENGTH_SHORT).show()
            return
        }
        
        val platformName = platformNameEditText.text.toString().trim()
        val loginMethod = loginMethodEditText.text.toString().trim()
        val account = accountEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        
        if (platformName.isEmpty()) {
            platformNameEditText.error = "请输入平台名称"
            return
        }
        
        if (loginMethod.isEmpty()) {
            loginMethodEditText.error = "请选择登录方式"
            return
        }
        
        if (account.isEmpty()) {
            accountEditText.error = "请输入账号"
            return
        }
        
        if (password.isEmpty()) {
            passwordEditText.error = "请输入密码"
            return
        }
        
        viewModel.savePasswordEntry(platformName, loginMethod, account, password)
    }
    
    private fun selectPlatform(displayName: String, platformKey: String) {
        selectedPlatform = platformKey
        platformNameEdit?.setText(displayName)
        
        // 重置所有卡片的选中状态
        resetPlatformCardSelection()
        
        // 设置选中的卡片样式
        when (platformKey) {
            "Google" -> highlightCard(googleCard)
            "Facebook" -> highlightCard(facebookCard)
            "WeChat" -> highlightCard(wechatCard)
            "QQ" -> highlightCard(qqCard)
            "Alipay" -> highlightCard(alipayCard)
            "Taobao" -> highlightCard(taobaoCard)
            "Twitter" -> highlightCard(twitterCard)
        }
        
        Toast.makeText(context, "已选择 $displayName", Toast.LENGTH_SHORT).show()
    }
    
    private fun resetPlatformCardSelection() {
        // 重置所有卡片的背景色
        listOf(googleCard, facebookCard, wechatCard, qqCard, alipayCard, taobaoCard, twitterCard).forEach { card ->
            (card as? com.google.android.material.card.MaterialCardView)?.apply {
                strokeColor = android.graphics.Color.TRANSPARENT
                strokeWidth = 0
            }
        }
    }
    
    private fun highlightCard(card: View?) {
        (card as? com.google.android.material.card.MaterialCardView)?.apply {
            strokeColor = resources.getColor(R.color.primary_blue, null)
            strokeWidth = 4
        }
    }
    
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        
        if (isPasswordVisible) {
            passwordEdit?.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordVisibilityToggle?.setImageResource(R.drawable.ic_visibility)
        } else {
            passwordEdit?.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordVisibilityToggle?.setImageResource(R.drawable.ic_visibility_off)
        }
        
        // 保持光标位置
        passwordEdit?.setSelection(passwordEdit?.text?.length ?: 0)
    }
    
    private fun clearForm() {
        platformNameEdit?.text?.clear()
        loginMethodEdit?.text?.clear()
        accountEdit?.text?.clear()
        passwordEdit?.text?.clear()
        selectedPlatform = ""
        isPasswordVisible = false
        resetPlatformCardSelection()
        passwordVisibilityToggle?.setImageResource(R.drawable.ic_visibility_off)
    }
}