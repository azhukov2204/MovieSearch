package ru.androidlearning.moviesearch.model.web.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.androidlearning.moviesearch.model.web.MoviesListDTO

interface MoviesSearchAPI {
    @GET("3/search/movie")
    fun getMoviesList(
        @Query("api_key") token: String,
        @Query("query") query: String,
        @Query("language") language: String,
        @Query("include_adult") useAdultsContent: Boolean
    ): Call<MoviesListDTO>
}
