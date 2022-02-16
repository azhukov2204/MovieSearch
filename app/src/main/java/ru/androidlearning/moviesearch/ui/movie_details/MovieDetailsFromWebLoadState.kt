package ru.androidlearning.moviesearch.ui.movie_details

import ru.androidlearning.moviesearch.model.web.MovieDetailsDTO

sealed class MovieDetailsFromWebLoadState{
    data class Success(val movieDetailsDTO: MovieDetailsDTO): MovieDetailsFromWebLoadState()
    data class Error(val error: Throwable): MovieDetailsFromWebLoadState()
    object Loading: MovieDetailsFromWebLoadState()
}
