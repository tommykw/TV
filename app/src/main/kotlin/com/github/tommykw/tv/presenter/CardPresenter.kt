package com.github.tommykw.tv.presenter

import android.graphics.drawable.Drawable
import android.support.v17.leanback.widget.ImageCardView
import android.support.v17.leanback.widget.Presenter
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.github.tommykw.tv.model.Movie
import com.github.tommykw.tv.R

class CardPresenter : Presenter() {
    private var defaultCardImage: Drawable? = null

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        defaultBackgroundColor = parent.resources.getColor(R.color.default_background)
        selectedBackgroundColor = parent.resources.getColor(R.color.selected_background)
        defaultCardImage = parent.resources.getDrawable(R.drawable.movie)

        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                updateCardBackgroundColor(this, selected)
                super.setSelected(selected)
            }
        }

        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)
        return Presenter.ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
        val movie = item as Movie
        val cardView = viewHolder.view as ImageCardView

        if (movie.cardImageUrl != null) {
            cardView.titleText = movie.title
            cardView.contentText = movie.studio
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            Glide.with(viewHolder.view.context)
                .load(movie.cardImageUrl)
                .centerCrop()
                .error(defaultCardImage)
                .into(cardView.mainImageView)
        }
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }

    companion object {
        private const val CARD_WIDTH = 313
        private const val CARD_HEIGHT = 176
        private var selectedBackgroundColor = 0
        private var defaultBackgroundColor = 0

        private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
            val color = if (selected) selectedBackgroundColor else defaultBackgroundColor
            view.setBackgroundColor(color)
            view.findViewById(R.id.info_field).setBackgroundColor(color)
        }
    }
}
