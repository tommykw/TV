package com.github.tommykw.tv.fragment

import android.os.Bundle
import android.view.View
import android.support.v17.leanback.app.ErrorFragment
import android.support.v4.content.res.ResourcesCompat

import com.github.tommykw.tv.R

class ErrorFragment : ErrorFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = resources.getString(R.string.app_name)
    }

    fun setErrorContent() {
        imageDrawable = ResourcesCompat.getDrawable(resources, R.drawable.lb_ic_sad_cloud, null)
        message = resources.getString(R.string.error_fragment_message)
        setDefaultBackground(TRANSLUCENT)
        buttonText = resources.getString(R.string.dismiss_error)
        buttonClickListener = View.OnClickListener {
            fragmentManager.beginTransaction().remove(this@ErrorFragment).commit()
        }
    }

    companion object {
        private val TRANSLUCENT = true
    }
}
