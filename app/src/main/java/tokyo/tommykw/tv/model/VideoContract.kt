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

    interface Columns {
        companion object {
            const val ID = "id"
            const val CATEGORY = "category"
            const val TITLE = "title"
            const val CONTENT_URL = "content_url"
            const val DESCRIPTION = "description"
            const val RATING = "rating"
            const val THUMB_URL = "thumb_url"
            const val TAGS = "tags"
            const val YEAR = "year"
        }
    }
}