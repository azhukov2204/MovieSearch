package ru.androidlearning.moviesearch.model.web.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import ru.androidlearning.moviesearch.model.web.MovieDetailsDTO

interface MovieDetailsAPI {
    @GET
    fun getMovieDetails(
        @Url url: String,
        @Query("api_key") token: String,
        @Query("language") language: String,
        @Query("append_to_response") actorsInfo: String = "credits"
    ): Call<MovieDetailsDTO>
}
