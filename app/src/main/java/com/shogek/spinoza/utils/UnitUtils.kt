package com.shogek.spinoza.utils

import android.content.res.Resources
import android.util.TypedValue

object UnitUtils {
    /** Converts dip (dp) into its equivalent in px. */
    fun asPixels(dp: Float, resources: Resources): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }
}