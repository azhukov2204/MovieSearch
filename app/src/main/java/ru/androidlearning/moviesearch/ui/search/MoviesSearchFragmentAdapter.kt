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

class MoviesSearchFragmentAdapter :
    RecyclerView.Adapter<MoviesSearchFragmentAdapter.MoviesSearchFragmentHolder>() {
    private var moviesList = listOf<Movie>()
    private var onMovieItemClickListener: MoviesListsFragmentAdapter.OnMovieItemClickListener? =
        null

    fun setData(moviesList: List<Movie>) {
        this.moviesList = moviesList
        notifyDataSetChanged()
    }

    fun setOnClickListener(onMovieItemClickListener: MoviesListsFragmentAdapter.OnMovieItemClickListener) {
        this.onMovieItemClickListener = onMovieItemClickListener
    }

    fun removeListener() {
        onMovieItemClickListener = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesSearchFragmentHolder {
        return MoviesSearchFragmentHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.movie_vertical_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MoviesSearchFragmentHolder, position: Int) {
        holder.bind(moviesList[position])
    }

    override fun getItemCount(): Int {
        return moviesList.size
    }

    inner class MoviesSearchFragmentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(movie: Movie) = with(itemView) {
            findViewById<TextView>(R.id.movie_name).text = movie.title
            findViewById<TextView>(R.id.movie_genre).text = movie.genre
            val movieRatingView = findViewById<TextView>(R.id.movie_rating)
            movieRatingView.text = movie.rating.toString()
            Picasso.get().load("$POSTERS_BASE_URL${movie.posterUri}")
                .into(findViewById<AppCompatImageView>(R.id.actorPhoto))
            val forAdultTextView = findViewById<TextView>(R.id.for_adult)
            if (movie.isAdult == true) {
                forAdultTextView.visibility = View.VISIBLE
            } else {
                forAdultTextView.visibility = View.GONE
            }
            setOnClickListener { onMovieItemClickListener?.onMovieItemClick(movie) }
            movieRatingView.setTextColor(
                when {
                    movie.rating ?: 0.0 > GOOD_RATING -> {
                        ContextCompat.getColor(context, R.color.green)
                    }
                    movie.rating ?: 0.0 > MIDDLE_RATING -> {
                        ContextCompat.getColor(context, R.color.gray)
                    }
                    else -> {
                        ContextCompat.getColor(context, R.color.red)
                    }
                }
            )
        }
    }
}
