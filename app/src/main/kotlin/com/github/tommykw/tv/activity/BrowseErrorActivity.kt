package com.github.tommykw.tv.activity

import android.os.Bundle
import com.github.tommykw.tv.fragment.ErrorFragment
import com.github.tommykw.tv.R
import com.github.tommykw.tv.fragment.SpinnerFragment
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class BrowseErrorActivity : BaseActivity() {
    private lateinit var errorFragment: ErrorFragment
    private lateinit var spinnerFragment: SpinnerFragment

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        callError()
    }

    private fun callError() {
        errorFragment = ErrorFragment()
        spinnerFragment = SpinnerFragment()
        fragmentManager.beginTransaction().apply {
            add(R.id.main_browse_fragment, errorFragment)
            add(R.id.main_browse_fragment, spinnerFragment)
            commit()
        }

        Observable.interval(TIMER_DELAY.toLong(), TimeUnit.MILLISECONDS).subscribe({
            fragmentManager.beginTransaction().remove(spinnerFragment).commit()
            errorFragment.setErrorContent()
        }, { e -> })
    }

    companion object {
        private val TIMER_DELAY = 3000
    }
}
