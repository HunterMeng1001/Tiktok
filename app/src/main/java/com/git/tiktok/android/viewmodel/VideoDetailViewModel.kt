package com.git.tiktok.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.git.tiktok.android.data.VideoItem

/**
 * 视频详情页ViewModel，负责管理UI状态和业务逻辑
 */
class VideoDetailViewModel : ViewModel() {
    
    // 当前视频Item
    private val _currentVideoItem = MutableLiveData<VideoItem>()
    val currentVideoItem: LiveData<VideoItem> = _currentVideoItem
    
    // 视频列表
    private val _videoList = MutableLiveData<List<VideoItem>>()
    val videoList: LiveData<List<VideoItem>> = _videoList
    
    // 当前位置
    private val _currentPosition = MutableLiveData(0)
    val currentPosition: LiveData<Int> = _currentPosition
    
    /**
     * 初始化视频数据
     */
    fun initVideoData(videoItem: VideoItem, videoList: List<VideoItem>, currentPosition: Int) {
        _currentVideoItem.value = videoItem
        _videoList.value = videoList
        _currentPosition.value = currentPosition
    }
    
    /**
     * 切换到下一个视频
     */
    fun switchToNextVideo(): Boolean {
        val currentPos = _currentPosition.value ?: 0
        val list = _videoList.value ?: emptyList()
        
        if (currentPos < list.size - 1) {
            val nextPos = currentPos + 1
            _currentPosition.value = nextPos
            _currentVideoItem.value = list[nextPos]
            return true
        }
        return false
    }
    
    /**
     * 切换到上一个视频
     */
    fun switchToPreviousVideo(): Boolean {
        val currentPos = _currentPosition.value ?: 0
        val list = _videoList.value ?: emptyList()
        
        if (currentPos > 0) {
            val prevPos = currentPos - 1
            _currentPosition.value = prevPos
            _currentVideoItem.value = list[prevPos]
            return true
        }
        return false
    }
    
    /**
     * 切换点赞状态
     */
    fun toggleLike(): VideoItem? {
        val currentItem = _currentVideoItem.value ?: return null
        
        val updatedItem = currentItem.copy(
            isLiked = !currentItem.isLiked,
            likeCount = if (currentItem.isLiked) currentItem.likeCount - 1 else currentItem.likeCount + 1
        )
        
        _currentVideoItem.value = updatedItem
        updateVideoList(updatedItem)
        return updatedItem
    }
    
    /**
     * 切换关注状态
     */
    fun toggleFollow(): VideoItem? {
        val currentItem = _currentVideoItem.value ?: return null
        
        val updatedItem = currentItem.copy(
            isFollowed = !currentItem.isFollowed
        )
        
        _currentVideoItem.value = updatedItem
        updateVideoList(updatedItem)
        return updatedItem
    }
    
    /**
     * 更新视频列表中的当前视频
     */
    private fun updateVideoList(updatedItem: VideoItem) {
        val list = _videoList.value ?: return
        val currentPos = _currentPosition.value ?: 0
        
        val updatedList = list.toMutableList()
        updatedList[currentPos] = updatedItem
        _videoList.value = updatedList
    }
    
    /**
     * 格式化数字，如1000 -> 1k
     */
    fun formatCount(count: Int): String {
        return when {
            count >= 10000 -> "${count / 10000}w"
            count >= 1000 -> "${count / 1000}k"
            else -> count.toString()
        }
    }
}