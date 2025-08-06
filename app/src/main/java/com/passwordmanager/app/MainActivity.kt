package com.passwordmanager.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.passwordmanager.app.databinding.ActivityMainBinding

/**
 * 主Activity
 * 承载底部导航栏和Fragment容器
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setupBottomNavigation()
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果绑定失败，使用简单布局
            setContentView(R.layout.activity_main)
            setupBottomNavigationFallback()
        }
    }

    /**
     * 设置底部导航栏
     */
    private fun setupBottomNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
    }

    /**
     * 备用导航设置
     */
    private fun setupBottomNavigationFallback() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        // 将底部导航与NavController关联
        navView.setupWithNavController(navController)
        
        // 设置导航动画
        setupNavigationAnimations(navController)
    }

    /**
     * 设置导航动画
     */
    private fun setupNavigationAnimations(navController: androidx.navigation.NavController) {
        // 可以在这里添加自定义的页面切换动画
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 根据目标页面执行相应的动画或设置
            when (destination.id) {
                R.id.recordFragment -> {
                    // 记录页面的特殊设置
                }
                R.id.searchFragment -> {
                    // 查询页面的特殊设置
                }
                R.id.profileFragment -> {
                    // 个人中心页面的特殊设置
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 应用回到前台时的处理
    }

    override fun onPause() {
        super.onPause()
        // 应用进入后台时的处理
    }

    /**
     * 获取当前显示的Fragment ID
     */
    fun getCurrentFragmentId(): Int {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.currentDestination?.id ?: R.id.recordFragment
    }

    /**
     * 导航到指定Fragment
     */
    fun navigateToFragment(fragmentId: Int) {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        if (navController.currentDestination?.id != fragmentId) {
            navController.navigate(fragmentId)
        }
    }

    /**
     * 设置自定义背景
     */
    fun setCustomBackground(backgroundPath: String?, opacity: Float) {
        // TODO: 实现自定义背景设置逻辑
        // 这里可以加载背景图片并应用到主容器
    }

    /**
     * 切换主题模式
     */
    fun switchTheme(themeMode: String) {
        // TODO: 实现主题切换逻辑
        // 可以使用AppCompatDelegate.setDefaultNightMode()
    }
    
    /**
     * 应用自定义背景
     */
    fun applyCustomBackground() {
        try {
            val backgroundManager = com.passwordmanager.app.utils.BackgroundManager(this)
            val customBackground = backgroundManager.getCustomBackground()
            
            if (customBackground != null) {
                findViewById<View>(R.id.container)?.background = customBackground
            } else {
                findViewById<View>(R.id.container)?.background = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}