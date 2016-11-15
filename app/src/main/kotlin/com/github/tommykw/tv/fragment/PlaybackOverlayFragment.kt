package com.github.tommykw.tv.fragment

import android.app.Activity
import android.content.Context

import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v17.leanback.app.PlaybackOverlayFragment
import android.support.v17.leanback.widget.*
import android.support.v17.leanback.widget.OnItemViewClickedListener
import android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction
import android.support.v17.leanback.widget.PlaybackControlsRow.RepeatAction
import android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction
import android.support.v17.leanback.widget.PlaybackControlsRow.ShuffleAction
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipNextAction
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipPreviousAction
import android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsDownAction
import android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsUpAction
import android.util.Log
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.github.tommykw.tv.R
import com.github.tommykw.tv.activity.DetailsActivity
import com.github.tommykw.tv.model.Movie
import com.github.tommykw.tv.model.MovieList
import com.github.tommykw.tv.presenter.CardPresenter

import java.util.ArrayList
import java.util.HashMap

class PlaybackOverlayFragment : PlaybackOverlayFragment() {
    private var mRowsAdapter: ArrayObjectAdapter? = null
    private var mPrimaryActionsAdapter: ArrayObjectAdapter? = null
    private var mSecondaryActionsAdapter: ArrayObjectAdapter? = null
    private var mPlayPauseAction: PlayPauseAction? = null
    private var mRepeatAction: RepeatAction? = null
    private var mThumbsUpAction: ThumbsUpAction? = null
    private var mThumbsDownAction: ThumbsDownAction? = null
    private var mShuffleAction: ShuffleAction? = null
    private var mFastForwardAction: FastForwardAction? = null
    private var mRewindAction: RewindAction? = null
    private var mSkipNextAction: SkipNextAction? = null
    private var mSkipPreviousAction: SkipPreviousAction? = null
    private var mPlaybackControlsRow: PlaybackControlsRow? = null
    private var mItems = ArrayList<Movie>()
    private var mCurrentItem: Int = 0
    private var mHandler: Handler? = null
    private var mRunnable: Runnable? = null
    private var mSelectedMovie: Movie? = null

    private var mCallback: OnPlayPauseClickedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mItems = ArrayList<Movie>()
        mSelectedMovie = activity.intent.getSerializableExtra(DetailsActivity.MOVIE) as Movie

        val movies = MovieList.list

        for (j in movies.indices) {
            mItems.add(movies[j])
            if (mSelectedMovie!!.title!!.contentEquals(movies[j].title!!)) {
                mCurrentItem = j
            }
        }

        mHandler = Handler()

        backgroundType = BACKGROUND_TYPE
        isFadingEnabled = false

