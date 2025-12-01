package com.git.tiktok.android.ui.detail

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.git.tiktok.android.R
import com.git.tiktok.android.data.VideoItem
import com.git.tiktok.android.manager.VideoPlayerManager
import com.git.tiktok.android.viewmodel.VideoDetailViewModel
import com.google.android.exoplayer2.ui.PlayerView


/**
 * 视频详情页Activity，用于全屏展示视频或图片内容
 */
class VideoDetailActivity : AppCompatActivity() {

    // UI组件
    private lateinit var playerView: PlayerView
    private lateinit var ivImage: ImageView
    private lateinit var ivBack: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvVideoTitle: TextView
    private lateinit var tvLikeCount: TextView
    private lateinit var tvCommentCount: TextView
    private lateinit var tvShareCount: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var btnFollow: android.widget.Button
    private lateinit var ivLike: ImageView
    private lateinit var ivComment: ImageView
    private lateinit var ivShare: ImageView
    
    // ViewModel
    private lateinit var viewModel: VideoDetailViewModel
    
    // 视频播放管理器
    private lateinit var videoPlayerManager: VideoPlayerManager
    
    // 手势识别相关
    private val SWIPE_THRESHOLD = 100 // 滑动阈值，超过这个值才认为是滑动

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置全屏显示
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        setContentView(R.layout.activity_video_detail)
        
        // 初始化UI组件
        initViews()
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[VideoDetailViewModel::class.java]
        
        // 初始化视频播放管理器
        videoPlayerManager = VideoPlayerManager(this)
        
        // 获取传递过来的视频数据
        val videoItem = intent.getParcelableExtra<VideoItem>(EXTRA_VIDEO_ITEM) ?: return
        val videoList = intent.getParcelableArrayListExtra<VideoItem>(EXTRA_VIDEO_LIST) ?: listOf(videoItem)
        val currentPosition = intent.getIntExtra(EXTRA_CURRENT_POSITION, 0)
        
        // 初始化当前视频ID
        currentVideoId = videoItem.id
        
        // 初始化ViewModel数据
        viewModel.initVideoData(videoItem, videoList, currentPosition)
        
        // 观察ViewModel变化，更新UI
        observeViewModel()
        
        // 设置点击事件
        setClickListeners()
        
