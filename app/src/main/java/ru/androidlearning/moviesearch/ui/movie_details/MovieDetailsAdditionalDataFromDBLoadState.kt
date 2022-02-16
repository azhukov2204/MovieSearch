package ru.androidlearning.moviesearch.ui.movie_details

import ru.androidlearning.moviesearch.model.db.MovieEntity

sealed class MovieDetailsAdditionalDataFromDBLoadState {
    data class Success(val movieEntity: MovieEntity): MovieDetailsAdditionalDataFromDBLoadState()
    data class Error(val error: Throwable): MovieDetailsAdditionalDataFromDBLoadState()
    object Loading: MovieDetailsAdditionalDataFromDBLoadState()
}
