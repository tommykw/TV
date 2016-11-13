package com.github.tommykw.tv.util

import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import android.widget.Toast

class Utils {
    companion object {
        @JvmStatic
        fun getDisplaySize(context: Context): Point {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val size = Point()
            val display = wm.defaultDisplay.getSize(size)
            return size
        }

        @JvmStatic
        fun showToast(context: Context, msg: String) =
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

        @JvmStatic
        fun showToast(context: Context, resourceId: Int) =
                Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show()

        @JvmStatic
        fun convertDpToPixel(ctx: Context, dp: Int) =
                Math.round(dp.toFloat() * ctx.resources.displayMetrics.density)

        @JvmStatic
        fun formatMillis(millis: Int): String {
            var millis = millis
            var result = ""
            val hr = millis / 3600000
            millis %= 3600000
            val min = millis / 60000
            millis %= 60000
            val sec = millis / 1000
            if (hr > 0) {
                result += "${hr}:"
            }
            if (min >= 0) {
                if (min > 9) {
                    result += "${min}:"
                } else {
                    result += "0${min}:"
                }
            }
            if (sec > 9) {
                result += sec
            } else {
                result += "0${sec}"
            }
            return result
        }
    }
}