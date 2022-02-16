package ru.androidlearning.moviesearch.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MovieEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val title: String?,
    val releaseDate: String?,
    val rating: Double?,
    val posterUri: String? = null,
    val genre: String?,
    var durationInMinutes: Int?,
    val description: String?,
    val isAdult: Boolean?,
    val note: String?,
    val isFavorites: Boolean?,
    val viewedDate: String?
)
