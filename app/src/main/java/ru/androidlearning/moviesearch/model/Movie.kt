package ru.androidlearning.moviesearch.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
    val id: Int? = 0,
    val title: String?,
    val releaseDate: String?,
    val rating: Double?,
    val posterUri: String? = null,
    val genre: String?,
    var durationInMinutes: Int?,
    val description: String?,
    val category: String,
    val isAdult: Boolean?
) : Parcelable {
    companion object {
        const val MOVIE_BUNDLE_KEY = "Movie"
    }
}
