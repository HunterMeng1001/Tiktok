package com.git.tiktok.android.adapter

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.git.tiktok.android.R
import com.git.tiktok.android.data.VideoItem

/**
 * 视频适配器，用于RecyclerView的视频列表展示
 */
class VideoAdapter : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    // 点击监听器接口
    interface OnVideoClickListener {
        fun onVideoClick(videoList: List<VideoItem>, position: Int)
    }
    
    // 点击监听器
    private var onVideoClickListener: OnVideoClickListener? = null
    
    // 使用可变列表存储视频数据，支持动态更新
    private val videoList: MutableList<VideoItem> = mutableListOf()
    
    /**
     * 设置点击监听器
     */
    fun setOnVideoClickListener(listener: OnVideoClickListener) {
        this.onVideoClickListener = listener
    }

    /**
     * 视频ViewHolder，缓存视频卡片的UI组件
     */
    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivVideoCover: ImageView = itemView.findViewById(R.id.ivVideoCover)
        val videoView: VideoView = itemView.findViewById(R.id.videoView)
        val tvVideoTitle: TextView = itemView.findViewById(R.id.tvVideoTitle)
        val ivLikeButton: ImageView = itemView.findViewById(R.id.ivLikeButton)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)

        val videoContainer: FrameLayout = itemView.findViewById(R.id.videoContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_card, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        
        // 绑定数据到UI组件
        holder.tvVideoTitle.text = video.title
        holder.tvLikeCount.text = formatCount(video.likeCount)
        
        // 设置点赞图标状态
        if (video.isLiked) {
            holder.ivLikeButton.setImageResource(R.drawable.ic_heart_red)
        } else {
            holder.ivLikeButton.setImageResource(R.drawable.ic_heart_gray)
        }
        
        // 加载封面图片
        val coverUrl = if (video.videoUrl.isNotEmpty()) {
            // 如果有视频URL，优先使用视频URL获取封面
            video.videoUrl
        } else {
            // 否则使用封面URL
            video.coverUrl
        }
        
        // 隐藏VideoView，避免影响布局高度
        holder.videoView.visibility = View.GONE
        
        // 加载封面，使用centerCrop和adjustViewBounds实现自适应高度
        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(coverUrl)
            .apply {
                // 如果是视频URL，取视频第一帧作为封面
                if (video.videoUrl.isNotEmpty()) {
                    frame(0) // 0毫秒，即第一帧
                }
                placeholder(R.color.darker_gray)
                centerCrop()
            }
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // 计算图片的宽高比
                    val aspectRatio = resource.width.toFloat() / resource.height.toFloat()
                    
                    // 获取屏幕宽度，计算每个item的宽度
                    val displayMetrics = holder.itemView.context.resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val itemWidth = (screenWidth - 24) / 2 // 减去间距和边距
                    
                    // 根据宽高比计算item高度
                    val itemHeight = itemWidth / aspectRatio
                    
                    // 设置ImageView的高度
                    val layoutParams = holder.ivVideoCover.layoutParams
                    layoutParams.height = itemHeight.toInt()
                    holder.ivVideoCover.layoutParams = layoutParams
                    
                    // 设置图片
                    holder.ivVideoCover.setImageBitmap(resource)
                    
                    // 也设置VideoView的高度，保持一致
                    val videoLayoutParams = holder.videoView.layoutParams
                    videoLayoutParams.height = itemHeight.toInt()
                    holder.videoView.layoutParams = videoLayoutParams
                }
            })
        

        
        // 视频播放逻辑
        if (video.videoUrl.isNotEmpty()) {
            setupVideoPlayback(holder, video)
        }
        
        // 点赞按钮点击事件
        holder.ivLikeButton.setOnClickListener {
            toggleLike(holder, video)
        }
        
        // 设置封面图片的transitionName，用于共享元素转场
        holder.ivVideoCover.transitionName = "video_cover_${video.id}"
        
        // 封面点击事件，调用监听器
        holder.ivVideoCover.setOnClickListener {
            onVideoClickListener?.onVideoClick(videoList, position)
        }
        
        // 实现单击屏幕暂停播放和双击点赞功能
        holder.videoContainer.setOnTouchListener(object : View.OnTouchListener {
            private var lastClickTime = 0L
            private var clickCount = 0
            
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    clickCount++
                    val currentTime = System.currentTimeMillis()
                    
                    if (clickCount == 1) {
                        // 第一次点击，延迟判断是单击还是双击
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (clickCount == 1) {
                                // 单击事件
                                if (holder.videoView.isPlaying) {
                            // 暂停播放
                            holder.videoView.pause()
                        } else {
                            // 开始播放
                            holder.videoView.start()
                        }
                            }
                            clickCount = 0
                        }, 300)
                    } else if (clickCount == 2) {
                        // 双击事件
                        toggleLike(holder, video)
                        showLikeAnimation(holder)
                        clickCount = 0
                    }
                    
                    lastClickTime = currentTime
                }
                return true
            }
        })
    }

    override fun getItemCount(): Int = videoList.size

    /**
     * 设置视频播放逻辑
     */
    private fun setupVideoPlayback(holder: VideoViewHolder, video: VideoItem) {
        // 设置视频URL
        holder.videoView.setVideoPath(video.videoUrl)
        
        // 确保音频正常播放
        holder.videoView.setAudioAttributes(
            android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .build()
        )
        
        // 视频播放完成回调
        holder.videoView.setOnCompletionListener {
            // 重置视频，准备重新播放
            holder.videoView.seekTo(0)
        }
        
        // 视频准备完成回调
        holder.videoView.setOnPreparedListener {
            // 确保音频正常播放
            it.setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }

    /**
     * 格式化数字，如1000 -> 1k
     */
    private fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> "${count / 10000}w"
            count >= 1000 -> "${count / 1000}k"
            else -> count.toString()
        }
    }

    /**
     * 设置新的数据列表，用于下拉刷新
     */
    fun setData(newVideoList: List<VideoItem>) {
        videoList.clear()
        videoList.addAll(newVideoList)
        notifyDataSetChanged()
    }

    /**
     * 添加更多数据，用于上拉加载更多
     */
    fun addData(moreVideoList: List<VideoItem>) {
        val startPosition = videoList.size
        videoList.addAll(moreVideoList)
        notifyItemRangeInserted(startPosition, moreVideoList.size)
    }
    
    /**
     * 切换点赞状态
     */
    private fun toggleLike(holder: VideoViewHolder, video: VideoItem) {
        video.isLiked = !video.isLiked
        if (video.isLiked) {
            video.likeCount++
            holder.ivLikeButton.setImageResource(R.drawable.ic_heart_red)
        } else {
            video.likeCount--
            holder.ivLikeButton.setImageResource(R.drawable.ic_heart_gray)
        }
        holder.tvLikeCount.text = formatCount(video.likeCount)
    }
    
    /**
     * 显示点赞动画
     */
    private fun showLikeAnimation(holder: VideoViewHolder) {
        val likeAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.like_animation)
        val animationView = ImageView(holder.itemView.context)
        animationView.setImageResource(R.drawable.ic_heart_red)
        animationView.layoutParams = FrameLayout.LayoutParams(100, 100, android.view.Gravity.CENTER)
        holder.videoContainer.addView(animationView)
        
        likeAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            
            override fun onAnimationEnd(animation: Animation?) {
                holder.videoContainer.removeView(animationView)
            }
            
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        
        animationView.startAnimation(likeAnimation)
    }
    

}