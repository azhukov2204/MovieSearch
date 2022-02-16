package ru.androidlearning.moviesearch.ui.favorite

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.databinding.MoviesFavoriteFragmentBinding
import ru.androidlearning.moviesearch.model.Movie
import ru.androidlearning.moviesearch.model.db.MovieEntity
import ru.androidlearning.moviesearch.ui.MainActivity
import ru.androidlearning.moviesearch.ui.movie_details.MovieDetailFragment
import ru.androidlearning.moviesearch.ui.history.ERROR_LOADING_MOVIES_HISTORY_MESSAGE
import ru.androidlearning.moviesearch.ui.history.MoviesListFromDBFragmentAdapter
import ru.androidlearning.moviesearch.ui.history.MoviesListFromDBLoadState
import ru.androidlearning.moviesearch.ui.search.MoviesListsFragmentAdapter
import ru.androidlearning.moviesearch.utils.showSnackBar

class MoviesFavoriteFragment : Fragment() {
    private var _binding: MoviesFavoriteFragmentBinding? = null
    private val moviesFavoriteFragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private val moviesFavoriteViewModel: MoviesFavoriteViewModel by lazy {
        ViewModelProvider(this).get(MoviesFavoriteViewModel::class.java)
    }
    private val movieFavoriteFragmentAdapter = MoviesListFromDBFragmentAdapter()

    private val onMovieItemClickListener =
        object : MoviesListsFragmentAdapter.OnMovieItemClickListener {
            override fun onMovieItemClick(movie: Movie) {
                activity?.supportFragmentManager?.let {
                    val bundle = Bundle().apply { putParcelable(Movie.MOVIE_BUNDLE_KEY, movie) }
                    it.beginTransaction()
                        .replace(R.id.container, MovieDetailFragment.newInstance(bundle))
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                }
            }
        }

    companion object {
        fun newInstance() = MoviesFavoriteFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MoviesFavoriteFragmentBinding.inflate(inflater, container, false)
        mainActivity.hideHomeButton()
        setHasOptionsMenu(true) //используем меню
        return moviesFavoriteFragmentBinding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        moviesFavoriteFragmentBinding.moviesFavoriteRecyclerView.adapter = movieFavoriteFragmentAdapter
        movieFavoriteFragmentAdapter.setOnClickListener(onMovieItemClickListener)
        moviesFavoriteViewModel.moviesFavoriteFromDBLiveData.observe(viewLifecycleOwner) {renderMoviesListFromDBData(it)}
        moviesFavoriteViewModel.getFavoriteMoviesFromDB()

    }

    private fun renderMoviesListFromDBData(moviesListFromDBLoadState: MoviesListFromDBLoadState) {
        when (moviesListFromDBLoadState) {
            is MoviesListFromDBLoadState.Success -> onSuccessAction(moviesListFromDBLoadState.movieEntities)
            is MoviesListFromDBLoadState.Error -> onErrorAction(moviesListFromDBLoadState.error.message)
            is MoviesListFromDBLoadState.Loading -> onLoadingAction()
        }
    }

    private fun onSuccessAction(movieEntities: List<MovieEntity>) {
        moviesFavoriteFragmentBinding.movieFavoriteFragmentLoadingLayout.visibility = View.GONE
        movieFavoriteFragmentAdapter.setDataSortByMovieID(movieEntities)
    }

    private fun onLoadingAction() {
        moviesFavoriteFragmentBinding.movieFavoriteFragmentLoadingLayout.visibility = View.VISIBLE
    }

    private fun onErrorAction(errorMessage: String?) {
        val message = "$ERROR_LOADING_MOVIES_HISTORY_MESSAGE: $errorMessage"
        moviesFavoriteFragmentBinding.movieFavoriteFragmentLoadingLayout.showSnackBar(message)
    }
}
