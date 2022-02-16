package ru.androidlearning.moviesearch.model.db

import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.MutableLiveData
import ru.androidlearning.moviesearch.app.App
import ru.androidlearning.moviesearch.model.web.HANDLER_THREAD_NAME
import ru.androidlearning.moviesearch.ui.movie_details.MovieDetailsAdditionalDataFromDBLoadState
import ru.androidlearning.moviesearch.ui.history.MoviesListFromDBLoadState
import java.lang.Exception

class MoviesDBRepositoryImpl(
    private val movieDBDataSource: MovieDao = App.getMovieDao()
) : MoviesDBRepository {

    companion object {
        private val handlerWorkThread = HandlerThread(HANDLER_THREAD_NAME)

        init {
            handlerWorkThread.start()
        }
    }

    override fun getAllMoviesFromDB(MoviesHistoryDBLiveData: MutableLiveData<MoviesListFromDBLoadState>) {
        Handler(handlerWorkThread.looper).post {
            try {
                val moviesEntitiesList = movieDBDataSource.getMoviesListWithViewedDate()
                MoviesHistoryDBLiveData.postValue(MoviesListFromDBLoadState.Success(moviesEntitiesList))
            } catch (e: Exception) {
                MoviesHistoryDBLiveData.postValue(MoviesListFromDBLoadState.Error(e))
            }
        }
    }

    override fun saveMovieToDB(movieEntity: MovieEntity) {
        Handler(handlerWorkThread.looper).post { movieDBDataSource.insert(movieEntity) }
    }

    override fun updateViewedDate(movieId: Int, viewedDate: String?) {
        Handler(handlerWorkThread.looper).post { movieDBDataSource.updateViewedDate(movieId, viewedDate) }
    }

    override fun clearMoviesHistoryInDB() {
        Handler(handlerWorkThread.looper).post {
            movieDBDataSource.clearMoviesHistory()
            movieDBDataSource.deleteUnused()
        }
    }

    override fun saveNoteTextToDB(movieId: Int, movieNoteText: String) {
        Handler(handlerWorkThread.looper).post { movieDBDataSource.updateNote(movieId, movieNoteText) }
    }

    override fun saveFavoriteValueToDB(movieId: Int, checked: Boolean) {
        Handler(handlerWorkThread.looper).post { movieDBDataSource.updateIsFavorites(movieId, checked) }
    }

    override fun getAdditionalMovieInfoFromDB(movieID: Int, movieDetailsDBLiveData: MutableLiveData<MovieDetailsAdditionalDataFromDBLoadState>) {
        Handler(handlerWorkThread.looper).post {
            try {
                val movieEntity = movieDBDataSource.getMovieByID(movieID)
                movieDetailsDBLiveData.postValue(MovieDetailsAdditionalDataFromDBLoadState.Success(movieEntity))
            } catch (e: Exception) {
                movieDetailsDBLiveData.postValue(MovieDetailsAdditionalDataFromDBLoadState.Error(e))
            }
        }
    }

    override fun getFavoriteMoviesFromDB(moviesFavoriteFromDBLiveData: MutableLiveData<MoviesListFromDBLoadState>) {
        Handler(handlerWorkThread.looper).post {
            try {
                val movieEntity = movieDBDataSource.getFavoriteMovies()
                moviesFavoriteFromDBLiveData.postValue(MoviesListFromDBLoadState.Success(movieEntity))
            } catch (e: Exception) {
                moviesFavoriteFromDBLiveData.postValue(MoviesListFromDBLoadState.Error(e))
            }
        }
    }
}
