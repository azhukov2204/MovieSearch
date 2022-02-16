package ru.androidlearning.moviesearch.model.web

import com.google.gson.annotations.SerializedName

data class MoviesListDTO(
    val results: List<MovieDTO>?
)

data class MovieDTO(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("release_date")
    val release_date: String?,
    @SerializedName("vote_average")
    val vote_average: Double?,
    @SerializedName("poster_path")
    val poster_path: String?,
    @SerializedName("genre_ids")
    val genre_ids: List<Int>?,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("adult")
    val isAdult: Boolean?
)
