package ru.androidlearning.moviesearch.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.model.Movie

const val GOOD_RATING = 8.0
const val MIDDLE_RATING = 5.0
const val DEFAULT_RATING = 0.0
const val POSTERS_BASE_URL = "https://image.tmdb.org/t/p/w200"

class MoviesListsHorizontalAdapter(private val onMovieItemClickListener: MoviesListsFragmentAdapter.OnMovieItemClickListener?) :
    RecyclerView.Adapter<MoviesListsHorizontalAdapter.MoviesHorizontalListHolder>() {

    private var filteredMoviesList = listOf<Movie>()
    private lateinit var moveCategory: String

    fun setData(filteredMoviesList: List<Movie>, movesCategory: String) {
        this.moveCategory = movesCategory
        this.filteredMoviesList = filteredMoviesList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesHorizontalListHolder =
        MoviesHorizontalListHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.movie_horizintal_item, parent, false)
        )

    override fun onBindViewHolder(holder: MoviesHorizontalListHolder, position: Int) {
        holder.bind(filteredMoviesList[position])
    }

    override fun getItemCount(): Int = filteredMoviesList.size

    inner class MoviesHorizontalListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(movie: Movie) = with(itemView) {
            findViewById<TextView>(R.id.movie_name).text = movie.title
            findViewById<TextView>(R.id.movie_genre).text = movie.genre
            val movieRatingView = findViewById<TextView>(R.id.movie_rating)
            movieRatingView.text = movie.rating.toString()
            movieRatingView.setBackgroundColor(
                when {
                    movie.rating ?: DEFAULT_RATING > GOOD_RATING -> {
                        ContextCompat.getColor(context, R.color.green)
                    }
                    movie.rating ?: DEFAULT_RATING > MIDDLE_RATING -> {
                        ContextCompat.getColor(context, R.color.gray)
                    }
                    else -> {
                        ContextCompat.getColor(context, R.color.red)
                    }
                }
            )
            val forAdultTextView = findViewById<TextView>(R.id.for_adult)
            if (movie.isAdult == true) {
                forAdultTextView.visibility = View.VISIBLE
            } else {
                forAdultTextView.visibility = View.GONE
            }
            Picasso.get().load("${POSTERS_BASE_URL}${movie.posterUri}")
                .into(findViewById<AppCompatImageView>(R.id.actorPhoto))
            setOnClickListener { onMovieItemClickListener?.onMovieItemClick(movie) }
        }
    }
}
