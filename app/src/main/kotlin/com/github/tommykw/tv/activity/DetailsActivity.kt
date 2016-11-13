package com.github.tommykw.tv.activity

import android.app.Activity
import android.os.Bundle

import com.github.tommykw.tv.R

class DetailsActivity : BaseActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
    }

    companion object {
        val SHARED_ELEMENT_NAME = "hero"
        val MOVIE = "Movie"
    }

}
