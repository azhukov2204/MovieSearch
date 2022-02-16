package ru.androidlearning.moviesearch.model.web

import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.androidlearning.moviesearch.BuildConfig
import ru.androidlearning.moviesearch.model.*
import ru.androidlearning.moviesearch.model.web.api.*
import ru.androidlearning.moviesearch.ui.search.MoviesListsFromWebLoadState
import java.lang.Exception

const val BASE_URL = "https://api.themoviedb.org/"
const val BASE_MOVIE_DETAILS_URL = "https://api.themoviedb.org/3/movie/"
const val BASE_PERSON_DETAILS_URL = "https://api.themoviedb.org/3/person/"
const val ERROR_LOADING_GENRES = "Error with loading genres list"
const val ERROR_LOADING_MOVIES_LIST = "Error with loading movies list"
const val DEFAULT_PAGE_NUMBER = 1

private val moviesAPIClassesMap = mapOf(
    "Latest" to MoviesListLatestAPI::class.java,
    "Now Playing" to MoviesListNowPlayingAPI::class.java,
    "Upcoming" to MoviesListUpcomingAPI::class.java,
    "Popular" to MoviesListPopularAPI::class.java,
    "Top Rated" to MoviesListTopRatedAPI::class.java
)

private val genresAPIClass = GenresListAPI::class.java
private val movieDetailsAPIClass = MovieDetailsAPI::class.java
private val movieSearchAPIClass = MoviesSearchAPI::class.java
private val actorDetailsAPIClass = ActorDetailsAPI::class.java

class MoviesRemoteWebDataSource {
    private lateinit var moviesListDTOMap: MutableMap<String, MoviesListDTO>
    private lateinit var moviesSearchResult: MutableList<Movie>
    private var genresListDTO: GenresListDTO? = null

    fun searchMovies(moviesSearchLiveData: MutableLiveData<MoviesListsFromWebLoadState>, query: String, language: String, useAdultsContent: Boolean) {
        Thread {
            try {
                val movieSearchThread = createThreadForMoviesSearchQuery(query, language, useAdultsContent)
                movieSearchThread.start()
                movieSearchThread.join()
                when {
                    genresListDTO == null -> {
                        moviesSearchLiveData.postValue(MoviesListsFromWebLoadState.Error(Throwable(ERROR_LOADING_GENRES)))
                    }
                    moviesListDTOMap.isEmpty() -> {
                        moviesSearchLiveData.postValue(MoviesListsFromWebLoadState.Error(Throwable(ERROR_LOADING_MOVIES_LIST)))
                    }
                    else -> {
                        moviesSearchLiveData.postValue(MoviesListsFromWebLoadState.Success(moviesSearchResult))
                    }
                }
            } catch (e: Exception) {
                moviesSearchLiveData.postValue(MoviesListsFromWebLoadState.Error(e))
            }
        }.start()
    }

    fun getMoviesList(
        moviesListsLiveData: MutableLiveData<MoviesListsFromWebLoadState>,
        language: String,
        useAdultsContent: Boolean,
        pageNumber: Int = DEFAULT_PAGE_NUMBER
    ) {
        Thread {
            try {
                val moviesList = mutableListOf<Movie>()
                val threadsForGetMoviesListsQueries = mutableListOf<Thread>()
                val getGenresThread = createThreadForGetGenresListQuery(language)
                getGenresThread.start()
                for (moviesAPIEntry in moviesAPIClassesMap) {
                    val getMoviesThread = createThreadForGetMoviesListsQueries(moviesAPIEntry.value, pageNumber, language, useAdultsContent, moviesAPIEntry.key)
                    threadsForGetMoviesListsQueries.add(getMoviesThread)
                    getMoviesThread.start()
                }
                for (thread in threadsForGetMoviesListsQueries) {
                    thread.join()
                }
                getGenresThread.join()

                when {
                    genresListDTO == null -> {
                        moviesListsLiveData.postValue(MoviesListsFromWebLoadState.Error(Throwable(ERROR_LOADING_GENRES)))
                    }
                    moviesListDTOMap.isEmpty() -> {
                        moviesListsLiveData.postValue(MoviesListsFromWebLoadState.Error(Throwable(ERROR_LOADING_MOVIES_LIST)))
                    }
                    else -> {
                        addMoviesListDTOMapToMoviesList(moviesList)
                        moviesListsLiveData.postValue(MoviesListsFromWebLoadState.Success(moviesList))
                    }
                }
            } catch (e: Exception) {
                moviesListsLiveData.postValue(MoviesListsFromWebLoadState.Error(e))
            }
        }.start()
    }

