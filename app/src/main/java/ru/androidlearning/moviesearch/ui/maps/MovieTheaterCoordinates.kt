package ru.androidlearning.moviesearch.ui.maps

data class MovieTheaterCoordinates(
    val name: String,
    val lat: Double,
    val lng: Double
) {
    companion object {
        const val GEOFENCE_RADIUS = 200f
        const val GEOFENCE_LOITERING_DELAY_IN_MS = 10000

        //Несколько кинотеатров Екатеринбурга:
        fun getMovieTheaters() = listOf(
            MovieTheaterCoordinates("КАРО Фильм", 56.81697780829516, 60.53981009870767),
            MovieTheaterCoordinates("Синема Парк", 56.83249828923891, 60.582847818732255),
            MovieTheaterCoordinates("Гринвич Синема", 56.82836456249233, 60.59861384332181),
            MovieTheaterCoordinates("Пассаж Синема", 56.836570852456816, 60.59598192572593),
            MovieTheaterCoordinates("Дом Кино", 56.83729731566908, 60.62202617526054)
        )
    }
}
