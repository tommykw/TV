package com.github.tommykw.tv.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.tommykw.tv.R

class MainActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        @JvmStatic
        fun makeIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
