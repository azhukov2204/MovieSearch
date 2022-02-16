package ru.androidlearning.moviesearch.ui.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.databinding.MapsFragmentBinding
import java.io.IOException

private const val REFRESH_PERIOD = 60000L
private const val MINIMAL_DISTANCE = 100f
private const val DEFAULT_ZOOM = 10f
val defaultLocation = LatLng(56.833332, 60.583332)
const val DEFAULT_EMPTY_STRING = "'"
private const val DEFAULT_USE_MAP_POSITIONING = true

const val ADDRESS_BUNDLE_KEY = "ADDRESS_BUNDLE_KEY"
const val MARKER_TITLE_BUNDLE_KEY = "MARKER_TITLE_BUNDLE_KEY"
const val USE_MAP_POSITIONING_BUNDLE_KEY = "USE_MAP_POSITIONING_BUNDLE_KEY"

class MapsFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(address: String? = null, markerTitle: String? = null, useMapPositioning: Boolean = DEFAULT_USE_MAP_POSITIONING) =
            MapsFragment().apply {
                arguments = Bundle()
                arguments?.apply {
                    putString(ADDRESS_BUNDLE_KEY, address)
                    putString(MARKER_TITLE_BUNDLE_KEY, markerTitle)
                    putBoolean(USE_MAP_POSITIONING_BUNDLE_KEY, useMapPositioning)
                }
            }
    }

    private var _binding: MapsFragmentBinding? = null
    private val mapsFragmentBinding get() = _binding!!
    private lateinit var map: GoogleMap
    private val markers = mutableListOf<Marker>()
    private var address: String? = null
    private var markerTitle: String? = null
    private var useMapPositioning: Boolean = DEFAULT_USE_MAP_POSITIONING


    private val permissionToFineLocationResult: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            getSelfLocation()
        } else {
            context?.let { context ->
                AlertDialog.Builder(context)
                    .setTitle(getString(R.string.access_to_fine_location_text))
                    .setMessage(getString(R.string.explanation_of_fine_location_permission))
                    .setNegativeButton(getString(R.string.close_button_text)) { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation))
        addMovieTheatersOnMap()
        checkStringAddressAndShowPlace(address, markerTitle)
        checkPermissionToFineLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapsFragmentBinding.inflate(inflater, container, false)
        return mapsFragmentBinding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            address = it.getString(ADDRESS_BUNDLE_KEY, null)
            markerTitle = it.getString(MARKER_TITLE_BUNDLE_KEY, null)
            useMapPositioning = it.getBoolean(USE_MAP_POSITIONING_BUNDLE_KEY, DEFAULT_USE_MAP_POSITIONING)
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        initSearchByAddress()
    }

    private fun checkPermissionToFineLocation() {
        context?.let {
            when {
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                    getSelfLocation()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    requestAccessToFineLocationWithDialog()
                }
                else -> {
                    permissionToFineLocationResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    private fun requestAccessToFineLocationWithDialog() {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.access_to_fine_location_title))
                .setMessage(getString(R.string.explanation_of_fine_location_permission))
                .setPositiveButton(getString(R.string.grant_access_button_text)) { _, _ ->
                    permissionToFineLocationResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                .setNegativeButton(getString(R.string.negative_button_text)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    private fun getSelfLocation() {
        context?.let {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val locationManager = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val criteria = Criteria().apply { accuracy = Criteria.ACCURACY_COARSE }
                val provider = locationManager.getBestProvider(criteria, true)
                provider?.let {
                    locationManager.requestLocationUpdates(provider, REFRESH_PERIOD, MINIMAL_DISTANCE, onLocationListener)
                }
                activateMyLocation()
            } else {
                requestAccessToFineLocationWithDialog()
            }
        }
    }

    private fun activateMyLocation() {
        context?.let {
            val isPermissionGranted = (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            map.isMyLocationEnabled = isPermissionGranted
            map.uiSettings.isMyLocationButtonEnabled = isPermissionGranted
        }
    }

    private val onLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            //по сути - позиционирование карты раз в 60 секунд:
            val position = LatLng(location.latitude, location.longitude)
            if (useMapPositioning) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM))
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun initSearchByAddress() {
        mapsFragmentBinding.findButton.setOnClickListener {
            val searchText = mapsFragmentBinding.searchField.text.toString()
            checkStringAddressAndShowPlace(searchText, searchText)
        }
    }

    private fun checkStringAddressAndShowPlace(stringAddress: String?, markerTitle: String?) {
        stringAddress?.let {
            val geoCoder = Geocoder(context)
            Thread {
                try {
                    val addresses = geoCoder.getFromLocationName(stringAddress, 1)
                    if (addresses.size > 0) {
                        goToAddress(addresses, markerTitle ?: DEFAULT_EMPTY_STRING)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun goToAddress(addresses: List<Address>, searchText: String) {
        val location = LatLng(addresses[0].latitude, addresses[0].longitude)
        activity?.runOnUiThread {
            //Будет только один активный маркер:
            for (marker in markers) {
                marker.remove()
            }
            setMarker(location, searchText)?.let { markers.add(it) }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM))
        }
    }

    private fun setMarker(location: LatLng, title: String): Marker? {
        return map.addMarker(MarkerOptions().position(location).title(title))
    }

    private fun addMovieTheatersOnMap() {
        for (movieTheaterCoordinates in MovieTheaterCoordinates.getMovieTheaters()) {
            map.addMarker(MarkerOptions().position(LatLng(movieTheaterCoordinates.lat, movieTheaterCoordinates.lng)).title(movieTheaterCoordinates.name))
        }
    }

}
