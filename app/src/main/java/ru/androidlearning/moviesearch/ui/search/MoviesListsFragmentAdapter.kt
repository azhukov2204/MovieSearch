package ru.androidlearning.moviesearch.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.model.Movie

class MoviesListsFragmentAdapter :
        RecyclerView.Adapter<MoviesListsFragmentAdapter.MoviesListsFragmentHolder>() {
    private var moviesList = listOf<Movie>()
    private var movieCategoriesList: List<String> = arrayListOf()
    private var onMovieItemClickListener: OnMovieItemClickListener? = null

    fun setMoviesList(moviesList: List<Movie>) {
        this.moviesList = moviesList
        this.movieCategoriesList = getCategoriesListFromMoviesList(moviesList)
        notifyDataSetChanged()
    }

    private fun getCategoriesListFromMoviesList(moviesList: List<Movie>): List<String> {
        val categoriesSet: MutableSet<String> = hashSetOf()
        for (movie in moviesList) {
            categoriesSet.add(movie.category)
        }
        return ArrayList(categoriesSet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesListsFragmentHolder = MoviesListsFragmentHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.movies_horizontal_list_item, parent, false))

    override fun onBindViewHolder(holder: MoviesListsFragmentHolder, position: Int) {
        holder.bind(movieCategoriesList[position], moviesList)
    }

    override fun getItemCount(): Int = movieCategoriesList.size

    fun setOnClickListener(onMovieItemClickListener: OnMovieItemClickListener) {
        this.onMovieItemClickListener = onMovieItemClickListener
    }

    fun removeListener() {
        onMovieItemClickListener = null
    }

    inner class MoviesListsFragmentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(moveCategory: String, moviesList: List<Movie>) {
            val moviesHorizontalListAdapter = MoviesListsHorizontalAdapter(onMovieItemClickListener)
            itemView.apply {
                findViewById<TextView>(R.id.movieCategoryTextView).text = moveCategory
                findViewById<RecyclerView>(R.id.movieHorizontalRecyclerView).adapter = moviesHorizontalListAdapter
            }
            moviesHorizontalListAdapter.setData(moviesList.filter { it.category == moveCategory }, moveCategory)
        }
    }

    interface OnMovieItemClickListener {
        fun onMovieItemClick(movie: Movie)
    }
}
