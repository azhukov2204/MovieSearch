package ru.androidlearning.moviesearch.ui.history

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.databinding.MoviesHistoryFragmentBinding
import ru.androidlearning.moviesearch.model.Movie
import ru.androidlearning.moviesearch.model.db.MovieEntity
import ru.androidlearning.moviesearch.ui.MainActivity
import ru.androidlearning.moviesearch.ui.movie_details.MovieDetailFragment
import ru.androidlearning.moviesearch.ui.search.MoviesListsFragmentAdapter
import ru.androidlearning.moviesearch.utils.showSnackBar

const val ERROR_LOADING_MOVIES_HISTORY_MESSAGE = "Error loading movies history"

class MoviesHistoryFragment : Fragment() {
    private var _binding: MoviesHistoryFragmentBinding? = null
    private val movieHistoryFragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private val moviesHistoryViewModel: MoviesHistoryViewModel by lazy {
        ViewModelProvider(this).get(MoviesHistoryViewModel::class.java)
    }
    private val movieHistoryFragmentAdapter = MoviesListFromDBFragmentAdapter()

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
        @JvmStatic
        fun newInstance() = MoviesHistoryFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MoviesHistoryFragmentBinding.inflate(inflater, container, false)
        mainActivity.hideHomeButton()
        setHasOptionsMenu(true) //используем меню
        return movieHistoryFragmentBinding.root
    }

    override fun onDestroyView() {
        movieHistoryFragmentAdapter.removeListener()
        _binding = null
        super.onDestroyView()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        movieHistoryFragmentBinding.moviesHistoryRecyclerView.adapter = movieHistoryFragmentAdapter
        movieHistoryFragmentAdapter.setOnClickListener(onMovieItemClickListener)
        moviesHistoryViewModel.moviesHistoryFromDBLiveData.observe(viewLifecycleOwner) {
            renderMoviesHistoryData(it)
        }
        moviesHistoryViewModel.getMoviesHistoryFromDB()
    }

    private fun renderMoviesHistoryData(moviesListFromDBLoadState: MoviesListFromDBLoadState) {
        when (moviesListFromDBLoadState) {
            is MoviesListFromDBLoadState.Success -> onSuccessAction(moviesListFromDBLoadState.movieEntities)
            is MoviesListFromDBLoadState.Error -> onErrorAction(moviesListFromDBLoadState.error.message)
            is MoviesListFromDBLoadState.Loading -> onLoadingAction()
        }
    }

    private fun onSuccessAction(movieEntities: List<MovieEntity>) {
        movieHistoryFragmentBinding.movieSearchFragmentLoadingLayout.visibility = View.GONE
        movieHistoryFragmentAdapter.setDataSortByViewedDate(movieEntities)
    }

    private fun onLoadingAction() {
        movieHistoryFragmentBinding.movieSearchFragmentLoadingLayout.visibility = View.VISIBLE
    }

    private fun onErrorAction(errorMessage: String?) {
        val message = "$ERROR_LOADING_MOVIES_HISTORY_MESSAGE: $errorMessage"
        movieHistoryFragmentBinding.movieSearchFragmentLoadingLayout.showSnackBar(message)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.movies_history_menu, menu)
        (menu.findItem(R.id.action_search).actionView as SearchView).setOnQueryTextListener(
            searchMoviesListener
        )
    }

    private val searchMoviesListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            movieHistoryFragmentAdapter.applyFilter(query)
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            movieHistoryFragmentAdapter.applyFilter(newText)
            return true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_history -> {
                moviesHistoryViewModel.clearMoviesHistory()
                movieHistoryFragmentAdapter.setDataSortByViewedDate(listOf()) //пока пойдем простым путем, без обратной связи по факту удаления
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
