package ru.androidlearning.moviesearch.model.web.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import ru.androidlearning.moviesearch.model.web.ActorDetailsDTO

interface ActorDetailsAPI {
    @GET
    fun getActorDetails(
        @Url url: String,
        @Query("api_key") token: String,
        @Query("language") language: String
    ): Call<ActorDetailsDTO>
}
