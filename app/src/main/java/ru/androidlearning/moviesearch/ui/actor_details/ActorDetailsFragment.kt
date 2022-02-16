package ru.androidlearning.moviesearch.ui.actor_details

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.databinding.ActorDetailsFragmentBinding
import ru.androidlearning.moviesearch.model.web.ActorDetailsDTO
import ru.androidlearning.moviesearch.model.web.ActorItem
import ru.androidlearning.moviesearch.ui.MainActivity
import ru.androidlearning.moviesearch.ui.maps.MapsFragment
import ru.androidlearning.moviesearch.ui.movie_details.ERROR_LOADING_DETAILS_MESSAGE
import ru.androidlearning.moviesearch.ui.search.POSTERS_BASE_URL
import ru.androidlearning.moviesearch.utils.showSnackBar
import java.util.*

const val DEFAULT_EMPTY_STRING = ""

class ActorDetailsFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle) = ActorDetailsFragment().apply { arguments = bundle }
    }

    private var _binding: ActorDetailsFragmentBinding? = null
    private val actorDetailsFragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private val actorDetailsViewModel: ActorDetailsViewModel by lazy {
        ViewModelProvider(this).get(ActorDetailsViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActorDetailsFragmentBinding.inflate(inflater, container, false)
        mainActivity.showHomeButton()
        mainActivity.title = getString(R.string.actorDetailsFragmentTitle)
        return actorDetailsFragmentBinding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val actorItem = arguments?.getParcelable<ActorItem>(ActorItem.ACTOR_BUNDLE_KEY)

        actorItem?.id?.let { actorId ->
            actorDetailsViewModel.run {
                actorDetailsWebLiveData.observe(viewLifecycleOwner) { renderData(it) }
                if (savedInstanceState == null) {  //чтоб каждый раз при смене ориентации не запрашивать данные с сервера
                    getActorDetailsFromServer(actorId, getString(R.string.language))
                }
            }
        }
    }

    private fun renderData(actorDetailsFromWebLoadState: ActorDetailsFromWebLoadState?) {
        when (actorDetailsFromWebLoadState) {
            is ActorDetailsFromWebLoadState.Success -> setData(actorDetailsFromWebLoadState.actorDetailsDTO)
            is ActorDetailsFromWebLoadState.Error -> onErrorAction(actorDetailsFromWebLoadState.error.message)
            ActorDetailsFromWebLoadState.Loading -> onLoadingAction()
        }
    }

    private fun setData(actorDetailsDTO: ActorDetailsDTO) {
        with(actorDetailsFragmentBinding) {
            actorDetailsFragmentBinding.actorDetailFragmentLoadingLayout.visibility = View.GONE
            actorName.text = actorDetailsDTO.name
            val birthday = actorDetailsDTO.birthday
            val placeOfBirth = actorDetailsDTO.placeOfBirth

            if (placeOfBirth.isNullOrBlank()) {
                showPlaceOnMapButton.visibility = View.GONE
            }
            if (placeOfBirth.isNullOrBlank() && birthday.isNullOrBlank()) {
                dayOfBirthTitle.visibility = View.GONE
            }

            dateAndPlaceOfBirth.text =
                String.format(
                    Locale.getDefault(), getString(R.string.date_and_place_of_birth_formatted), birthday ?: DEFAULT_EMPTY_STRING, placeOfBirth ?: DEFAULT_EMPTY_STRING
                )
            actorBiography.text = actorDetailsDTO.biography
            Picasso.get().load("$POSTERS_BASE_URL${actorDetailsDTO.actorPhotoURL}").into(actorDetailsFragmentBinding.actorPhoto)

            actorDetailsFragmentBinding.showPlaceOnMapButton.setOnClickListener {
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.container, MapsFragment.newInstance(placeOfBirth, actorDetailsDTO.name, false))
                    ?.addToBackStack(null)
                    ?.commitAllowingStateLoss()
            }
        }
    }

    private fun onErrorAction(errorMessage: String?) {
        val message = "$ERROR_LOADING_DETAILS_MESSAGE: $errorMessage"
        actorDetailsFragmentBinding.actorDetailFragmentLoadingLayout.showSnackBar(message)
    }

    private fun onLoadingAction() {
        actorDetailsFragmentBinding.actorDetailFragmentLoadingLayout.visibility = View.VISIBLE
    }
}
