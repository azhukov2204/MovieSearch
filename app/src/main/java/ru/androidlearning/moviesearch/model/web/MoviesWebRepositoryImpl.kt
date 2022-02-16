package ru.androidlearning.moviesearch.model.web

import android.os.HandlerThread
import androidx.lifecycle.MutableLiveData
import retrofit2.Callback
import ru.androidlearning.moviesearch.ui.search.MoviesListsFromWebLoadState

const val HANDLER_THREAD_NAME = "WorkThread"

class MoviesWebRepositoryImpl(
    private val moviesRemoteWebDataSource: MoviesRemoteWebDataSource = MoviesRemoteWebDataSource()
) : MoviesWebRepository {

    companion object {
        private val handlerWorkThread = HandlerThread(HANDLER_THREAD_NAME)

        init {
            handlerWorkThread.start()
        }
    }

    override fun getMoviesListFromServer(
        moviesListsLiveData: MutableLiveData<MoviesListsFromWebLoadState>,
        language: String,
        useAdultsContent: Boolean,
        pageNumber: Int
    ) {
        moviesRemoteWebDataSource.getMoviesList(
            moviesListsLiveData,
            language,
            useAdultsContent,
            pageNumber
        )
    }

    override fun getMovieDetailsFromServer(
        callback: Callback<MovieDetailsDTO>,
        movieId: Int,
        language: String
    ) {
        moviesRemoteWebDataSource.getMovieDetails(callback, movieId, language)
    }

    override fun getActorDetailsFromServer(callback: Callback<ActorDetailsDTO>, actorId: Int, language: String) {
        moviesRemoteWebDataSource.getActorDetails(callback, actorId, language)
    }

    override fun searchMovies(
        moviesSearchLiveData: MutableLiveData<MoviesListsFromWebLoadState>,
        query: String,
        language: String,
        useAdultsContent: Boolean
    ) {
        moviesRemoteWebDataSource.searchMovies(
            moviesSearchLiveData,
            query,
            language,
            useAdultsContent
        )
    }
}
