package com.ixigua.common.danmaku.render.engine.render.layer.line

import android.graphics.Canvas
import com.ixigua.common.danmaku.render.engine.data.DanmakuData
import com.ixigua.common.danmaku.render.engine.render.draw.DrawItem

/**
 * Created by dss886 on 2019/9/22.
 */
interface IRenderLine {

    fun onLayoutChanged(width: Float, height: Float, x: Float, y: Float)

    fun addItem(playTime:Long, item: DrawItem<DanmakuData>): Boolean

    fun typesetting(playTime:Long, isPlaying: Boolean, configChanged: Boolean = false): Int

    fun getPreDrawItems(): List<DrawItem<DanmakuData>>

    fun drawBounds(canvas: Canvas)

    fun clearRender()

}