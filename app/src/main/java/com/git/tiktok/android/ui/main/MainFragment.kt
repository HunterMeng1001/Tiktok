package com.git.tiktok.android.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.git.tiktok.android.R
import com.git.tiktok.android.ui.home.HomeViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

/**
 * 首页Fragment
 */
class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化ViewPager2和TabLayout
        val viewPager2 = view.findViewById<ViewPager2>(R.id.topViewPager2)
        val tabLayout = view.findViewById<TabLayout>(R.id.topTabLayout)

        // 创建并设置适配器
        val adapter = HomeViewPagerAdapter(requireActivity())
        viewPager2.adapter = adapter

        // 关联TabLayout和ViewPager2，实现相互联动
        TabLayoutMediator(tabLayout, viewPager2) {
            tab, position ->
            // 设置Tab的标题
            tab.text = adapter.tabTitles[position]
        }.attach()
    }
}