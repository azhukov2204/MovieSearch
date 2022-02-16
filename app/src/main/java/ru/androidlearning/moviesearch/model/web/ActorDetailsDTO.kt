package ru.androidlearning.moviesearch.model.web

import com.google.gson.annotations.SerializedName

data class ActorDetailsDTO(
    @SerializedName("name")
    val name: String?,
    @SerializedName("biography")
    val biography: String?,
    @SerializedName("birthday")
    val birthday: String?,
    @SerializedName("profile_path")
    val actorPhotoURL: String?,
    @SerializedName("place_of_birth")
    val placeOfBirth: String?
)
