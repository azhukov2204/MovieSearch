package ru.androidlearning.moviesearch.ui.search

import ru.androidlearning.moviesearch.model.Movie

sealed class MoviesListsFromWebLoadState {
    data class Success(val movies: List<Movie>): MoviesListsFromWebLoadState()
    data class Error(val error: Throwable): MoviesListsFromWebLoadState()
    object Loading: MoviesListsFromWebLoadState()
}
