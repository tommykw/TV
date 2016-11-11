package com.github.tommykw.tv.model

import java.io.Serializable
import java.net.URI
import java.net.URISyntaxException

class Movie : Serializable {
    var id: Long = 0
    var title: String? = null
    var description: String? = null
    var backgroundImageUrl: String? = null
    var cardImageUrl: String? = null
    var videoUrl: String? = null
    var studio: String? = null
    var category: String? = null

    val backgroundImageUri: URI?
        get() {
            try {
                return URI(backgroundImageUrl)
            } catch (e: URISyntaxException) {
                return null
            }
        }

    val cardImageUri: URI?
        get() {
            try {
                return URI(cardImageUrl)
            } catch (e: URISyntaxException) {
                return null
            }
        }

    companion object {
        @JvmStatic
        var count: Long = 0
            private set

        @JvmStatic
        fun incrementCount() = count++
    }
}
