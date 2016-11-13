package com.github.tommykw.tv.fragment

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v17.leanback.app.BackgroundManager
import android.support.v17.leanback.app.DetailsFragment
import android.support.v17.leanback.widget.Action
import android.support.v17.leanback.widget.ArrayObjectAdapter
import android.support.v17.leanback.widget.ClassPresenterSelector
import android.support.v17.leanback.widget.DetailsOverviewRow
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter
import android.support.v17.leanback.widget.HeaderItem
import android.support.v17.leanback.widget.ImageCardView
import android.support.v17.leanback.widget.ListRow
import android.support.v17.leanback.widget.ListRowPresenter
import android.support.v17.leanback.widget.OnActionClickedListener
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.Presenter
import android.support.v17.leanback.widget.Row
import android.support.v17.leanback.widget.RowPresenter
import android.support.v4.app.ActivityOptionsCompat
import android.util.DisplayMetrics
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.github.tommykw.tv.R
import com.github.tommykw.tv.activity.DetailsActivity
import com.github.tommykw.tv.activity.MainActivity
import com.github.tommykw.tv.activity.PlaybackOverlayActivity
import com.github.tommykw.tv.model.Movie
import com.github.tommykw.tv.model.MovieList
import com.github.tommykw.tv.presenter.CardPresenter
import com.github.tommykw.tv.presenter.DetailsDescriptionPresenter
import com.github.tommykw.tv.util.Utils

import java.util.Collections

class VideoDetailsFragment : DetailsFragment() {
    private var selectedMovie: Movie? = null
    private var adapter: ArrayObjectAdapter? = null
    private var presenterSelector: ClassPresenterSelector? = null
    private var backgroundManager: BackgroundManager? = null
    private var defaultBackground: Drawable? = null
    private var metrics: DisplayMetrics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prepareBackgroundManager()

        selectedMovie = activity.intent.getSerializableExtra(DetailsActivity.MOVIE) as Movie
        if (selectedMovie != null) {
            setupAdapter()
            setupDetailsOverviewRow()
            setupDetailsOverviewRowPresenter()
            setupMovieListRow()
            setupMovieListRowPresenter()
            updateBackground(selectedMovie!!.backgroundImageUrl.toString())
            onItemViewClickedListener = ItemViewClickedListener()
        } else {
            startActivity(MainActivity.makeIntent(activity))
        }
    }

    override fun onStop() {
        super.onStop()
    }

    private fun prepareBackgroundManager() {
        backgroundManager = BackgroundManager.getInstance(activity)
        backgroundManager!!.attach(activity.window)
        defaultBackground = resources.getDrawable(R.drawable.default_background)
        metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
    }

    protected fun updateBackground(uri: String) {
        Glide.with(activity).load(uri).centerCrop().error(defaultBackground).into(object : SimpleTarget<GlideDrawable>(metrics!!.widthPixels, metrics!!.heightPixels) {
            override fun onResourceReady(resource: GlideDrawable,
                                         glideAnimation: GlideAnimation<in GlideDrawable>) {
                backgroundManager!!.drawable = resource
            }
        })
    }

    private fun setupAdapter() {
        presenterSelector = ClassPresenterSelector()
        adapter = ArrayObjectAdapter(presenterSelector!!)
        adapter = adapter!!
    }

    private fun setupDetailsOverviewRow() {
        val row = DetailsOverviewRow(selectedMovie)
        row.imageDrawable = resources.getDrawable(R.drawable.default_background)
        val width = Utils.convertDpToPixel(activity.applicationContext, DETAIL_THUMB_WIDTH)
        val height = Utils.convertDpToPixel(activity.applicationContext, DETAIL_THUMB_HEIGHT)
        Glide.with(activity).load(selectedMovie!!.cardImageUrl).centerCrop().error(R.drawable.default_background).into(object : SimpleTarget<GlideDrawable>(width, height) {
            override fun onResourceReady(resource: GlideDrawable,
                                         glideAnimation: GlideAnimation<in GlideDrawable>) {
                row.imageDrawable = resource
                adapter!!.notifyArrayItemRangeChanged(0, adapter!!.size())
            }
        })

        row.addAction(Action(ACTION_WATCH_TRAILER.toLong(), resources.getString(
                R.string.watch_trailer_1), resources.getString(R.string.watch_trailer_2)))
        row.addAction(Action(ACTION_RENT.toLong(), resources.getString(R.string.rent_1),
                resources.getString(R.string.rent_2)))
        row.addAction(Action(ACTION_BUY.toLong(), resources.getString(R.string.buy_1),
                resources.getString(R.string.buy_2)))

        adapter!!.add(row)
    }

    private fun setupDetailsOverviewRowPresenter() {
        val detailsPresenter = DetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        detailsPresenter.backgroundColor = resources.getColor(R.color.selected_background)
        detailsPresenter.isStyleLarge = true

        detailsPresenter.setSharedElementEnterTransition(activity,
                DetailsActivity.SHARED_ELEMENT_NAME)

        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->
            if (action.id == ACTION_WATCH_TRAILER.toLong()) {
                val intent = Intent(activity, PlaybackOverlayActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, selectedMovie)
                startActivity(intent)
            } else {
                Toast.makeText(activity, action.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        presenterSelector!!.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    private fun setupMovieListRow() {
        val subcategories = arrayOf(getString(R.string.related_movies))
        val list = MovieList.list

        Collections.shuffle(list)
        val listRowAdapter = ArrayObjectAdapter(CardPresenter())
        for (j in 0..NUM_COLS - 1) {
            listRowAdapter.add(list[j % 5])
        }

        val header = HeaderItem(0, subcategories[0])
        adapter!!.add(ListRow(header, listRowAdapter))
    }

    private fun setupMovieListRowPresenter() {
        presenterSelector!!.addClassPresenter(ListRow::class.java, ListRowPresenter())
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(itemViewHolder: Presenter.ViewHolder, item: Any,
                                   rowViewHolder: RowPresenter.ViewHolder, row: Row) {

            if (item is Movie) {
                val intent = Intent(activity, DetailsActivity::class.java)
                intent.putExtra(resources.getString(R.string.movie), selectedMovie)
                intent.putExtra(resources.getString(R.string.should_start), true)
                startActivity(intent)


                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        (itemViewHolder.view as ImageCardView).mainImageView,
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle()
                activity.startActivity(intent, bundle)
            }
        }
    }

    companion object {
        private const val ACTION_WATCH_TRAILER = 1
        private const val ACTION_RENT = 2
        private const val ACTION_BUY = 3
        private const val DETAIL_THUMB_WIDTH = 274
        private const val DETAIL_THUMB_HEIGHT = 274
        private const val NUM_COLS = 10
    }
}
