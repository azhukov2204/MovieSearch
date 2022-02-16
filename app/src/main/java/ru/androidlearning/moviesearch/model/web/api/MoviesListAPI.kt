package ru.androidlearning.moviesearch.model.web.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.androidlearning.moviesearch.model.web.MoviesListDTO

interface MoviesListAPI {
    @GET
    fun getMoviesList(
        @Query("api_key") token: String,
        @Query("page") pageNumber: Int,
        @Query("include_adult") useAdultsContent: Boolean,
        @Query("language") language: String
    ): Call<MoviesListDTO>
}

interface MoviesListLatestAPI : MoviesListAPI {
    @GET("3/movie/latest")
    override fun getMoviesList(
        @Query("api_key") token: String,
        @Query("page") pageNumber: Int,
        @Query("include_adult") useAdultsContent: Boolean,
        @Query("language") language: String
    ): Call<MoviesListDTO>
}

interface MoviesListNowPlayingAPI : MoviesListAPI {
    @GET("3/movie/now_playing")
    override fun getMoviesList(
        @Query("api_key") token: String,
        @Query("page") pageNumber: Int,
        @Query("include_adult") useAdultsContent: Boolean,
        @Query("language") language: String
    ): Call<MoviesListDTO>
}

interface MoviesListPopularAPI: MoviesListAPI {
    @GET("3/movie/popular")
    override fun getMoviesList(
        @Query("api_key") token: String,
        @Query("page") pageNumber: Int,
        @Query("include_adult") useAdultsContent: Boolean,
        @Query("language") language: String
    ): Call<MoviesListDTO>
}

interface MoviesListTopRatedAPI: MoviesListAPI {
    @GET("3/movie/top_rated")
    override fun getMoviesList (
        @Query("api_key") token: String,
        @Query("page") pageNumber: Int,
        @Query("include_adult") useAdultsContent: Boolean,
        @Query("language") language: String
    ): Call<MoviesListDTO>
}

interface MoviesListUpcomingAPI: MoviesListAPI {
    @GET("3/movie/upcoming")
    override fun getMoviesList(
        @Query("api_key") token: String,
        @Query("page") pageNumber: Int,
        @Query("include_adult") useAdultsContent: Boolean,
        @Query("language") language: String
    ): Call<MoviesListDTO>
}
