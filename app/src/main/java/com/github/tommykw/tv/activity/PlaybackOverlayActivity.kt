package com.github.tommykw.tv.activity

import android.app.Activity
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.VideoView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.github.tommykw.tv.PlaybackOverlayFragment
import com.github.tommykw.tv.R
import com.github.tommykw.tv.model.Movie

class PlaybackOverlayActivity : Activity(), PlaybackOverlayFragment.OnPlayPauseClickedListener {

    private val mVideoView by lazy { findViewById(R.id.videoView) as VideoView }
    private var mPlaybackState = LeanbackPlaybackState.IDLE
    private var mSession = MediaSession(this, "LeanbackSampleApp")

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playback_controls)
        loadViews()
        setupCallbacks()
        mSession.apply {
            setCallback(MediaSessionCallback())
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
            isActive = true
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        mVideoView.suspend()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        val playbackOverlayFragment = fragmentManager.findFragmentById(R.id.playback_controls_fragment) as PlaybackOverlayFragment
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                playbackOverlayFragment.togglePlayback(false)
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                playbackOverlayFragment.togglePlayback(false)
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                    playbackOverlayFragment.togglePlayback(false)
                } else {
                    playbackOverlayFragment.togglePlayback(true)
                }
                return true
            }
            else -> return super.onKeyUp(keyCode, event)
        }
    }

    override fun onFragmentPlayPause(movie: Movie, position: Int, playPause: Boolean?) {
        mVideoView.setVideoPath(movie.videoUrl)

        if (position == 0 || mPlaybackState == LeanbackPlaybackState.IDLE) {
            setupCallbacks()
            mPlaybackState = LeanbackPlaybackState.IDLE
        }

        if (playPause!! && mPlaybackState != LeanbackPlaybackState.PLAYING) {
            mPlaybackState = LeanbackPlaybackState.PLAYING
            if (position > 0) {
                mVideoView.seekTo(position)
                mVideoView.start()
            }
        } else {
            mPlaybackState = LeanbackPlaybackState.PAUSED
            mVideoView.pause()
        }
        updatePlaybackState(position)
        updateMetadata(movie)
    }

    private fun updatePlaybackState(position: Int) {
        val stateBuilder = PlaybackState.Builder()
            .setActions(availableActions)
        var state = PlaybackState.STATE_PLAYING
        if (mPlaybackState == LeanbackPlaybackState.PAUSED) {
            state = PlaybackState.STATE_PAUSED
        }
        stateBuilder.setState(state, position.toLong(), 1.0f)
        mSession.setPlaybackState(stateBuilder.build())
    }

    private val availableActions: Long
        get() {
            var actions = PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackState.ACTION_PLAY_FROM_SEARCH

            if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                actions = actions or PlaybackState.ACTION_PAUSE
            }

            return actions
        }

    private fun updateMetadata(movie: Movie) {
        val metadataBuilder = MediaMetadata.Builder()

        val title = movie.title!!.replace("_", " -")

        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title)
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
            movie.description)
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,
            movie.cardImageUrl)

        // And at minimum the title and artist for legacy support
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, title)
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, movie.studio)

        Glide.with(this)
            .load(Uri.parse(movie.cardImageUrl))
            .asBitmap()
            .into(object : SimpleTarget<Bitmap>(500, 500) {
                override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                    metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, resource)
                    mSession.setMetadata(metadataBuilder.build())

                }
            })
    }

    private fun loadViews() {
        mVideoView.isFocusable = false
        mVideoView.isFocusableInTouchMode = false
    }

    private fun setupCallbacks() {
        mVideoView.setOnErrorListener { mp, what, extra ->
            var msg = ""
            if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                msg = getString(R.string.video_error_media_load_timeout)
            } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                msg = getString(R.string.video_error_server_inaccessible)
            } else {
                msg = getString(R.string.video_error_unknown_error)
            }
            mVideoView.stopPlayback()
            mPlaybackState = LeanbackPlaybackState.IDLE
            false
        }

        mVideoView.setOnPreparedListener {
            if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                mVideoView.start()
            }
        }

        mVideoView.setOnCompletionListener { mPlaybackState = LeanbackPlaybackState.IDLE }

    }

    public override fun onResume() {
        super.onResume()
        mSession.isActive = true
    }

    public override fun onPause() {
        super.onPause()
        if (mVideoView.isPlaying) {
            if (!requestVisibleBehind(true)) {
                stopPlayback()
            }
        } else {
            requestVisibleBehind(false)
        }
    }

    override fun onStop() {
        super.onStop()
        mSession.release()
    }

    override fun onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled()
    }

    private fun stopPlayback() {
        mVideoView.stopPlayback()
    }

    enum class LeanbackPlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }

    private inner class MediaSessionCallback : MediaSession.Callback()
}
