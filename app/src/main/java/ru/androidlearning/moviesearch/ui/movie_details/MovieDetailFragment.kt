package ru.androidlearning.moviesearch.ui.movie_details

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.databinding.MovieDetailFragmentBinding
import ru.androidlearning.moviesearch.model.*
import ru.androidlearning.moviesearch.model.db.MovieEntity
import ru.androidlearning.moviesearch.model.web.ActorItem
import ru.androidlearning.moviesearch.model.web.MovieDetailsDTO
import ru.androidlearning.moviesearch.utils.getStringFromDate
import ru.androidlearning.moviesearch.ui.MainActivity
import ru.androidlearning.moviesearch.ui.actor_details.ActorDetailsFragment
import java.util.*

const val ERROR_LOADING_DETAILS_MESSAGE = "Error loading data"
const val POSTERS_BASE_URL = "https://image.tmdb.org/t/p/w200"

class MovieDetailFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle) = MovieDetailFragment().apply { arguments = bundle }
    }

    private var _binding: MovieDetailFragmentBinding? = null
    private val movieDetailFragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private var movie: Movie? = null
    private val movieDetailsViewModel: MovieDetailsViewModel by lazy {
        ViewModelProvider(this).get(MovieDetailsViewModel::class.java)
    }

    private val actorsRecyclerViewAdapter = ActorsRecyclerViewAdapter()

    private val onNoteTextChangedListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            movie?.id?.let { movieDetailsViewModel.saveNoteTextToDB(it, movieDetailFragmentBinding.movieNote.text.toString()) }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    private val onFavoriteCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        movie?.id?.let { movieDetailsViewModel.saveFavoriteValueToDB(it, isChecked) }

    }

    private val onActorClickListener = object : ActorsRecyclerViewAdapter.OnActorClickListener {
        override fun onClick(actor: ActorItem) {
            activity?.supportFragmentManager?.let {
                val bundle = Bundle().apply { putParcelable(ActorItem.ACTOR_BUNDLE_KEY, actor) }
                it.beginTransaction()
                    .replace(R.id.container, ActorDetailsFragment.newInstance(bundle))
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MovieDetailFragmentBinding.inflate(inflater, container, false)
        mainActivity.showHomeButton()
        mainActivity.title = getString(R.string.movieDetailsFragmentTitle)
        movieDetailFragmentBinding.movieNote.addTextChangedListener(onNoteTextChangedListener)
        movieDetailFragmentBinding.favorite.setOnCheckedChangeListener(onFavoriteCheckedChangeListener)
        return movieDetailFragmentBinding.root
    }

    override fun onDestroyView() {
        _binding = null
        actorsRecyclerViewAdapter.removeListener()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        movieDetailFragmentBinding.actorsRecyclerView.adapter = actorsRecyclerViewAdapter
        actorsRecyclerViewAdapter.setOnClickListener(onActorClickListener)
        movie = arguments?.getParcelable(Movie.MOVIE_BUNDLE_KEY)
        movie?.id?.let { movieId ->
            movieDetailsViewModel.run {
                movieDetailsWebLiveData.observe(viewLifecycleOwner) { renderData(it) }
                movieDetailsDBLiveData.observe(viewLifecycleOwner) { renderAdditionalData(it) }
                if (savedInstanceState == null) {  //чтоб каждый раз при смене ориентации не запрашивать данные с сервера
                    getMovieDetailsFromServer(movieId, getString(R.string.language))
                }
            }
        }
    }

    private fun renderData(movieDetailsFromWebLoadState: MovieDetailsFromWebLoadState) {
        when (movieDetailsFromWebLoadState) {
            is MovieDetailsFromWebLoadState.Success -> onSuccessAction(movieDetailsFromWebLoadState.movieDetailsDTO)
            is MovieDetailsFromWebLoadState.Error -> onErrorAction(movieDetailsFromWebLoadState.error.message)
            MovieDetailsFromWebLoadState.Loading -> onLoadingAction()
        }
    }

    private fun renderAdditionalData(movieDetailsAdditionalDataFromDBLoadState: MovieDetailsAdditionalDataFromDBLoadState) {
        when (movieDetailsAdditionalDataFromDBLoadState) {
            is MovieDetailsAdditionalDataFromDBLoadState.Success -> setAdditionalData(movieDetailsAdditionalDataFromDBLoadState.movieEntity)
            is MovieDetailsAdditionalDataFromDBLoadState.Error -> onErrorAction(movieDetailsAdditionalDataFromDBLoadState.error.message)
            MovieDetailsAdditionalDataFromDBLoadState.Loading -> onLoadingAction()
        }
    }

    private fun onLoadingAction() {
        movieDetailFragmentBinding.movieDetailFragmentLoadingLayout.visibility = View.VISIBLE
    }

    private fun onErrorAction(errorMessage: String?) {
        val message = "$ERROR_LOADING_DETAILS_MESSAGE: $errorMessage"
        movieDetailFragmentBinding.movieDetailFragmentLoadingLayout.showSnackBar(message)
    }

    private fun onSuccessAction(movieDetailsDTO: MovieDetailsDTO) {
        movie?.durationInMinutes = movieDetailsDTO.runtime
        movie?.let { setData(it) }
        movieDetailsDTO.credits?.actors?.let { actorsRecyclerViewAdapter.setData(it) }
    }

    private fun setData(movie: Movie) {
        with(movieDetailFragmentBinding) {
            saveDataToDB(movie)
            movieDetailFragmentLoadingLayout.visibility = View.GONE
            movieName.text = movie.title
            movieGenre.text =
                String.format(Locale.getDefault(), getString(R.string.genreWord) + movie.genre)
            movieDuration.text = String.format(
                Locale.getDefault(),
                getString(R.string.durationWord) + movie.durationInMinutes.toString() + " " + getString(
                    R.string.minutesWord
                )
            )
            movieRating.text = String.format(
                Locale.getDefault(),
                getString(R.string.ratingWord) + movie.rating.toString()
            )
            movieReleaseDate.text = String.format(
                Locale.getDefault(),
                getString(R.string.releaseDateWord) + movie.releaseDate
            )
            movieDescription.text = movie.description
            if (movie.isAdult == true) {
                forAdult.visibility = View.VISIBLE
            } else {
                forAdult.visibility = View.GONE
            }
            Picasso.get().load("$POSTERS_BASE_URL${movie.posterUri}").into(moviePoster)
            movie.id?.let { movieDetailsViewModel.getAdditionalMovieInfoFromDB(it) }
        }
    }

    private fun setAdditionalData(movieEntity: MovieEntity) {
        movieDetailFragmentBinding.movieNote.setText(movieEntity.note)
        movieEntity.isFavorites?.let { movieDetailFragmentBinding.favorite.isChecked = it }
    }

    private fun saveDataToDB(movie: Movie, note: String = "", isFavorites: Boolean = false) {
        val movieEntity = MovieEntity(
            id = movie.id!!,
            title = movie.title,
            releaseDate = movie.releaseDate,
            rating = movie.rating,
            posterUri = movie.posterUri,
            genre = movie.genre,
            durationInMinutes = movie.durationInMinutes,
            description = movie.description,
            isAdult = movie.isAdult,
            note = note,
            isFavorites = isFavorites,
            viewedDate = getStringFromDate(Date())
        )
        movieDetailsViewModel.saveMovieToDB(movieEntity)
    }

    private fun View.showSnackBar(
        message: String,
        length: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: ((View) -> Unit)? = null
    ) {
        Snackbar.make(this, message, length).setAction(actionText, action).show()
    }
}
