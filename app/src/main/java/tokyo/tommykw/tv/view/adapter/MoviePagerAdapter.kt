package tokyo.tommykw.tv.view.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import tokyo.tommykw.tv.view.fragment.VideoItemFragment

/**
 * Created by tommy on 2016/09/04.
 */
class MoviePagerAdapter(val fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment? {
        throw UnsupportedOperationException()
    }

    override fun getCount() = NUM_OF_ITEMS

    companion object {
        const val NUM_OF_ITEMS = 5
    }

}