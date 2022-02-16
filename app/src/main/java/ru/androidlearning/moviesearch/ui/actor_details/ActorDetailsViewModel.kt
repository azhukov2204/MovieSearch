package ru.androidlearning.moviesearch.ui.actor_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.androidlearning.moviesearch.model.web.ActorDetailsDTO
import ru.androidlearning.moviesearch.model.web.MoviesWebRepository
import ru.androidlearning.moviesearch.model.web.MoviesWebRepositoryImpl
import ru.androidlearning.moviesearch.ui.movie_details.ERROR_CORRUPT_DATA

class ActorDetailsViewModel(
    val actorDetailsWebLiveData: MutableLiveData<ActorDetailsFromWebLoadState> = MutableLiveData()
) : ViewModel() {
    private val moviesWebRepository: MoviesWebRepository = MoviesWebRepositoryImpl()
    private val callback: Callback<ActorDetailsDTO> = object : Callback<ActorDetailsDTO> {
        override fun onResponse(call: Call<ActorDetailsDTO>, response: Response<ActorDetailsDTO>) {
            val movieDetailsDTO = response.body()
            if (response.isSuccessful && movieDetailsDTO != null) {
                actorDetailsWebLiveData.postValue(ActorDetailsFromWebLoadState.Success(movieDetailsDTO))
            } else {
                actorDetailsWebLiveData.postValue(
                    ActorDetailsFromWebLoadState.Error(Throwable(ERROR_CORRUPT_DATA))
                )
            }
        }

        override fun onFailure(call: Call<ActorDetailsDTO>, t: Throwable) {
            actorDetailsWebLiveData.postValue(ActorDetailsFromWebLoadState.Error(Throwable(t.message)))
        }
    }

    fun getActorDetailsFromServer(actorId: Int, language: String) {
        actorDetailsWebLiveData.value = ActorDetailsFromWebLoadState.Loading
        moviesWebRepository.getActorDetailsFromServer(callback, actorId, language)
    }
}
