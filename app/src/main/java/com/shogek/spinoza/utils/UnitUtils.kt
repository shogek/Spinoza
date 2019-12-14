package com.shogek.spinoza.utils

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager

object UnitUtils {
    private val cache: HashMap<Float, Float> = HashMap()

    /** Converts dip (dp) into its equivalent in px. */
    fun dpsAsPixels(dp: Float,
                 resources: Resources
    ): Float {
        if (this.cache.containsKey(dp)) {
            return this.cache[dp]!!
        }

        val pixels = TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

        this.cache[dp] = pixels
        return pixels
    }

//    fun asDps(windowManager: WindowManager,
//              pixels: Number
//    ) {
//        val metrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(metrics)
//        val logicalDensity: Float = metrics.density
//        val px = Math.ceil(dp * logicalDensity).toInt()
//    }
}