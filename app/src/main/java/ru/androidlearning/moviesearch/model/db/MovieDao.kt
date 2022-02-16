package ru.androidlearning.moviesearch.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovieDao {
    @Query("SELECT * FROM MovieEntity")
    fun getAllMoviesList(): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(movieEntity: MovieEntity)

    @Query("DELETE FROM MovieEntity")
    fun deleteAll()

    @Query("UPDATE MovieEntity SET note = :note WHERE id = :id")
    fun updateNote(id: Int, note: String)

    @Query("SELECT * FROM MovieEntity WHERE id = :movieID")
    fun getMovieByID(movieID: Int): MovieEntity

    @Query("UPDATE MovieEntity SET viewedDate = :viewedDate WHERE id = :movieId")
    fun updateViewedDate(movieId: Int, viewedDate: String?)

    @Query("UPDATE MovieEntity SET isFavorites = :checked WHERE id = :movieId")
    fun updateIsFavorites(movieId: Int, checked: Boolean)

    @Query("SELECT * FROM MovieEntity WHERE isFavorites = 1")
    fun getFavoriteMovies(): List<MovieEntity>

    @Query("SELECT * FROM MovieEntity WHERE viewedDate is not null")
    fun getMoviesListWithViewedDate(): List<MovieEntity>

    @Query("UPDATE MovieEntity SET viewedDate = null WHERE viewedDate is not null")
    fun clearMoviesHistory()

    @Query("DELETE FROM MovieEntity WHERE viewedDate is null and (isFavorites = 0 or isFavorites is null) and note is null")
    fun deleteUnused()
}
