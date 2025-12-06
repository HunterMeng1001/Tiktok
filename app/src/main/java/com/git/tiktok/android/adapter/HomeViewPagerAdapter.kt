package com.git.tiktok.android.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 首页顶部导航的ViewPager2适配器
 * 用于管理推荐、关注、本地三个Fragment
 */
class HomeViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    // 顶部导航的标题列表
    val tabTitles = listOf("推荐", "关注", "本地")

    /**
     * 返回Fragment的数量
     */
    override fun getItemCount(): Int {
        return tabTitles.size
    }

    /**
     * 创建并返回对应位置的Fragment
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RecommendFragment.newInstance()
            1 -> FollowFragment.newInstance()
            2 -> LocalFragment.newInstance()
            else -> RecommendFragment.newInstance()
        }
    }
}