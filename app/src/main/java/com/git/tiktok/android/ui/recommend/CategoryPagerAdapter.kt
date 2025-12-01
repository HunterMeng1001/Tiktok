package com.git.tiktok.android.ui.recommend

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * ViewPager2适配器，用于管理不同分类的Fragment
 */
class CategoryPagerAdapter(fragmentActivity: FragmentActivity, private val categories: List<String>) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun createFragment(position: Int): Fragment {
        // 根据位置创建对应的分类Fragment
        return CategoryFragment(categories[position])
    }
}