package ru.androidlearning.moviesearch.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.androidlearning.moviesearch.model.web.MoviesWebRepository
import ru.androidlearning.moviesearch.model.web.MoviesWebRepositoryImpl

class MoviesListsViewModel(
    private val moviesListsLiveData: MutableLiveData<MoviesListsFromWebLoadState> = MutableLiveData(),
    private val moviesSearchLiveData: MutableLiveData<MoviesListsFromWebLoadState> = MutableLiveData()
) : ViewModel() {
    private val moviesWebRepository: MoviesWebRepository = MoviesWebRepositoryImpl()

    fun getMoviesFromServer(language: String, useAdultsContent: Boolean) {
        moviesListsLiveData.value = MoviesListsFromWebLoadState.Loading
        moviesWebRepository.getMoviesListFromServer(moviesListsLiveData, language, useAdultsContent)
    }

    fun searchMovies(query: String, language: String, useAdultsContent: Boolean) {
        moviesSearchLiveData.value = MoviesListsFromWebLoadState.Loading
        moviesWebRepository.searchMovies(moviesSearchLiveData, query, language, useAdultsContent)
    }

    fun getMovieDetailsLiveData() = moviesListsLiveData

    fun getMoviesSearchLiveData() = moviesSearchLiveData
}
