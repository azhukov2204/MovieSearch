package ru.androidlearning.moviesearch.model.web

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MovieDetailsDTO(
    @SerializedName("runtime")
    val runtime: Int?, //пока буду забирать только одно поле, которое предусмотрено в текущем UI. Затем добавлю другие поля по мере необходимости
    @SerializedName("credits")
    val credits: Credits?
): Parcelable

@Parcelize
data class Credits(
    @SerializedName("cast")
    val actors: List<ActorItem>?
): Parcelable

@Parcelize
data class ActorItem(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("profile_path")
    val actorPhotoURL: String?
): Parcelable
{
    companion object {
        const val ACTOR_BUNDLE_KEY = "Actor"
    }
}
