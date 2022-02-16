package ru.androidlearning.moviesearch.ui.actor_details

import ru.androidlearning.moviesearch.model.web.ActorDetailsDTO

sealed class ActorDetailsFromWebLoadState {
    data class Success(val actorDetailsDTO: ActorDetailsDTO): ActorDetailsFromWebLoadState()
    data class Error(val error: Throwable): ActorDetailsFromWebLoadState()
    object Loading: ActorDetailsFromWebLoadState()
}
