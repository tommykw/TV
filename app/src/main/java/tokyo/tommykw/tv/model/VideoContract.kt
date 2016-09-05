package tokyo.tommykw.tv.model

import android.net.Uri

/**
 * Created by tommy on 2016/09/05.
 */
class VideoContract {
    companion object {
        const val CONTENT_AUTHORITY = "com.android.example.leanback"
        val BASE_URI = Uri.parse("content://com.android.example.leanback")
    }
}