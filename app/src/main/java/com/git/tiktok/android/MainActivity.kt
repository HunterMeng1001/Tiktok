package com.git.tiktok.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取NavHostFragment和导航控制器
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 配置底部导航栏
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        // 遍历所有菜单项，移除图标（实现纯文字导航）
        bottomNavigationView.menu.forEach { item ->
            item.setIcon(null)
        }
        
        // 配置底部导航栏与导航控制器的联动
        bottomNavigationView.setupWithNavController(navController)
    }
}