/*
 * Copyright (C) 2022 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.danmaku.render.engine.render.draw.bitmap

import android.graphics.*
import com.bytedance.danmaku.render.engine.control.DanmakuConfig
import com.bytedance.danmaku.render.engine.render.draw.DrawItem
import com.bytedance.danmaku.render.engine.utils.DRAW_TYPE_BITMAP

/**
 * Created by dss886 on 2019-05-20.
 */
open class BitmapDrawItem: DrawItem<BitmapData>() {

    private val mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private val mRectF = RectF()

    override fun getDrawType(): Int {
        return DRAW_TYPE_BITMAP
    }

    override fun onBindData(data: BitmapData) {
        mBitmapPaint.flags = Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG
    }

    override fun onMeasure(config: DanmakuConfig) {
        val width = data?.width ?: 0F
        val height = data?.height ?: 0F
        if (width > 0 && height > 0) {
            this.width = width
            this.height = height
        } else {
            this.width = data?.bitmap?.width?.toFloat() ?: 0F
            this.height = data?.bitmap?.height?.toFloat() ?: 0F
        }
    }

    override fun onDraw(canvas: Canvas, config: DanmakuConfig) {
        data?.bitmap?.let { bitmap ->
            mRectF.set(x, y, x + width, y + height)
            val tint = data?.tintColor
            mBitmapPaint.colorFilter = if (tint == null) null else PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, null, mRectF, mBitmapPaint)
        }
    }

    override fun recycle() {
        super.recycle()
        mBitmapPaint.reset()
        mRectF.set(0F, 0F, 0F, 0F)
    }
}