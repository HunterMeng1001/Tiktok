package com.git.tiktok.android.ui.recommend

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.git.tiktok.android.R
import com.git.tiktok.android.adapter.VideoAdapter
import com.git.tiktok.android.data.VideoItem
import com.git.tiktok.android.viewmodel.RecommendViewModel

/**
 * 分类页Fragment，显示不同分类的feed
 */
class CategoryFragment(private val categoryName: String) : Fragment() {

    private lateinit var recyclerView: RecyclerView
    // 下拉刷新的核心实现
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    // 视频列表适配器
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var layoutManager: StaggeredGridLayoutManager
    private var isLoading = false // 防止重复加载
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var viewModel: RecommendViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)
        
        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        
        // 初始化SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        
        // 设置抖音主题色的刷新动画
        swipeRefreshLayout.setColorSchemeResources(
            R.color.douyin_red, // 抖音红色
            R.color.douyin_white, // 抖音白色
            R.color.douyin_black // 抖音黑色
        )
        
        swipeRefreshLayout.setOnRefreshListener {
            // 下拉刷新回调
            refreshData()
        }
        
        // 配置StaggeredGridLayoutManager（双列垂直瀑布流）
        layoutManager = StaggeredGridLayoutManager(
            2, StaggeredGridLayoutManager.VERTICAL
        )
        
        // 设置间隙策略，避免瀑布流布局错乱
        // fixme: 这个策略可能会导致布局在刷新或者上拉加载时item跳动，后续考虑优化
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        
        recyclerView.layoutManager = layoutManager
        
        // ai优化: 为了避免item跳动，这里设置item间距为4dp
        // 添加ItemDecoration，设置item间距
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(4, 4, 4, 4) // 设置item间距
            }
        })
        
        // 以下代码为ai实现
        // 设置适配器
        videoAdapter = VideoAdapter()
        // 设置视频点击监听器
        videoAdapter.setOnVideoClickListener(object : VideoAdapter.OnVideoClickListener {
            override fun onVideoClick(videoList: List<VideoItem>, position: Int) {
                // 跳转到视频详情页，传递视频列表和当前位置
                val intent = com.git.tiktok.android.ui.detail.VideoDetailActivity.createIntent(
                    requireContext(),
                    videoList,
                    position
                )
                // 添加共享元素转场动画，可能跟网络有关，在线浏览时会有延迟，导致转场动画不流畅？
                // 后续考虑优化
                requireActivity().startActivity(
                    intent,
                    android.app.ActivityOptions.makeSceneTransitionAnimation(
                        requireActivity(),
                        recyclerView.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<android.widget.ImageView>(R.id.ivVideoCover),
                        "video_cover_${videoList[position].id}"
                    ).toBundle()
                )
            }
        })
        recyclerView.adapter = videoAdapter
        
        // 设置滚动监听器，实现上拉加载更多，ai实现逻辑
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                // 只有向下滚动时才处理加载更多
                if (dy <= 0) return
                
                // 获取最后一个可见的item位置
                val lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null)
                val lastVisibleItemPosition = getLastVisibleItemPosition(lastVisibleItemPositions)
                val totalItemCount = layoutManager.itemCount
                
                // 当滚动到倒数第2个item时，开始加载更多
                if (!isLoading && lastVisibleItemPosition >= totalItemCount - 2) {
                    loadMoreData()
                }
            }
        })
        
        return view
    }
    
    // 防止反复切换导航栏时，重复加载数据
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化ViewModel
        viewModel = (activity as? RecommendFragment)?.viewModel ?: RecommendViewModel()
        
        // 观察视频列表数据变化
        viewModel.videoList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                videoAdapter.setData(it)
            }
        }
        
        // 只有在首次创建时加载数据
        if (!viewModel.isDataLoaded.value!!) {
            // 创建模拟视频数据
            val videoList = createMockVideoList(0)
            viewModel.setVideoList(videoList)
        }
    }
    
    /**
     * 获取最后一个可见的item位置
     */
    private fun getLastVisibleItemPosition(lastVisibleItemPositions: IntArray): Int {
        var maxPosition = lastVisibleItemPositions[0]
        for (position in lastVisibleItemPositions) {
            if (position > maxPosition) {
                maxPosition = position
            }
        }
        return maxPosition
    }
    
    /**
     * 下拉刷新数据，ai实现逻辑与动画
     */
    private fun refreshData() {
        isLoading = true
        
        // 显示刷新动画
        swipeRefreshLayout.isRefreshing = true
        
        // 模拟网络请求，延迟1秒后刷新数据
        handler.postDelayed({
            val newVideoList = createMockVideoList(0)
            // 刷新时对数据进行随机排序
            viewModel.setVideoList(newVideoList.shuffled())
            swipeRefreshLayout.isRefreshing = false
            isLoading = false
        }, 1000)
    }
    
    /**
     * 上拉加载更多数据
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
     * 创建模拟视频数据列表，使用用户提供的图片素材和视频素材
     */
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
        
        // 使用一个已知有声音的测试视频URL
        val videoUrls = listOf(
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/Captures/25.mp4",
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/Captures/6.mp4",
            "https://miniblog-oss1.oss-cn-wuhan-lr.aliyuncs.com/Captures/89.mp4"
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
            "生活感悟：珍惜当下的美好时光",
            "精彩视频：动感舞蹈表演",
            "音乐分享：最新流行歌曲",
            "搞笑视频：幽默短剧"
        )
        
        val videoList = mutableListOf<VideoItem>()
        var videoCounter = 0
        
        // 每次生成11条新数据，包含8张图片和3个视频
        for (i in 0 until (imageUrls.size + videoUrls.size)) {
            val index = startIndex + i + 1
            // 循环使用图片和标题，确保每次加载都有新内容
            val imageIndex = i % imageUrls.size
            val titleIndex = i % titles.size
            
            // 每3个项中插入1个视频，其余为图片
            val isVideo = i % 3 == 0
            
            // 只有当有可用视频且未用完时才添加视频
            val currentVideoUrl = if (isVideo && videoCounter < videoUrls.size) {
                videoUrls[videoCounter++]
            } else {
                ""
            }
            
            videoList.add(
                VideoItem(
                    id = index.toString(),
                    title = titles[titleIndex],
                    coverUrl = imageUrls[imageIndex], // 使用图片URL作为封面
                    videoUrl = currentVideoUrl, // 根据条件设置视频URL
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