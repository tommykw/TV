package com.github.tommykw.tv.presenter

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter

import com.github.tommykw.tv.model.Movie

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
    override fun onBindDescription(viewHolder: AbstractDetailsDescriptionPresenter.ViewHolder, item: Any) {
        viewHolder.apply {
            val movie = item as Movie
            title.text = movie.title
            subtitle.text = movie.studio
            body.text = movie.description
        }
    }
}
