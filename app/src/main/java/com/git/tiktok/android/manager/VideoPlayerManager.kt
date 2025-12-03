package com.git.tiktok.android.manager

import android.content.Context
import android.view.View
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.DefaultLoadControl

/**
 * 视频播放管理器，封装ExoPlayer的初始化、播放、暂停和释放等操作
 * 后续优化建议：
 * 视频预加载：在视频播放前提前加载视频数据，减少播放延迟
 * 视频缓存：使用ExoPlayer的缓存机制，避免重复加载视频数据
 * 断电续播：在视频播放过程中，用户关闭应用或切换到其他应用，视频播放会暂停，用户再次打开应用后，视频会从上次暂停的位置继续播放
 */
class VideoPlayerManager(private val context: Context) {
    
    private var player: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null
    
    /**
     * 初始化播放器
     */
    fun initPlayer(playerView: PlayerView, onTouchListener: View.OnTouchListener? = null) {
        // 1. 关键修复：先释放旧的播放器实例，防止多个播放器同时播放
        if (player != null) {
            release()
        }
        
        // 2. 保存PlayerView引用
        this.playerView = playerView
        
        // 3. 创建LoadControl，优化缓冲区设置
        // 资源都比较短，参数改小一点
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1000, // 最小缓冲时间，增加到1秒
                4000, // 最大缓冲时间，增加到4秒
                1000, // 播放前缓冲时间，增加到1秒
                1000  // 重新缓冲时间，确保不超过最小缓冲时间
            )
            .build()
        
        // 4. 创建不带缓存的DataSource.Factory
        // 数据加载组件，用于从网络或本地加载视频数据
        val dataSourceFactory = DefaultDataSource.Factory(context)
        
        // 5. 创建Player实例
        val playerBuilder = SimpleExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(dataSourceFactory)
            )
        
        player = playerBuilder.build()
        
        // 6. 设置PlayerView
        playerView.player = player
        
        // 7. 禁用PlayerView的默认控制器，防止双击重新播放
        playerView.useController = false
        
        // 8. 设置触摸监听器
        onTouchListener?.let {
            playerView.setOnTouchListener(it)
        }
        
        // 9. 设置循环播放
        player?.repeatMode = ExoPlayer.REPEAT_MODE_ONE
        
        // 10. 确保音频正常播放
        player?.volume = 1.0f // 设置音量为最大
        player?.playWhenReady = true // 准备好后自动播放
    }
    
    /**
     * 播放视频
     */
    fun playVideo(videoUrl: String): Boolean {
        try {
            // 1. 验证URL有效性
            if (videoUrl.isEmpty() || !videoUrl.startsWith("http")) {
                throw IllegalArgumentException("Invalid video URL: $videoUrl")
            }
            
            // 2. 清理URL，去除可能存在的反引号
            val cleanUrl = videoUrl.replace("`", "")
            
            // 3. 检查播放器状态
            if (player == null) {
                // 如果播放器未初始化，直接返回
                return false
            }
            
            // 4. 重置播放器状态
            player?.stop()
            player?.clearMediaItems()
            
            // 5. 创建MediaItem
            val mediaItem = MediaItem.fromUri(cleanUrl)
            
            // 6. 设置MediaItem并准备播放器
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * 切换播放/暂停状态
     */
    fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }
    
    /**
     * 暂停播放
     */
    fun pause() {
        player?.pause()
    }
    
    /**
     * 释放播放器资源
     */
    fun release() {
        player?.let {
            it.stop()
            it.clearMediaItems()
            it.release()
        }
        player = null
        playerView?.player = null
        playerView = null
    }
    
    /**
     * 获取当前播放器实例
     */
    fun getPlayer(): SimpleExoPlayer? = player
    
    /**
     * 获取当前PlayerView
     */
    fun getPlayerView(): PlayerView? = playerView
}
