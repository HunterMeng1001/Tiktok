package com.git.tiktok.android.data

import android.os.Parcel
import android.os.Parcelable

/**
 * 视频数据模型
 * 用于存储视频相关信息，包括ID、标题、封面URL、视频URL、点赞数、评论数、分享数、用户名、头像URL、是否关注、是否点赞
 * 后续优化建议：
 * 1. 考虑添加视频时长字段，用于展示视频播放时间
 * 2. 考虑添加视频播放进度字段，用于实现视频的 seekTo 功能
 * 3. 考虑添加视频播放状态字段，用于判断视频是否正在播放
 * @property id 视频ID
 * @property title 视频标题
 * @property coverUrl 封面图片URL
 * @property videoUrl 视频播放URL
 * @property likeCount 点赞数
 * @property commentCount 评论数
 * @property shareCount 分享数
 * @property username 用户名
 * @property avatarUrl 用户头像URL
 * @property isFollowed 是否已关注
 */
data class VideoItem(
    val id: String,
    val title: String,
    val coverUrl: String,
    val videoUrl: String,
    var likeCount: Int,
    val commentCount: Int,
    val shareCount: Int,
    val username: String,
    val avatarUrl: String,
    var isFollowed: Boolean = false,
    var isLiked: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(coverUrl)
        parcel.writeString(videoUrl)
        parcel.writeInt(likeCount)
        parcel.writeInt(commentCount)
        parcel.writeInt(shareCount)
        parcel.writeString(username)
        parcel.writeString(avatarUrl)
        parcel.writeByte(if (isFollowed) 1 else 0)
        parcel.writeByte(if (isLiked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoItem> {
        override fun createFromParcel(parcel: Parcel): VideoItem {
            return VideoItem(parcel)
        }

        override fun newArray(size: Int): Array<VideoItem?> {
            return arrayOfNulls(size)
        }
    }
}