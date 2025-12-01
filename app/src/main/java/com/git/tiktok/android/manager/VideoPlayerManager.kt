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
 */
class VideoPlayerManager(private val context: Context) {
    
    private var player: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null
    
    /**
     * 初始化播放器
     */
    fun initPlayer(playerView: PlayerView, onTouchListener: View.OnTouchListener? = null) {
        this.playerView = playerView
        
        // 创建LoadControl，优化缓冲区设置
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                5000, // 最小缓冲时间，增加到5秒
                15000, // 最大缓冲时间，增加到15秒
                2000, // 播放前缓冲时间，增加到2秒
                3000  // 重新缓冲时间，增加到3秒
            )
            .build()
        
        // 创建不带缓存的DataSource.Factory
        val dataSourceFactory = DefaultDataSource.Factory(context)
        
        // 创建Player实例
        val playerBuilder = SimpleExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(dataSourceFactory)
            )
        
        player = playerBuilder.build()
        
        // 设置PlayerView
        playerView.player = player
        
        // 禁用PlayerView的默认控制器，防止双击重新播放
        playerView.useController = false
        
        // 设置触摸监听器
        onTouchListener?.let {
            playerView.setOnTouchListener(it)
        }
        
        // 设置循环播放
        player?.repeatMode = ExoPlayer.REPEAT_MODE_ONE
        
        // 确保音频正常播放
        player?.volume = 1.0f // 设置音量为最大
        player?.playWhenReady = true // 准备好后自动播放
    }
    
    /**
     * 播放视频
     */
    fun playVideo(videoUrl: String) {
        try {
            // 清理URL，去除可能存在的反引号
            val cleanUrl = videoUrl.replace("`", "")
            
            // 创建MediaItem
            val mediaItem = MediaItem.fromUri(cleanUrl)
            
            if (player == null) {
                // 如果播放器未初始化，直接返回
                return
            }
            
            // 设置MediaItem并准备播放器
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
        } catch (e: Exception) {
            e.printStackTrace()
            // 处理URL解析错误
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
