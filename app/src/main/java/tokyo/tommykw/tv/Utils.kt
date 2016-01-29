/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package tokyo.tommykw.tv

import android.content.Context
import android.graphics.Point
import android.view.Display
import android.view.WindowManager
import android.widget.Toast

/**
 * A collection of utility methods, all static.
 */
object Utils {

    /**
     * Returns the screen/display size
     */
    fun getDisplaySize(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    /**
     * Shows a (long) toast
     */
    fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    /**
     * Shows a (long) toast.
     */
    fun showToast(context: Context, resourceId: Int) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show()
    }

    fun convertDpToPixel(ctx: Context, dp: Int): Int {
        val density = ctx.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    /**
     * Formats time in milliseconds to hh:mm:ss string format.
     */
    fun formatMillis(millis: Int): String {
        var millis = millis
        var result = ""
        val hr = millis / 3600000
        millis %= 3600000
        val min = millis / 60000
        millis %= 60000
        val sec = millis / 1000
        if (hr > 0) {
            result += hr + ":"
        }
        if (min >= 0) {
            if (min > 9) {
                result += min + ":"
            } else {
                result += "0$min:"
            }
        }
        if (sec > 9) {
            result += sec
        } else {
            result += "0" + sec
        }
        return result
    }
}/*
     * Making sure public utility methods remain static
     */
