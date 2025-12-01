package com.git.tiktok.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.git.tiktok.android.data.VideoItem

/**
 * 推荐页ViewModel，用于保存推荐页的数据状态
 */
class RecommendViewModel : ViewModel() {
    
    // 保存视频列表数据
    private val _videoList = MutableLiveData<MutableList<VideoItem>>()
    val videoList: LiveData<MutableList<VideoItem>> = _videoList
    
    // 数据是否已经加载
    private val _isDataLoaded = MutableLiveData(false)
    val isDataLoaded: LiveData<Boolean> = _isDataLoaded
    
    /**
     * 设置视频列表数据
     */
    fun setVideoList(list: List<VideoItem>) {
        _videoList.value = list.toMutableList()
        _isDataLoaded.value = true
    }
    
    /**
     * 添加更多视频数据
     */
    fun addVideoList(list: List<VideoItem>) {
        val currentList = _videoList.value ?: mutableListOf()
        currentList.addAll(list)
        _videoList.value = currentList
    }
    
    /**
     * 清空视频列表数据
     */
    fun clearVideoList() {
        _videoList.value = mutableListOf()
        _isDataLoaded.value = false
    }
    
    /**
     * 获取当前视频列表大小
     */
    fun getCurrentListSize(): Int {
        return _videoList.value?.size ?: 0
    }
}