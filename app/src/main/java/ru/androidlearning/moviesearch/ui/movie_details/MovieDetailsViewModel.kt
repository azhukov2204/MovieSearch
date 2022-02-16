package ru.androidlearning.moviesearch.ui.movie_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import ru.androidlearning.moviesearch.model.web.MovieDetailsDTO
import ru.androidlearning.moviesearch.model.web.MoviesWebRepository
import retrofit2.Callback
import retrofit2.Response
import ru.androidlearning.moviesearch.model.db.MovieEntity
import ru.androidlearning.moviesearch.model.db.MoviesDBRepository
import ru.androidlearning.moviesearch.model.db.MoviesDBRepositoryImpl
import ru.androidlearning.moviesearch.model.web.MoviesWebRepositoryImpl

const val ERROR_CORRUPT_DATA = "Corrupt data"

class MovieDetailsViewModel(
    val movieDetailsWebLiveData: MutableLiveData<MovieDetailsFromWebLoadState> = MutableLiveData(),
    val movieDetailsDBLiveData: MutableLiveData<MovieDetailsAdditionalDataFromDBLoadState> = MutableLiveData()
) : ViewModel() {
    private val moviesWebRepository: MoviesWebRepository = MoviesWebRepositoryImpl()
    private val moviesDBRepository: MoviesDBRepository = MoviesDBRepositoryImpl()
    private val callback: Callback<MovieDetailsDTO> = object : Callback<MovieDetailsDTO> {
        override fun onResponse(
            call: Call<MovieDetailsDTO>,
            response: Response<MovieDetailsDTO>
        ) {
            val movieDetailsDTO = response.body()
            if (response.isSuccessful && movieDetailsDTO != null) {
                movieDetailsWebLiveData.postValue(MovieDetailsFromWebLoadState.Success(movieDetailsDTO))
            } else {
                movieDetailsWebLiveData.postValue(
                    MovieDetailsFromWebLoadState.Error(Throwable(ERROR_CORRUPT_DATA))
                )
            }
        }

        override fun onFailure(call: Call<MovieDetailsDTO>, t: Throwable) {
            movieDetailsWebLiveData.postValue(MovieDetailsFromWebLoadState.Error(Throwable(t.message)))
        }
    }

    fun getMovieDetailsFromServer(movieId: Int, language: String) {
        movieDetailsWebLiveData.value = MovieDetailsFromWebLoadState.Loading
        moviesWebRepository.getMovieDetailsFromServer(callback, movieId, language)
    }

    fun saveMovieToDB(movieEntity: MovieEntity) {
        moviesDBRepository.saveMovieToDB(movieEntity)
        moviesDBRepository.updateViewedDate(movieEntity.id, movieEntity.viewedDate)
    }

    fun saveNoteTextToDB(movieId: Int, movieNoteText: String) {
        moviesDBRepository.saveNoteTextToDB(movieId, movieNoteText)
    }

    fun getAdditionalMovieInfoFromDB(movieID: Int) {
        moviesDBRepository.getAdditionalMovieInfoFromDB(movieID, movieDetailsDBLiveData)
    }

    fun saveFavoriteValueToDB(movieId: Int, isChecked: Boolean) {
        moviesDBRepository.saveFavoriteValueToDB(movieId, isChecked)
    }
}
