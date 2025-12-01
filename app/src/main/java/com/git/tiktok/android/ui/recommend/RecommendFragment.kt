package com.git.tiktok.android.ui.recommend

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.git.tiktok.android.R
import com.git.tiktok.android.data.VideoItem
import com.git.tiktok.android.viewmodel.RecommendViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * 推荐页Fragment，显示推荐feed
 */
class RecommendFragment : Fragment() {
    // 可滚动的TabLayout
    private lateinit var tabLayout: TabLayout
    // 可滚动的ViewPager2，页面切换容器
    private lateinit var viewPager: ViewPager2
    // 分类页的ViewPager2适配器
    private lateinit var categoryPagerAdapter: CategoryPagerAdapter
    // 用于处理UI更新的Handler(下拉刷新的延迟)
    private val handler = Handler(Looper.getMainLooper())
    // 推荐ViewModel，用于管理推荐数据
    lateinit var viewModel: RecommendViewModel
    // 防止重复加载
    private var isLoading = false

    // 分类列表
    private val categories = listOf(
        "推荐", "关注", "热点", "体育", "科技", "美食", "旅行", "时尚", "音乐", "舞蹈"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recommend, container, false)
        
        // 初始化TabLayout
        tabLayout = view.findViewById(R.id.tabLayout)
        
        // 初始化ViewPager2
        viewPager = view.findViewById(R.id.viewPager)
        
        return view
    }
    // ai优化：在onViewCreated中初始化ViewModel
    // 首次创建时加载数据，避免重复切换导航页刷新数据
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[RecommendViewModel::class.java]
        
        // 只有在首次创建时加载数据
        if (!viewModel.isDataLoaded.value!!) {
            // 创建模拟视频数据
            val videoList = createMockVideoList(0)
            viewModel.setVideoList(videoList)
        }
        
        // 初始化适配器
        categoryPagerAdapter = CategoryPagerAdapter(requireActivity(), categories)
        viewPager.adapter = categoryPagerAdapter
        
        // 关联TabLayout和ViewPager2，实现双向联动
        // 当用户滑动ViewPager2时，TabLayout会自动切换到对应的标签页
        // 当用户点击TabLayout时，ViewPager2会自动切换到对应的页面
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categories[position]
        }.attach()
    }
    
    /**
     * 预留接口，用于外部触发推荐页刷新，导航栏版本更新后遗留，考虑是否保留
     */
    fun refreshData() {
        // 这里可以添加刷新逻辑，例如通知所有Fragment刷新数据
        isLoading = false
    }
    
    /**
     * 预留接口，用于外部触发推荐页加载更多数据，导航栏版本更新后遗留，考虑是否保留
     */
    private fun loadMoreData() {
        isLoading = true
        
        // 模拟网络请求，延迟1秒后加载更多数据
        handler.postDelayed({
            val currentCount = viewModel.getCurrentListSize()
            val moreVideoList = createMockVideoList(currentCount)
            viewModel.addVideoList(moreVideoList)
            isLoading = false
        }, 1000)
    }
    
    /**
     * 创建模拟视频数据列表，使用用户提供的图片素材
     */
    // ai优化：从OSS上拿到数据，算是一个创新点吧
    private fun createMockVideoList(startIndex: Int): List<VideoItem> {
        // 用户提供的图片URL列表
        val imageUrls = listOf(
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/1/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251130135105_25_145.jpg",
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/1/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251130135107_26_145.jpg",
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/1/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251130135110_28_145.jpg",
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/1/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251130135111_29_145.jpg",
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/1/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251130135112_30_145.jpg",
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/1/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251130135113_31_145.jpg",
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/1/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251130135114_32_145.jpg",
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/1/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251130135123_34_145.jpg"
        )
        
        // 为每个图片生成的标题列表
        val titles = listOf(
            "时尚穿搭：秋季新款潮流搭配",
            "美食分享：自制美味蛋糕教程",
            "旅行日记：探索未知的风景",
            "健身日常：坚持锻炼的第30天",
            "宠物萌照：可爱猫咪的日常",
            "手工制作：DIY创意小物件",
            "摄影技巧：如何拍出好看的照片",
            "生活感悟：珍惜当下的美好时光"
        )
        
        val videoList = mutableListOf<VideoItem>()
        
        // 每次生成8条新数据，对应所有图片
        for (i in 0 until imageUrls.size) {
            val index = startIndex + i + 1
            // 循环使用图片和标题，确保每次加载都有新内容
            val imageIndex = i % imageUrls.size
            val titleIndex = i % titles.size
            videoList.add(
                VideoItem(
                    id = index.toString(),
                    title = titles[titleIndex],
                    coverUrl = imageUrls[imageIndex], // 使用图片URL作为封面
                    videoUrl = "", // 图片内容，不需要视频URL
                    likeCount = 1234 + index * 100,
                    commentCount = 56 + index * 5,
                    shareCount = 78 + index * 3,
                    username = "抖音用户$index",
                    avatarUrl = "https://avatars.githubusercontent.com/u/$index?v=4"
                )
            )
        }
        
        // 固定顺序，不再随机排列
        return videoList
    }
    
    
}