    fun getMovieDetails(callback: Callback<MovieDetailsDTO>, movieId: Int, language: String) {
        val urlWithMovieId = "$BASE_MOVIE_DETAILS_URL$movieId"
        buildAPI(movieDetailsAPIClass).getMovieDetails(
            urlWithMovieId,
            BuildConfig.MOVIE_DB_API_KEY,
            language,
        ).enqueue(callback)
    }

    fun getActorDetails(callback: Callback<ActorDetailsDTO>, actorId: Int, language: String) {
        val urlWithActorId = "$BASE_PERSON_DETAILS_URL$actorId"
        buildAPI(actorDetailsAPIClass).getActorDetails(
            urlWithActorId,
            BuildConfig.MOVIE_DB_API_KEY,
            language,
        ).enqueue(callback)
    }

    private fun createThreadForGetMoviesListsQueries(
        apiClass: Class<out MoviesListAPI>, pageNumber: Int, language: String, useAdultsContent: Boolean, movieCategory: String
    ) = Thread {
        try {
            moviesListDTOMap = mutableMapOf()
            val moviesListResponse = buildAPI(apiClass).getMoviesList(BuildConfig.MOVIE_DB_API_KEY, pageNumber, useAdultsContent, language).execute()
            val moviesListDTO = moviesListResponse.body()
            if (moviesListResponse.isSuccessful && moviesListDTO != null) {
                synchronized(moviesListDTOMap) { moviesListDTOMap[movieCategory] = moviesListDTO }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createThreadForMoviesSearchQuery(query: String, language: String, useAdultsContent: Boolean) = Thread {
        try {
            moviesSearchResult = mutableListOf()
            val moviesListResponse = buildAPI(movieSearchAPIClass).getMoviesList(BuildConfig.MOVIE_DB_API_KEY, query, language, useAdultsContent).execute()
            val moviesListDTO = moviesListResponse.body()
            if (moviesListResponse.isSuccessful && moviesListDTO != null) {
                addMoviesListDTOtoMoviesList(moviesSearchResult, moviesListDTO, "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createThreadForGetGenresListQuery(language: String) = Thread {
        try {
            val genresListResponse = buildAPI(genresAPIClass).getGenres(BuildConfig.MOVIE_DB_API_KEY, language).execute()
            genresListDTO = genresListResponse.body()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun <T> buildAPI(service: Class<T>): T = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build().create(service)

    private fun addMoviesListDTOtoMoviesList(moviesList: MutableList<Movie>, moviesListDTO: MoviesListDTO?, movieCategory: String) {
        moviesListDTO?.results?.run {
            for (movieDTO in this) {
                moviesList.add(mapMovieItemDTOtoMovie(movieDTO, genresListDTO, movieCategory))
            }
        }
    }

    private fun addMoviesListDTOMapToMoviesList(moviesList: MutableList<Movie>) {
        for (moviesListDTOEntry in moviesListDTOMap) {
            addMoviesListDTOtoMoviesList(moviesList, moviesListDTOEntry.value, moviesListDTOEntry.key)
        }
    }

    private fun mapMovieItemDTOtoMovie(movieDTO: MovieDTO, genresListDTO: GenresListDTO?, movieCategory: String) = Movie(
        id = movieDTO.id,
        title = movieDTO.title,
        releaseDate = movieDTO.release_date,
        rating = movieDTO.vote_average,
        posterUri = movieDTO.poster_path,
        genre = getGenresStringFromIds(movieDTO.genre_ids, genresListDTO),
        durationInMinutes = 0,
        description = movieDTO.overview,
        category = movieCategory,
        isAdult = movieDTO.isAdult
    )

    private fun getGenresStringFromIds(genreIds: List<Int>?, genresListDTO: GenresListDTO?): String {
        var genresString = ""
        if (genreIds != null) {
            for (genreId in genreIds) {
                genresString += "${genresListDTO?.genres?.find { it.id == genreId }?.name}, "
            }
        }
        return genresString.removeSuffix(", ")
    }
}
