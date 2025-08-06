package com.passwordmanager.app.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.passwordmanager.app.R
import com.passwordmanager.app.data.entities.PasswordEntry
import com.passwordmanager.app.utils.PlatformIconMatcher

class PasswordListAdapter(
    private val onPasswordDecrypt: (String) -> String,
    private val onPasswordClick: (PasswordEntry) -> Unit = {}
) : ListAdapter<PasswordEntry, PasswordListAdapter.PasswordViewHolder>(PasswordDiffCallback()) {

    class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val platformIcon: ImageView = itemView.findViewById(R.id.platformIcon)
        val platformName: TextView = itemView.findViewById(R.id.platformName)
        val accountInfo: TextView = itemView.findViewById(R.id.accountInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_password, parent, false)
        return PasswordViewHolder(view)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val password = getItem(position)
        val platformIconMatcher = PlatformIconMatcher()
        
        // 设置平台信息
        holder.platformName.text = password.platform
        holder.accountInfo.text = password.account
        
        // 设置平台图标
        val iconRes = platformIconMatcher.matchIcon(password.platform)
        holder.platformIcon.setImageResource(iconRes)
        
        // 点击整个项目
        holder.itemView.setOnClickListener {
            onPasswordClick(password)
        }
    }
    
    /**
     * 掩码显示账号信息
     */
    private fun maskAccount(account: String): String {
        return when {
            account.length <= 3 -> account
            account.contains("@") -> {
                // 邮箱掩码
                val parts = account.split("@")
                val username = parts[0]
                val domain = parts[1]
                val maskedUsername = if (username.length > 3) {
                    username.take(2) + "***" + username.takeLast(1)
                } else {
                    username
                }
                "$maskedUsername@$domain"
            }
            account.all { it.isDigit() } -> {
                // 手机号掩码
                if (account.length == 11) {
                    account.take(3) + "****" + account.takeLast(4)
                } else {
                    account.take(2) + "***" + account.takeLast(2)
                }
            }
            else -> {
                // 普通用户名掩码
                account.take(2) + "***" + account.takeLast(1)
            }
        }
    }
}

class PasswordDiffCallback : DiffUtil.ItemCallback<PasswordEntry>() {
    override fun areItemsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry): Boolean {
        return oldItem == newItem
    }
}