        setupRows()

        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row -> Log.i(TAG, "onItemSelected: $item row $row") }
        onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            Log.i(TAG, "onItemClicked: $item row $row")
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnPlayPauseClickedListener) {
            mCallback = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnPlayPauseClickedListener")
        }
    }

    private fun setupRows() {

        val ps = ClassPresenterSelector()

        val playbackControlsRowPresenter: PlaybackControlsRowPresenter
        if (SHOW_DETAIL) {
            playbackControlsRowPresenter = PlaybackControlsRowPresenter(
                    DescriptionPresenter())
        } else {
            playbackControlsRowPresenter = PlaybackControlsRowPresenter()
        }
        playbackControlsRowPresenter.onActionClickedListener = OnActionClickedListener { action ->
            if (action.id == mPlayPauseAction!!.id) {
                togglePlayback(mPlayPauseAction!!.index == PlayPauseAction.PLAY)
            } else if (action.id == mSkipNextAction!!.id) {
                next()
            } else if (action.id == mSkipPreviousAction!!.id) {
                prev()
            } else if (action.id == mFastForwardAction!!.id) {
                Toast.makeText(activity, "TODO: Fast Forward", Toast.LENGTH_SHORT).show()
            } else if (action.id == mRewindAction!!.id) {
                Toast.makeText(activity, "TODO: Rewind", Toast.LENGTH_SHORT).show()
            }
            if (action is PlaybackControlsRow.MultiAction) {
                action.nextIndex()
                notifyChanged(action)
            }
        }
        playbackControlsRowPresenter.setSecondaryActionsHidden(HIDE_MORE_ACTIONS)

        ps.addClassPresenter(PlaybackControlsRow::class.java, playbackControlsRowPresenter)
        ps.addClassPresenter(ListRow::class.java, ListRowPresenter())
        mRowsAdapter = ArrayObjectAdapter(ps)

        addPlaybackControlsRow()
        addOtherRows()

        adapter = mRowsAdapter!!
    }

    fun togglePlayback(playPause: Boolean) {
        if (playPause) {
            startProgressAutomation()
            isFadingEnabled = true
            mCallback!!.onFragmentPlayPause(mItems[mCurrentItem],
                    mPlaybackControlsRow!!.currentTime, true)
            mPlayPauseAction!!.icon = mPlayPauseAction!!.getDrawable(PlayPauseAction.PAUSE)
        } else {
            stopProgressAutomation()
            isFadingEnabled = false
            mCallback!!.onFragmentPlayPause(mItems[mCurrentItem],
                    mPlaybackControlsRow!!.currentTime, false)
            mPlayPauseAction!!.icon = mPlayPauseAction!!.getDrawable(PlayPauseAction.PLAY)
        }
        notifyChanged(mPlayPauseAction!!)
    }

    private val duration: Int
        get() {
            val movie = mItems[mCurrentItem]
            val mmr = MediaMetadataRetriever()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mmr.setDataSource(movie.videoUrl, HashMap<String, String>())
            } else {
                mmr.setDataSource(movie.videoUrl)
            }
            val time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = java.lang.Long.parseLong(time)
            return duration.toInt()
        }

    private fun addPlaybackControlsRow() {
        if (SHOW_DETAIL) {
            mPlaybackControlsRow = PlaybackControlsRow(mSelectedMovie)
        } else {
            mPlaybackControlsRow = PlaybackControlsRow()
        }
        mRowsAdapter!!.add(mPlaybackControlsRow)

        updatePlaybackRow(mCurrentItem)

        val presenterSelector = ControlButtonPresenterSelector()
        mPrimaryActionsAdapter = ArrayObjectAdapter(presenterSelector)
        mSecondaryActionsAdapter = ArrayObjectAdapter(presenterSelector)
        mPlaybackControlsRow!!.primaryActionsAdapter = mPrimaryActionsAdapter
        mPlaybackControlsRow!!.secondaryActionsAdapter = mSecondaryActionsAdapter

        mPlayPauseAction = PlayPauseAction(activity)
        mRepeatAction = RepeatAction(activity)
        mThumbsUpAction = ThumbsUpAction(activity)
        mThumbsDownAction = ThumbsDownAction(activity)
        mShuffleAction = ShuffleAction(activity)
        mSkipNextAction = PlaybackControlsRow.SkipNextAction(activity)
        mSkipPreviousAction = PlaybackControlsRow.SkipPreviousAction(activity)
        mFastForwardAction = PlaybackControlsRow.FastForwardAction(activity)
        mRewindAction = PlaybackControlsRow.RewindAction(activity)

        if (PRIMARY_CONTROLS > 5) {
            mPrimaryActionsAdapter!!.add(mThumbsUpAction)
        } else {
            mSecondaryActionsAdapter!!.add(mThumbsUpAction)
        }
        mPrimaryActionsAdapter!!.add(mSkipPreviousAction)
        if (PRIMARY_CONTROLS > 3) {
            mPrimaryActionsAdapter!!.add(PlaybackControlsRow.RewindAction(activity))
        }
        mPrimaryActionsAdapter!!.add(mPlayPauseAction)
        if (PRIMARY_CONTROLS > 3) {
            mPrimaryActionsAdapter!!.add(PlaybackControlsRow.FastForwardAction(activity))
        }
        mPrimaryActionsAdapter!!.add(mSkipNextAction)

        mSecondaryActionsAdapter!!.add(mRepeatAction)
        mSecondaryActionsAdapter!!.add(mShuffleAction)
        if (PRIMARY_CONTROLS > 5) {
            mPrimaryActionsAdapter!!.add(mThumbsDownAction)
        } else {
            mSecondaryActionsAdapter!!.add(mThumbsDownAction)
        }
        mSecondaryActionsAdapter!!.add(PlaybackControlsRow.HighQualityAction(activity))
        mSecondaryActionsAdapter!!.add(PlaybackControlsRow.ClosedCaptioningAction(activity))
    }

    private fun notifyChanged(action: Action) {
        var adapter: ArrayObjectAdapter = mPrimaryActionsAdapter!!
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1)
            return
        }
        adapter = mSecondaryActionsAdapter!!
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1)
            return
        }
    }

    private fun updatePlaybackRow(index: Int) {
        if (mPlaybackControlsRow!!.item != null) {
            val item = mPlaybackControlsRow!!.item as Movie
            item.title = mItems[mCurrentItem].title
            item.studio = mItems[mCurrentItem].studio
        }
        if (SHOW_IMAGE) {
            updateVideoImage(mItems[mCurrentItem].cardImageUri!!.toString())
        }
        mRowsAdapter!!.notifyArrayItemRangeChanged(0, 1)
        mPlaybackControlsRow!!.totalTime = duration
        mPlaybackControlsRow!!.currentTime = 0
        mPlaybackControlsRow!!.bufferedProgress = 0
    }

    private fun addOtherRows() {
        val listRowAdapter = ArrayObjectAdapter(CardPresenter())
        for (movie in mItems) {
            listRowAdapter.add(movie)
        }
        val header = HeaderItem(0, getString(R.string.related_movies))
        mRowsAdapter!!.add(ListRow(header, listRowAdapter))

    }

    private val updatePeriod: Int
        get() {
            if (view == null || mPlaybackControlsRow!!.totalTime <= 0) {
                return DEFAULT_UPDATE_PERIOD
            }
            return Math.max(UPDATE_PERIOD, mPlaybackControlsRow!!.totalTime / view!!.width)
        }

    private fun startProgressAutomation() {
        mRunnable = object : Runnable {
            override fun run() {
                val updatePeriod = updatePeriod
                val currentTime = mPlaybackControlsRow!!.currentTime + updatePeriod
                val totalTime = mPlaybackControlsRow!!.totalTime
                mPlaybackControlsRow!!.currentTime = currentTime
                mPlaybackControlsRow!!.bufferedProgress = currentTime + SIMULATED_BUFFERED_TIME

                if (totalTime > 0 && totalTime <= currentTime) {
                    next()
                }
                mHandler!!.postDelayed(this, updatePeriod.toLong())
            }
        }
        mHandler!!.postDelayed(mRunnable, updatePeriod.toLong())
    }

    private operator fun next() {
        if (++mCurrentItem >= mItems.size) {
            mCurrentItem = 0
        }

        if (mPlayPauseAction!!.index == PlayPauseAction.PLAY) {
            mCallback!!.onFragmentPlayPause(mItems[mCurrentItem], 0, false)
        } else {
            mCallback!!.onFragmentPlayPause(mItems[mCurrentItem], 0, true)
        }
        updatePlaybackRow(mCurrentItem)
    }

    private fun prev() {
        if (--mCurrentItem < 0) {
            mCurrentItem = mItems.size - 1
        }
        if (mPlayPauseAction!!.index == PlayPauseAction.PLAY) {
            mCallback!!.onFragmentPlayPause(mItems[mCurrentItem], 0, false)
        } else {
            mCallback!!.onFragmentPlayPause(mItems[mCurrentItem], 0, true)
        }
        updatePlaybackRow(mCurrentItem)
    }

    private fun stopProgressAutomation() {
        if (mHandler != null && mRunnable != null) {
            mHandler!!.removeCallbacks(mRunnable)
        }
    }

    override fun onStop() {
        stopProgressAutomation()
        super.onStop()
    }

    protected fun updateVideoImage(uri: String) {
        Glide.with(activity).load(uri).centerCrop().into(object : SimpleTarget<GlideDrawable>(CARD_WIDTH, CARD_HEIGHT) {
            override fun onResourceReady(resource: GlideDrawable, glideAnimation: GlideAnimation<in GlideDrawable>) {
                mPlaybackControlsRow!!.imageDrawable = resource
                mRowsAdapter!!.notifyArrayItemRangeChanged(0, mRowsAdapter!!.size())
            }
        })
    }

    // Container Activity must implement this interface
    interface OnPlayPauseClickedListener {
        fun onFragmentPlayPause(movie: Movie, position: Int, playPause: Boolean?)
    }

    internal class DescriptionPresenter : AbstractDetailsDescriptionPresenter() {
        override fun onBindDescription(viewHolder: AbstractDetailsDescriptionPresenter.ViewHolder, item: Any) {
            viewHolder.title.text = (item as Movie).title
            viewHolder.subtitle.text = item.studio
        }
    }

    companion object {
        private val TAG = "PlaybackControlsFragmnt"

        private val SHOW_DETAIL = true
        private val HIDE_MORE_ACTIONS = false
        private val PRIMARY_CONTROLS = 5
        private val SHOW_IMAGE = PRIMARY_CONTROLS <= 5
        private val BACKGROUND_TYPE = PlaybackOverlayFragment.BG_LIGHT
        private val CARD_WIDTH = 200
        private val CARD_HEIGHT = 240
        private val DEFAULT_UPDATE_PERIOD = 1000
        private val UPDATE_PERIOD = 16
        private val SIMULATED_BUFFERED_TIME = 10000
    }
}
