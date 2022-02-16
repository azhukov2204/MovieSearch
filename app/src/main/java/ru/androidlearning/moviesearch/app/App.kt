package ru.androidlearning.moviesearch.app

import android.app.Application
import androidx.room.Room
import ru.androidlearning.moviesearch.model.db.MovieDao
import ru.androidlearning.moviesearch.model.db.MovieDatabase

const val ERROR_TEXT_CREATING_DB = "Application is null while creating DataBase"

class App : Application() {
    companion object {
        private var appInstance: App? = null
        private var movieDatabase: MovieDatabase? = null
        private const val DB_NAME = "Movies.db"

        fun getMovieDao(): MovieDao {
            if (movieDatabase == null) {
                synchronized(MovieDatabase::class.java) {
                    if (movieDatabase == null) {
                        if (appInstance == null) throw IllegalStateException(ERROR_TEXT_CREATING_DB)
                        movieDatabase = Room.databaseBuilder(appInstance!!.applicationContext, MovieDatabase::class.java, DB_NAME)
                            .build()
                    }
                }
            }
            return movieDatabase!!.movieDao()
        }
    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }
}
