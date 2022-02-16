package ru.androidlearning.moviesearch.model.db

import androidx.lifecycle.MutableLiveData
import ru.androidlearning.moviesearch.ui.movie_details.MovieDetailsAdditionalDataFromDBLoadState
import ru.androidlearning.moviesearch.ui.history.MoviesListFromDBLoadState

interface MoviesDBRepository {
    fun getAllMoviesFromDB(MoviesHistoryDBLiveData: MutableLiveData<MoviesListFromDBLoadState>)
    fun saveMovieToDB(movieEntity: MovieEntity)
    fun clearMoviesHistoryInDB()
    fun saveNoteTextToDB(movieId: Int, movieNoteText: String)
    fun getAdditionalMovieInfoFromDB(movieID: Int, movieDetailsDBLiveData: MutableLiveData<MovieDetailsAdditionalDataFromDBLoadState>)
    fun updateViewedDate(movieId: Int, viewedDate: String?)
    fun saveFavoriteValueToDB(movieId: Int, checked: Boolean)
    fun getFavoriteMoviesFromDB(moviesFavoriteFromDBLiveData: MutableLiveData<MoviesListFromDBLoadState>)
}
