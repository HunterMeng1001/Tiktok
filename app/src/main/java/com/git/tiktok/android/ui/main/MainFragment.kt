package com.git.tiktok.android.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.git.tiktok.android.R
import com.google.android.material.tabs.TabLayout

/**
 * 首页Fragment，包含顶部导航栏和内部导航容器
 */
class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        
        // 获取内部NavHostFragment和导航控制器
        val homeNavHostFragment = childFragmentManager.findFragmentById(R.id.homeNavHostFragment) as NavHostFragment
        val homeNavController = homeNavHostFragment.navController
        
        // 获取顶部TabLayout
        val topTabLayout = view.findViewById<TabLayout>(R.id.homeTopTabLayout)
        
        // 配置顶部TabLayout，实现标签与Fragment的联动
        // 利用ai去做了一些空值处理、空指针异常的问题
        topTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // 根据选中的标签页切换对应的Fragment
                val fragmentId = when (tab?.position) {
                    0 -> R.id.recommendFragment  // 推荐
                    1 -> R.id.mallFragment       // 商城
                    2 -> R.id.liveFragment       // 直播
                    else -> R.id.recommendFragment
                }
                // 使用内部导航控制器切换Fragment
                homeNavController.navigate(fragmentId)
            }
            // ai去实现取消选中和重新选中的逻辑，优化视觉效果，另外添加了一个调研抖音的功能
            // 当在推荐页面点击推荐时，会刷新推荐页的数据
            override fun onTabUnselected(tab: TabLayout.Tab?) { /* 标签页取消选中 */ }
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // 标签页重新选中，处理推荐页刷新
                if (tab?.position == 0) {
                    // 获取当前Fragment
                    val currentFragment = homeNavHostFragment.childFragmentManager.primaryNavigationFragment
                    if (currentFragment is com.git.tiktok.android.ui.recommend.RecommendFragment) {
                        // 调用推荐页的刷新方法
                        // 导航栏3.0更新后，此功能废除，因为刷新数据的功能已经被移动到了推荐页的ViewModel中
                        // 后续有需要时，再考虑是否需要保留此功能
                        currentFragment.refreshData()
                    }
                }
            }
        })
        // bug修复：解决了在推荐页点击推荐后，顶部导航栏没有切换到推荐页的问题
        // 监听内部导航控制器的目的地变化，更新顶部导航栏的选中状态
        // 实现导航状态的反向联动，当内部Fragment切换时，顶部导航栏会自动切换到对应的标签页
        homeNavController.addOnDestinationChangedListener {
            _, destination, _ ->
            // 根据当前显示的Fragment更新顶部导航栏的选中状态
            val tabPosition = when (destination.id) {
                R.id.recommendFragment -> 0  // 推荐
                R.id.mallFragment -> 1        // 商城
                R.id.liveFragment -> 2        // 直播
                else -> 0                     // 默认推荐
            }
            
            // 更新顶部导航栏的选中状态
            topTabLayout.getTabAt(tabPosition)?.select()
        }
        
        return view
    }
}