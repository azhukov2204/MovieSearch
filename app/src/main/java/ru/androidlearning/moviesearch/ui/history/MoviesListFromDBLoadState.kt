package ru.androidlearning.moviesearch.ui.history

import ru.androidlearning.moviesearch.model.db.MovieEntity

sealed class MoviesListFromDBLoadState {
    data class Success(val movieEntities: List<MovieEntity>): MoviesListFromDBLoadState()
    data class Error(val error: Throwable): MoviesListFromDBLoadState()
    object Loading: MoviesListFromDBLoadState()
}
