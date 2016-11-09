package com.github.tommykw.tv.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar

class SpinnerFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle): View? {

        val progressBar = ProgressBar(container!!.context)
        if (container is FrameLayout) {
            val layoutParams = FrameLayout.LayoutParams(SPINNER_WIDTH, SPINNER_HEIGHT, Gravity.CENTER)
            progressBar.layoutParams = layoutParams
        }
        return progressBar
    }

    companion object {
        private const val SPINNER_WIDTH = 100
        private const val SPINNER_HEIGHT = 100
    }
}