        // 开始展示内容
        showContent()
    }
    
    /**
     * 初始化UI组件引用
     */
    private fun initViews() {
        playerView = findViewById(R.id.playerView)
        ivImage = findViewById(R.id.ivImage)
        ivBack = findViewById(R.id.ivBack)
        tvUsername = findViewById(R.id.tvUsername)
        tvVideoTitle = findViewById(R.id.tvVideoTitle)
        tvLikeCount = findViewById(R.id.tvLikeCount)
        tvCommentCount = findViewById(R.id.tvCommentCount)
        tvShareCount = findViewById(R.id.tvShareCount)
        ivAvatar = findViewById(R.id.ivAvatar)
        btnFollow = findViewById(R.id.btnFollow)
        ivLike = findViewById(R.id.ivLike)
        ivComment = findViewById(R.id.ivComment)
        ivShare = findViewById(R.id.ivShare)
    }
    
    /**
     * 观察ViewModel变化，更新UI
     */
    private fun observeViewModel() {
        viewModel.currentVideoItem.observe(this) {
            // 更新UI
            updateUI(it)
            
            // 更新共享元素的transitionName
            playerView.transitionName = "video_cover_${it.id}"
            ivImage.transitionName = "video_cover_${it.id}"
            
            // 只有当视频ID变化时才重新加载视频，避免点赞时重新加载
            if (currentVideoId != it.id) {
                // 重新展示内容
                showContent()
                // 更新当前视频ID
                currentVideoId = it.id
            }
        }
    }

    /**
     * 更新UI组件
     */
    private fun updateUI(videoItem: VideoItem) {
        // 设置视频信息
        tvUsername.text = videoItem.username
        tvVideoTitle.text = videoItem.title
        tvLikeCount.text = viewModel.formatCount(videoItem.likeCount)
        tvCommentCount.text = viewModel.formatCount(videoItem.commentCount)
        tvShareCount.text = viewModel.formatCount(videoItem.shareCount)
        
        // 加载用户头像
        Glide.with(this)
            .load(videoItem.avatarUrl)
            .circleCrop()
            .into(ivAvatar)
        
        // 设置关注按钮状态
        btnFollow.text = if (videoItem.isFollowed) "已关注" else "关注"
        
        // 设置点赞图标状态
        if (videoItem.isLiked) {
            ivLike.setImageResource(R.drawable.ic_heart_red)
        } else {
            ivLike.setImageResource(R.drawable.ic_heart_gray)
        }
    }

    // 双击检测相关
    private var clickCount = 0
    private var lastClickTime = 0L
    private val DOUBLE_CLICK_TIMEOUT = 300L // 双击超时时间，毫秒
    private var startY = 0f // 触摸起始Y坐标
    private var isScrolling = false // 是否正在滑动
    private var currentVideoId: String? = null // 当前视频ID，用于判断是否需要重新加载视频
    private var clickHandler: Handler = Handler(Looper.getMainLooper()) // 用于处理延迟点击事件
    
    /**
     * 设置点击事件监听器
     */
    private fun setClickListeners() {
        // 返回按钮点击事件
        ivBack.setOnClickListener {
            finish()
        }
        
        // 关注按钮点击事件
        btnFollow.setOnClickListener {
            viewModel.toggleFollow()
        }
        
        // 点赞按钮点击事件
        ivLike.setOnClickListener {
            viewModel.toggleLike()
        }
        
        // 评论按钮点击事件
        ivComment.setOnClickListener {
            // TODO: 实现评论功能
        }
        
        // 分享按钮点击事件
        ivShare.setOnClickListener {
            // TODO: 实现分享功能
        }
    }
    
    /**
     * 处理触摸事件，主要用于传递给其他视图
     */
    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        // 触摸事件已经在PlayerView的OnTouchListener中处理
        return super.onTouchEvent(event)
    }
    
    /**
     * 切换到下一个元素
     */
    private fun switchToNextItem() {
        if (viewModel.switchToNextVideo()) {
            // 切换前先释放媒体控制器，防止资源泄漏和冲突
            videoPlayerManager.release()
        }
    }
    
    /**
     * 切换到上一个元素
     */
    private fun switchToPreviousItem() {
        if (viewModel.switchToPreviousVideo()) {
            // 切换前先释放媒体控制器，防止资源泄漏和冲突
            videoPlayerManager.release()
        }
    }
    
    /**
     * 在Activity恢复时重新初始化播放器并开始播放
     */
    override fun onResume() {
        super.onResume()
        showContent()
    }
    
    /**
     * 在Activity暂停时暂停播放，但不释放资源
     */
    override fun onPause() {
        super.onPause()
        
        // 暂停视频播放
        videoPlayerManager.pause()
    }
    
    /**
     * 在Activity销毁时释放资源，防止WindowLeaked错误
     */
    override fun onDestroy() {
        super.onDestroy()
        
        // 释放ExoPlayer资源
        videoPlayerManager.release()
    }

    /**
     * 根据内容类型展示视频或图片
     */
    private fun showContent() {
        val videoItem = viewModel.currentVideoItem.value ?: return
        
        if (videoItem.videoUrl.isNotEmpty()) {
            // 视频内容
            showVideo(videoItem.videoUrl)
        } else {
            // 图片内容
            showImage(videoItem.coverUrl)
        }
    }

    /**
     * 展示视频内容
     */
    private fun showVideo(videoUrl: String) {
        // 显示PlayerView，隐藏ImageView
        playerView.visibility = View.VISIBLE
        ivImage.visibility = View.GONE
        
        // 先释放旧的播放器资源
        videoPlayerManager.release()
        
        // 初始化播放器，不设置OnTouchListener，避免覆盖OnClickListener
        videoPlayerManager.initPlayer(playerView)
        
        // 播放视频
        videoPlayerManager.playVideo(videoUrl)
        
        // 重置触摸相关状态
        clickCount = 0
        lastClickTime = 0L
        isScrolling = false
        // 移除所有延迟任务
        clickHandler.removeCallbacksAndMessages(null)
        
        // 为PlayerView添加触摸监听器，处理点击和滑动事件
        playerView.setOnTouchListener { _, event ->
            when (event.action) {
                // 触摸开始
                android.view.MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                    isScrolling = false
                    return@setOnTouchListener true
                }
                // 触摸移动
                android.view.MotionEvent.ACTION_MOVE -> {
                    val deltaY = Math.abs(event.y - startY)
                    // 如果移动距离超过阈值，认为是滑动
                    if (deltaY > SWIPE_THRESHOLD) {
                        isScrolling = true
                        // 移除之前的点击延迟任务
                        clickHandler.removeCallbacksAndMessages(null)
                    }
                    return@setOnTouchListener true
                }
                // 触摸结束
                android.view.MotionEvent.ACTION_UP -> {
                    val deltaY = event.y - startY
                    
                    // 如果是滑动事件，处理上下滑动
                    if (isScrolling || Math.abs(deltaY) > SWIPE_THRESHOLD) {
                        if (deltaY < -SWIPE_THRESHOLD) {
                            // 向上滑动，切换到下一个元素
                            switchToNextItem()
                        } else if (deltaY > SWIPE_THRESHOLD) {
                            // 向下滑动，切换到上一个元素
                            switchToPreviousItem()
                        }
                    } else {
                        // 点击事件，处理单击和双击
                        clickCount++
                        
                        if (clickCount == 1) {
                            // 第一次点击，延迟判断是单击还是双击
                            clickHandler.postDelayed({
                                if (clickCount == 1) {
                                    // 单击事件：切换播放/暂停
                                    videoPlayerManager.togglePlayPause()
                                }
                                clickCount = 0
                            }, DOUBLE_CLICK_TIMEOUT)
                        } else if (clickCount == 2) {
                            // 双击事件：点赞，不重新播放视频
                            viewModel.toggleLike()
                            // 移除之前的点击延迟任务
                            clickHandler.removeCallbacksAndMessages(null)
                            clickCount = 0
                        }
                    }
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
    }
    
    /**
     * 展示图片内容
     */
    private fun showImage(coverUrl: String) {
        // 显示ImageView，隐藏PlayerView
        ivImage.visibility = View.VISIBLE
        playerView.visibility = View.GONE
        
        // 重置触摸相关状态
        clickCount = 0
        lastClickTime = 0L
        isScrolling = false
        // 移除所有延迟任务
        clickHandler.removeCallbacksAndMessages(null)
        
        // 为ImageView添加触摸监听器，处理点击和滑动事件
        ivImage.setOnTouchListener { _, event ->
            when (event.action) {
                // 触摸开始
                android.view.MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                    isScrolling = false
                    return@setOnTouchListener true
                }
                // 触摸移动
                android.view.MotionEvent.ACTION_MOVE -> {
                    val deltaY = Math.abs(event.y - startY)
                    // 如果移动距离超过阈值，认为是滑动
                    if (deltaY > SWIPE_THRESHOLD) {
                        isScrolling = true
                    }
                    return@setOnTouchListener true
                }
                // 触摸结束
                android.view.MotionEvent.ACTION_UP -> {
                    val deltaY = event.y - startY
                    
                    // 如果是滑动事件，处理上下滑动
                    if (isScrolling || Math.abs(deltaY) > SWIPE_THRESHOLD) {
                        if (deltaY < -SWIPE_THRESHOLD) {
                            // 向上滑动，切换到下一个元素
                            switchToNextItem()
                        } else if (deltaY > SWIPE_THRESHOLD) {
                            // 向下滑动，切换到上一个元素
                            switchToPreviousItem()
                        }
                    }
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
        
        // 加载图片
        Glide.with(this)
            .load(coverUrl)
            .centerCrop()
            .into(ivImage)
    }

    companion object {
        // 传递视频数据的键
        const val EXTRA_VIDEO_ITEM = "extra_video_item"
        // 传递视频列表的键
        const val EXTRA_VIDEO_LIST = "extra_video_list"
        // 传递当前位置的键
        const val EXTRA_CURRENT_POSITION = "extra_current_position"
        
        /**
         * 创建跳转到视频详情页的Intent
         */
        fun createIntent(context: android.content.Context, videoItem: VideoItem): Intent {
            return Intent(context, VideoDetailActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_ITEM, videoItem)
            }
        }
        
        /**
         * 创建跳转到视频详情页的Intent，包含视频列表和当前位置
         */
        fun createIntent(context: android.content.Context, videoList: List<VideoItem>, currentPosition: Int): Intent {
            return Intent(context, VideoDetailActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_ITEM, videoList[currentPosition])
                putParcelableArrayListExtra(EXTRA_VIDEO_LIST, ArrayList(videoList))
                putExtra(EXTRA_CURRENT_POSITION, currentPosition)
            }
        }
    }
}