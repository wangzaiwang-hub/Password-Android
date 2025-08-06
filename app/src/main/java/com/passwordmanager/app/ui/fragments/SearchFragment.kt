package com.passwordmanager.app.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.passwordmanager.app.R
import com.passwordmanager.app.ui.adapters.PasswordListAdapter
import com.passwordmanager.app.ui.viewmodels.SearchViewModel
import com.passwordmanager.app.ui.dialogs.PasswordDetailsDialog

/**
 * 搜索密码Fragment
 * 用于查询和显示密码列表
 */
class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel
    private lateinit var passwordAdapter: PasswordListAdapter
    
    // UI组件 - 使用可空类型以防布局中不存在
    private var searchEdit: TextInputEditText? = null
    // 筛选标签已移除，保持简洁的UI
    private var passwordRecyclerView: RecyclerView? = null
    private var emptyStateLayout: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            inflater.inflate(R.layout.fragment_search, container, false)
        } catch (e: Exception) {
            e.printStackTrace()
            inflater.inflate(R.layout.fragment_search_simple, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            viewModel = ViewModelProvider(this)[SearchViewModel::class.java]
            initViews(view)
            setupRecyclerView()
            setupObservers()
            setupListeners()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "搜索页面初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun initViews(view: View) {
        searchEdit = view.findViewById(R.id.searchEdit)
        // 筛选标签UI已移除
        passwordRecyclerView = view.findViewById(R.id.passwordRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        
        // 检查关键UI元素是否存在
        if (searchEdit == null || passwordRecyclerView == null) {
            Toast.makeText(context, "使用简化界面", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupRecyclerView() {
        passwordAdapter = PasswordListAdapter(
            onPasswordDecrypt = { encryptedPassword ->
                viewModel.decryptPassword(encryptedPassword)
            },
            onPasswordClick = { passwordEntry ->
                showPasswordDetails(passwordEntry)
            }
        )
        
        passwordRecyclerView?.adapter = passwordAdapter
    }
    
    private fun setupObservers() {
        // 观察搜索结果
        viewModel.searchResults.observe(viewLifecycleOwner) { passwords ->
            passwordAdapter.submitList(passwords)
        }
        
        // 观察空状态
        viewModel.showEmptyState.observe(viewLifecycleOwner) { showEmpty ->
            if (showEmpty) {
                passwordRecyclerView?.visibility = View.GONE
                emptyStateLayout?.visibility = View.VISIBLE
            } else {
                passwordRecyclerView?.visibility = View.VISIBLE
                emptyStateLayout?.visibility = View.GONE
            }
        }
    }
    
    private fun setupListeners() {
        // 搜索输入监听
        searchEdit?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                viewModel.searchPasswords(query)
            }
        })
        
        // 筛选功能已移除，保持简洁的UI
    }
    
    override fun onResume() {
        super.onResume()
        // 每次回到页面时刷新数据
        viewModel.loadAllPasswords()
    }
    
    /**
     * 显示密码详情弹窗
     */
    private fun showPasswordDetails(passwordEntry: com.passwordmanager.app.data.entities.PasswordEntry) {
        try {
            val dialog = PasswordDetailsDialog.newInstance(passwordEntry)
            dialog.show(parentFragmentManager, "PasswordDetailsDialog")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "无法显示密码详情: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}