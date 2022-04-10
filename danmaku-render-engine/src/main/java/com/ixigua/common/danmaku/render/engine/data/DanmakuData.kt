package com.ixigua.common.danmaku.render.engine.data

import com.ixigua.common.danmaku.render.engine.utils.DRAW_ORDER_DEFAULT
import com.ixigua.common.danmaku.render.engine.utils.DRAW_TYPE_UNDEFINE
import com.ixigua.common.danmaku.render.engine.utils.LAYER_TYPE_UNDEFINE

/**
 * Created by dss886 on 2019-05-19.
 * Fields in [DanmakuData] should be the property of danmaku, not related to the drawing.
 */
abstract class DanmakuData {

    open var showAtTime: Long = 0
    open var layerType: Int = LAYER_TYPE_UNDEFINE
    open var drawType: Int = DRAW_TYPE_UNDEFINE
    /**
     * The order danmaku will be drawn.
     * The larger the value, the later the danmaku is drawn.
     * If the drawOrder is the same, item with smaller showAtTime will be drawn first.
     */
    open var drawOrder: Int = DRAW_ORDER_DEFAULT

}