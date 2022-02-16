package ru.androidlearning.moviesearch.ui.favorite

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.androidlearning.moviesearch.model.db.MoviesDBRepository
import ru.androidlearning.moviesearch.model.db.MoviesDBRepositoryImpl
import ru.androidlearning.moviesearch.ui.history.MoviesListFromDBLoadState

class MoviesFavoriteViewModel(
    val moviesFavoriteFromDBLiveData: MutableLiveData<MoviesListFromDBLoadState> = MutableLiveData()
) : ViewModel() {
    private val moviesDBRepository: MoviesDBRepository = MoviesDBRepositoryImpl()

    fun getFavoriteMoviesFromDB() {
        moviesDBRepository.getFavoriteMoviesFromDB(moviesFavoriteFromDBLiveData)
    }
}
