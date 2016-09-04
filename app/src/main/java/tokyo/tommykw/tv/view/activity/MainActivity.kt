package tokyo.tommykw.tv.view.activity

import android.app.Activity
import android.os.Bundle
import android.support.v4.view.ViewPager
import tokyo.tommykw.tv.R

class MainActivity : Activity() {
    private lateinit var pager: ViewPager
    private

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
