package com.github.tommykw.tv.fragment

import java.net.URI
import java.util.Collections
import java.util.Timer
import java.util.TimerTask

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.BackgroundManager
import android.support.v17.leanback.app.BrowseFragment
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ImageCardView
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.OnItemViewSelectedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v4.app.ActivityOptionsCompat
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.github.tommykw.tv.Movie
import com.github.tommykw.tv.MovieList
import com.github.tommykw.tv.R
import com.github.tommykw.tv.activity.BrowseErrorActivity
import com.github.tommykw.tv.activity.DetailsActivity
import com.github.tommykw.tv.presenter.CardPresenter

class MainFragment : BrowseFragment() {
    private val handler = Handler()
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private var defaultBackground: Drawable? = null
    private var metrics: DisplayMetrics? = null
    private var backgroundTimer: Timer? = null
    private var backgroundURI: URI? = null
    private val backgroundManager by lazy { BackgroundManager.getInstance(activity) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        prepareBackgroundManager()
        setupUIElements()
        loadRows()
        setupEventListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != backgroundTimer) {
            backgroundTimer!!.cancel()
        }
    }

    private fun loadRows() {
        val list = MovieList.setupMovies()
        val cardPresenter = CardPresenter()

        var i: Int
        i = 0
        while (i < NUM_ROWS) {
            if (i != 0) {
                Collections.shuffle(list)
            }
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (j in 0..NUM_COLS - 1) {
                listRowAdapter.add(list[j % 5])
            }
            val header = HeaderItem(i.toLong(), MovieList.MOVIE_CATEGORY[i])
            rowsAdapter.add(ListRow(header, listRowAdapter))
            i++
        }

        val gridHeader = HeaderItem(i.toLong(), "PREFERENCES")

        val mGridPresenter = GridItemPresenter()
        val gridRowAdapter = ArrayObjectAdapter(mGridPresenter)
        gridRowAdapter.add(resources.getString(R.string.grid_view))
        gridRowAdapter.add(getString(R.string.error_fragment))
        gridRowAdapter.add(resources.getString(R.string.personal_settings))
        rowsAdapter.add(ListRow(gridHeader, gridRowAdapter))

        adapter = rowsAdapter

    }

    private fun prepareBackgroundManager() {
        backgroundManager.attach(activity.window)
        defaultBackground = resources.getDrawable(R.drawable.default_background)
        metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        headersState = BrowseFragment.HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = resources.getColor(R.color.fastlane_background)
        searchAffordanceColor = resources.getColor(R.color.search_opaque)
    }

    private fun setupEventListeners() {
        setOnSearchClickedListener {
            Toast.makeText(activity, "Implement your own in-app search", Toast.LENGTH_LONG)
                .show()
        }

        onItemViewClickedListener = ItemViewClickedListener()
        onItemViewSelectedListener = ItemViewSelectedListener()
    }

    protected fun updateBackground(uri: String) {
        val width = metrics!!.widthPixels
        val height = metrics!!.heightPixels
        Glide.with(activity)
            .load(uri)
            .centerCrop()
            .error(defaultBackground)
            .into(object : SimpleTarget<GlideDrawable>(width, height) {
                override fun onResourceReady(resource: GlideDrawable,
                                             glideAnimation: GlideAnimation<in GlideDrawable>) {
                    backgroundManager!!.drawable = resource
                }
            })
        backgroundTimer!!.cancel()
    }

    private fun startBackgroundTimer() {
        if (null != backgroundTimer) {
            backgroundTimer!!.cancel()
        }
        backgroundTimer = Timer()
        backgroundTimer!!.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(itemViewHolder: Presenter.ViewHolder, item: Any,
                                   rowViewHolder: RowPresenter.ViewHolder, row: Row) {

            if (item is Movie) {
                val intent = Intent(activity, DetailsActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, item)

                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    (itemViewHolder.view as ImageCardView).mainImageView,
                    DetailsActivity.SHARED_ELEMENT_NAME).toBundle()
                activity.startActivity(intent, bundle)
            } else if (item is String) {
                if (item.indexOf(getString(R.string.error_fragment)) >= 0) {
                    val intent = Intent(activity, BrowseErrorActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(activity, item, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(itemViewHolder: Presenter.ViewHolder, item: Any,
                                    rowViewHolder: RowPresenter.ViewHolder, row: Row) {
            if (item is Movie) {
                backgroundURI = item.backgroundImageURI
                startBackgroundTimer()
            }

        }
    }

    private inner class UpdateBackgroundTask : TimerTask() {

        override fun run() {
            handler.post {
                if (backgroundURI != null) {
                    updateBackground(backgroundURI!!.toString())
                }
            }

        }
    }

    private inner class GridItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
            val view = TextView(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.setBackgroundColor(resources.getColor(R.color.default_background))
            view.setTextColor(Color.WHITE)
            view.gravity = Gravity.CENTER
            return Presenter.ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
            (viewHolder.view as TextView).text = item as String
        }

        override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        }
    }

    companion object {
        private const val BACKGROUND_UPDATE_DELAY = 300
        private const val GRID_ITEM_WIDTH = 200
        private const val GRID_ITEM_HEIGHT = 200
        private const val NUM_ROWS = 6
        private const val NUM_COLS = 15
    }
}
