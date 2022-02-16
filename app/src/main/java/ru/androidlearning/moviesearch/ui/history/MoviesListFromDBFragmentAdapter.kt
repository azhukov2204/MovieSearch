package ru.androidlearning.moviesearch.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.model.db.MovieEntity
import ru.androidlearning.moviesearch.ui.search.*
import ru.androidlearning.moviesearch.utils.mapMovieEntityToMovie

class MoviesListFromDBFragmentAdapter : RecyclerView.Adapter<MoviesListFromDBFragmentAdapter.MoviesListFromDBFragmentHolder>() {
    private var movieEntitiesList: List<MovieEntity> = listOf()
    private var filteredMovieEntitiesList: List<MovieEntity> = listOf()
    private var onMovieItemClickListener: MoviesListsFragmentAdapter.OnMovieItemClickListener? = null

    fun setDataSortByViewedDate(movieEntitiesList: List<MovieEntity>) {
        this.movieEntitiesList = movieEntitiesList.sortedByDescending { it.viewedDate }
        filteredMovieEntitiesList = this.movieEntitiesList
        notifyDataSetChanged()
    }

    fun setDataSortByMovieID(movieEntitiesList: List<MovieEntity>) {
        this.movieEntitiesList = movieEntitiesList.sortedBy { it.id }
        filteredMovieEntitiesList = this.movieEntitiesList
        notifyDataSetChanged()
    }

    fun applyFilter(query: String?) {
        filteredMovieEntitiesList = if (!query.isNullOrBlank()) {
            movieEntitiesList.filter {
                (it.title?.lowercase()?.contains(query.lowercase()) == true) ||
                        (it.genre?.lowercase()?.contains(query.lowercase()) == true) ||
                        (it.releaseDate?.lowercase()?.contains(query.lowercase()) == true) ||
                        (it.description?.lowercase()?.contains(query.lowercase()) == true) ||
                        (it.note?.lowercase()?.contains(query.lowercase()) == true)
            }
        } else {
            movieEntitiesList
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesListFromDBFragmentHolder {
        return MoviesListFromDBFragmentHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.movie_history_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MoviesListFromDBFragmentHolder, position: Int) {
        holder.bind(filteredMovieEntitiesList[position])
    }

    override fun getItemCount(): Int {
        return filteredMovieEntitiesList.size
    }

    fun setOnClickListener(onMovieItemClickListener: MoviesListsFragmentAdapter.OnMovieItemClickListener) {
        this.onMovieItemClickListener = onMovieItemClickListener
    }

    fun removeListener() {
        onMovieItemClickListener = null
    }

    inner class MoviesListFromDBFragmentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(movieEntity: MovieEntity) {
            with(itemView) {
                findViewById<TextView>(R.id.movie_name).text = movieEntity.title
                findViewById<TextView>(R.id.movie_genre).text = movieEntity.genre
                val movieRatingView = findViewById<TextView>(R.id.movie_rating)
                movieRatingView.text = movieEntity.rating.toString()
                Picasso.get().load("$POSTERS_BASE_URL${movieEntity.posterUri}")
                    .into(findViewById<AppCompatImageView>(R.id.actorPhoto))
                val forAdultTextView = findViewById<TextView>(R.id.for_adult)
                if (movieEntity.isAdult == true) {
                    forAdultTextView.visibility = View.VISIBLE
                } else {
                    forAdultTextView.visibility = View.GONE
                }
                findViewById<TextView>(R.id.viewed_date).text = movieEntity.viewedDate
                movieRatingView.setTextColor(
                    when {
                        movieEntity.rating ?: DEFAULT_RATING > GOOD_RATING -> {
                            ContextCompat.getColor(context, R.color.green)
                        }
                        movieEntity.rating ?: DEFAULT_RATING > MIDDLE_RATING -> {
                            ContextCompat.getColor(context, R.color.gray)
                        }
                        else -> {
                            ContextCompat.getColor(context, R.color.red)
                        }
                    }
                )
                setOnClickListener { onMovieItemClickListener?.onMovieItemClick(mapMovieEntityToMovie(movieEntity)) }
            }
        }
    }
}
