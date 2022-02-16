package ru.androidlearning.moviesearch.model.web

import androidx.lifecycle.MutableLiveData
import retrofit2.Callback
import ru.androidlearning.moviesearch.ui.search.MoviesListsFromWebLoadState

interface MoviesWebRepository {
    fun getMoviesListFromServer(
        moviesListsLiveData: MutableLiveData<MoviesListsFromWebLoadState>,
        language: String,
        useAdultsContent: Boolean,
        pageNumber: Int = 1
    )

    fun getMovieDetailsFromServer(
        callback: Callback<MovieDetailsDTO>,
        movieId: Int,
        language: String
    )

    fun getActorDetailsFromServer(callback: Callback<ActorDetailsDTO>, actorId: Int, language: String)

    fun searchMovies(
        moviesSearchLiveData: MutableLiveData<MoviesListsFromWebLoadState>,
        query: String,
        language: String,
        useAdultsContent: Boolean
    )
}
