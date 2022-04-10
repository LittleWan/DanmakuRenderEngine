package com.ixigua.common.danmaku.render.engine.render.layer.top

import android.graphics.Canvas
import android.view.MotionEvent
import com.ixigua.common.danmaku.render.engine.control.ConfigChangeListener
import com.ixigua.common.danmaku.render.engine.control.DanmakuConfig
import com.ixigua.common.danmaku.render.engine.control.DanmakuController
import com.ixigua.common.danmaku.render.engine.control.Events
import com.ixigua.common.danmaku.render.engine.data.DanmakuData
import com.ixigua.common.danmaku.render.engine.render.IRenderLayer
import com.ixigua.common.danmaku.render.engine.render.cache.IDrawCachePool
import com.ixigua.common.danmaku.render.engine.render.cache.LayerBuffer
import com.ixigua.common.danmaku.render.engine.render.draw.DrawItem
import com.ixigua.common.danmaku.render.engine.touch.ITouchDelegate
import com.ixigua.common.danmaku.render.engine.touch.ITouchTarget
import com.ixigua.common.danmaku.render.engine.utils.EVENT_DANMAKU_DISMISS
import com.ixigua.common.danmaku.render.engine.utils.EVENT_DANMAKU_SHOW
import com.ixigua.common.danmaku.render.engine.utils.LAYER_TYPE_TOP_CENTER
import com.ixigua.common.danmaku.render.engine.utils.LAYER_Z_INDEX_TOP_CENTER
import java.util.*

/**
 * Created by dss886 on 2019/9/23.
 */
class TopCenterLayer : IRenderLayer, ITouchDelegate, ConfigChangeListener {

    private lateinit var mController: DanmakuController
    private lateinit var mCachePool: IDrawCachePool
    private lateinit var mBuffer: LayerBuffer
    private lateinit var mConfig: DanmakuConfig
    private val mLines = LinkedList<TopCenterLine>()
    private val mPreDrawItems = LinkedList<DrawItem<DanmakuData>>()
    private var mTotalDanmakuCountInLayer = 0
    private var mWidth = 0
    private var mHeight = 0

    override fun init(controller: DanmakuController, cachePool: IDrawCachePool) {
        mController = controller
        mCachePool = cachePool
        mConfig = mController.config
        mBuffer = LayerBuffer(mConfig, mCachePool, mConfig.top.bufferSize, mConfig.top.bufferMaxTime)
        mConfig.addListener(this)
    }

    override fun getLayerType(): Int {
        return LAYER_TYPE_TOP_CENTER
    }

    override fun getLayerZIndex(): Int {
        return LAYER_Z_INDEX_TOP_CENTER
    }

    override fun onLayoutSizeChanged(width: Int, height: Int) {
        mWidth = width
        mHeight = height
        configLines()
    }

    override fun addItems(playTime: Long, list: List<DrawItem<DanmakuData>>) {
        mBuffer.addItems(list)
        mBuffer.trimBuffer(playTime)
    }

    override fun releaseItem(item: DrawItem<DanmakuData>) {
        mController.notifyEvent(Events.obtainEvent(EVENT_DANMAKU_DISMISS, item.data))
        mCachePool.release(item)
    }

    override fun typesetting(playTime: Long, isPlaying: Boolean, configChanged: Boolean): Int {
        mBuffer.forEach {
            distributeItemToLines(playTime, it)
        }
        mTotalDanmakuCountInLayer = 0
        mLines.forEach { line ->
            mTotalDanmakuCountInLayer += line.typesetting(playTime, isPlaying, configChanged)
        }
        if (configChanged) {
            mBuffer.measureItems()
        }
        return mTotalDanmakuCountInLayer
    }

    override fun drawBounds(canvas: Canvas) {
        mLines.forEach { line ->
            line.drawBounds(canvas)
        }
    }

    override fun getPreDrawItems(): List<DrawItem<DanmakuData>> {
        mPreDrawItems.clear()
        mLines.forEach { line ->
            mPreDrawItems.addAll(line.getPreDrawItems())
        }
        return mPreDrawItems
    }

    override fun clear() {
        mLines.forEach { line ->
            line.clearRender()
        }
        mBuffer.clear()
    }

    override fun findTouchTarget(event: MotionEvent): ITouchTarget? {
        mLines.forEach { line ->
            if (event.y > line.y + line.height) {
                return@forEach
            }
            if (event.y >= line.y && line.onTouchEvent(event)) {
                return line
            }
            return null
        }
        return null
    }

    override fun onConfigChanged(type: Int) {
        when (type) {
            DanmakuConfig.TYPE_TOP_CENTER_LINE_HEIGHT,
            DanmakuConfig.TYPE_TOP_CENTER_LINE_COUNT,
            DanmakuConfig.TYPE_TOP_CENTER_LINE_MARGIN,
            DanmakuConfig.TYPE_TOP_CENTER_MARGIN_TOP -> configLines()
            DanmakuConfig.TYPE_TOP_CENTER_BUFFER_MAX_TIME,
            DanmakuConfig.TYPE_TOP_CENTER_BUFFER_SIZE -> {
                mBuffer.onBufferChanged(mConfig.top.bufferSize, mConfig.top.bufferMaxTime)
            }
        }
    }

    /**
     * Try add item to lines.
     * Return true if find a line to add, return false otherwise.
     */
    private fun distributeItemToLines(playTime: Long, item: DrawItem<DanmakuData>): Boolean {
        mLines.maxByOrNull { it.getCurrentItemShowDuration() }?.let { line ->
            if (line.addItem(playTime, item)) {
                mController.notifyEvent(Events.obtainEvent(EVENT_DANMAKU_SHOW, item.data))
                return true
            }
        }
        return false
    }

    private fun configLines() {
        val lineCount = mConfig.top.lineCount
        val lineHeight = mConfig.top.lineHeight
        val lineSpace = mConfig.top.lineMargin
        val marginTop = mConfig.top.marginTop
        if (lineCount > mLines.size) {
            for (i in 1..(lineCount - mLines.size)) {
                mLines.add(TopCenterLine(mController, this).apply {
                    mController.registerCmdMonitor(this)
                })
            }
        } else if (lineCount < mLines.size) {
            for (i in 1..(mLines.size - lineCount)) {
                mLines.removeAt(mLines.size - 1).let {
                    mController.unRegisterCmdMonitor(it)
                }
            }
        }
        mLines.forEachIndexed { index, line ->
            line.onLayoutChanged(mWidth.toFloat(), lineHeight, 0F, marginTop + index * (lineSpace + lineHeight))
        }
    }

}