package ru.androidlearning.moviesearch.ui.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.androidlearning.moviesearch.model.db.MoviesDBRepository
import ru.androidlearning.moviesearch.model.db.MoviesDBRepositoryImpl

class MoviesHistoryViewModel(
    val moviesHistoryFromDBLiveData: MutableLiveData<MoviesListFromDBLoadState> = MutableLiveData()
) : ViewModel() {
    private val moviesDBRepository: MoviesDBRepository = MoviesDBRepositoryImpl()

    fun getMoviesHistoryFromDB() {
        moviesHistoryFromDBLiveData.value = MoviesListFromDBLoadState.Loading
        moviesDBRepository.getAllMoviesFromDB(moviesHistoryFromDBLiveData)
    }

    fun clearMoviesHistory() {
        moviesDBRepository.clearMoviesHistoryInDB()
    }
}
