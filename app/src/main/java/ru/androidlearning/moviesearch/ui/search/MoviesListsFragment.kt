package ru.androidlearning.moviesearch.ui.search

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.databinding.MoviesListsFragmentBinding
import ru.androidlearning.moviesearch.model.Movie
import ru.androidlearning.moviesearch.ui.MainActivity
import ru.androidlearning.moviesearch.ui.movie_details.MovieDetailFragment

private const val USE_ADULTS_CONTENT_KEY = "useAdultsContent"
private const val SHARED_PREFERENCES_KEY = "useAdultsContentSharedPref"

class MoviesListsFragment : Fragment() {
    private var _binding: MoviesListsFragmentBinding? = null
    private val movieSearchFragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private val moviesSearchViewModel: MoviesListsViewModel by lazy {
        ViewModelProvider(this).get(MoviesListsViewModel::class.java)
    }
    private var useAdultsContent: Boolean = false
    private val moviesListsFragmentAdapter = MoviesListsFragmentAdapter()
    private val moviesSearchFragmentAdapter = MoviesSearchFragmentAdapter()

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

    private val connectivityActionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val noConnectivity =
                intent?.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
            val reason = intent?.getStringExtra(ConnectivityManager.EXTRA_REASON)
            if (noConnectivity == true) {
                AlertDialog.Builder(context)
                    .setTitle(getString(R.string.errorWord))
                    .setMessage(getString(R.string.connectionErrorDialogText) + reason)
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.dialogOKButtonText), null)
                    .create().show()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MoviesListsFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.registerReceiver(
            connectivityActionReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION) //свойство deprecated, но в Д/З сказано подписаться на это событие
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MoviesListsFragmentBinding.inflate(inflater, container, false)
        mainActivity.hideHomeButton()
        setHasOptionsMenu(true) //используем меню
        return movieSearchFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readUseAdultsContentOption()
        moviesListsFragmentAdapter.setOnClickListener(onMovieItemClickListener)
        moviesSearchFragmentAdapter.setOnClickListener(onMovieItemClickListener)
        movieSearchFragmentBinding.moviesListRecyclerView.adapter = moviesListsFragmentAdapter
        movieSearchFragmentBinding.movieSearchResultsRecyclerView.adapter =
            moviesSearchFragmentAdapter
        moviesSearchViewModel.run {
            getMovieDetailsLiveData().observe(viewLifecycleOwner, { renderDataMoviesList(it) })
            getMoviesSearchLiveData().observe(viewLifecycleOwner, { renderDataMoviesSearch(it) })
            if (savedInstanceState == null) {  //чтоб каждый раз при смене ориентации не запрашивать данные с сервера
                getMoviesFromServer(getString(R.string.language), useAdultsContent)
            }
        }
    }

    override fun onDestroy() {
        context?.unregisterReceiver(connectivityActionReceiver)
        super.onDestroy()
    }

    private fun renderDataMoviesList(moviesListsFromWebLoadState: MoviesListsFromWebLoadState?) {
        when (moviesListsFromWebLoadState) {
            is MoviesListsFromWebLoadState.Success -> onSuccessListLoading(moviesListsFromWebLoadState.movies)
            is MoviesListsFromWebLoadState.Error -> onErrorAction(moviesListsFromWebLoadState.error.message)
            is MoviesListsFromWebLoadState.Loading -> onLoadingAction()
        }
    }

    private fun renderDataMoviesSearch(moviesListsFromWebLoadState: MoviesListsFromWebLoadState?) {
        when (moviesListsFromWebLoadState) {
            is MoviesListsFromWebLoadState.Success -> onSuccessMoviesSearching(moviesListsFromWebLoadState.movies)
            is MoviesListsFromWebLoadState.Error -> onErrorAction(moviesListsFromWebLoadState.error.message)
            is MoviesListsFromWebLoadState.Loading -> onLoadingAction()
        }
    }

    private fun onSuccessMoviesSearching(movies: List<Movie>) {
        movieSearchFragmentBinding.movieSearchFragmentLoadingLayout.visibility = View.GONE
        moviesSearchFragmentAdapter.setData(movies)
    }

    private fun onLoadingAction() {
        movieSearchFragmentBinding.movieSearchFragmentLoadingLayout.visibility = View.VISIBLE
    }

    private fun onErrorAction(message: String?) {
        message?.let {
            movieSearchFragmentBinding.movieSearchFragmentLoadingLayout.showSnackBar(
                message,
                Snackbar.LENGTH_INDEFINITE,
                getString(R.string.tryToReloadButtonText)
            ) {
                moviesSearchViewModel.getMoviesFromServer(
                    getString(R.string.language),
                    useAdultsContent
                )
            }
        }
    }

    private fun onSuccessListLoading(movies: List<Movie>) {
        movieSearchFragmentBinding.movieSearchFragmentLoadingLayout.visibility = View.GONE
        moviesListsFragmentAdapter.setMoviesList(movies)
    }

    override fun onDestroyView() {
        moviesListsFragmentAdapter.removeListener()
        moviesSearchFragmentAdapter.removeListener()
        _binding = null
        super.onDestroyView()
    }

    private fun View.showSnackBar(
        message: String,
        length: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: ((View) -> Unit)? = null
    ) {
        Snackbar.make(this, message, length).setAction(actionText, action).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.movies_search_menu, menu)
        (menu.findItem(R.id.action_search).actionView as SearchView).setOnQueryTextListener(
            searchMoviesListener
        )
        if (useAdultsContent) {
            menu.findItem(R.id.with_adults).isChecked = true
        } else {
            menu.findItem(R.id.no_adults).isChecked = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.with_adults -> {
                item.isChecked = !item.isChecked
                useAdultsContent = true
                saveUseAdultsContentOption()
                true
            }
            R.id.no_adults -> {
                item.isChecked = !item.isChecked
                useAdultsContent = false
                saveUseAdultsContentOption()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private val searchMoviesListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            startMoviesSearching(query)
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            startMoviesSearching(newText)
            return true
        }
    }

    private fun startMoviesSearching(movieSearchString: String?) {
        if (movieSearchString == null || movieSearchString.isBlank()) {
            movieSearchFragmentBinding.movieSearchFragmentSearchResults.visibility = View.GONE
        } else {
            movieSearchFragmentBinding.movieSearchFragmentSearchResults.visibility = View.VISIBLE
            moviesSearchViewModel.searchMovies(
                movieSearchString,
                getString(R.string.language),
                useAdultsContent
            )
        }
    }

    private fun saveUseAdultsContentOption() {
        activity?.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)?.edit()
            ?.putBoolean(USE_ADULTS_CONTENT_KEY, useAdultsContent)?.apply()
    }

    private fun readUseAdultsContentOption() {
        activity?.also {
            useAdultsContent =
                it.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                    .getBoolean(USE_ADULTS_CONTENT_KEY, false)
        }
    }
}
