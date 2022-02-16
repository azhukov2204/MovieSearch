package ru.androidlearning.moviesearch.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.otto.Subscribe
import ru.androidlearning.moviesearch.R
import ru.androidlearning.moviesearch.databinding.MainActivityBinding
import ru.androidlearning.moviesearch.geofence.GeofenceBroadcastReceiver
import ru.androidlearning.moviesearch.ui.favorite.MoviesFavoriteFragment
import ru.androidlearning.moviesearch.ui.history.MoviesHistoryFragment
import ru.androidlearning.moviesearch.ui.maps.MapsFragment
import ru.androidlearning.moviesearch.ui.maps.MovieTheaterCoordinates
import ru.androidlearning.moviesearch.ui.phones_book.PhoneBookFragment
import ru.androidlearning.moviesearch.ui.search.MoviesListsFragment
import ru.androidlearning.moviesearch.utils.OttoBus
import ru.androidlearning.moviesearch.utils.createNotificationChannel
import ru.androidlearning.moviesearch.utils.showNotification

private const val GEOFENCE_LOG_TAG = "Geofence"

private const val ON_SUCCESS_ADD_GEOFENCE_MESSAGE = "Geofences Successfully Added"
private const val ON_FAILURE_ADD_GEOFENCE_MESSAGE = "Failure with Adding Geofences"
private const val NOT_PERMITTED_MESSAGE = "No permission to ACCESS_FINE_LOCATION"
private const val REFRESH_PERIOD = 60000L
private const val MINIMAL_DISTANCE = 100f
private const val GEOFENCE_CHANNEL_ID = "GEOFENCE_CHANNEL_ID"
private const val GEOFENCE_CHANNEL_NAME = "GEOFENCE CHANNEL"
private const val GEOFENCE_CHANNEL_DESCRIPTION = "Geofence change notification channel"
private const val GEOFENCE_CHANNEL_NOTIFICATION_ID = 36

class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityBinding: MainActivityBinding
    private lateinit var geofencingClient: GeofencingClient
    private val geofenceList = mutableListOf<Geofence>()
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OttoBus.bus.register(this)
        initToolBar()
        if (savedInstanceState == null) {
            openFragment(MoviesListsFragment.newInstance(), getString(R.string.movieListsFragmentTitle))
        }

        createNotificationChannel(applicationContext, GEOFENCE_CHANNEL_ID, GEOFENCE_CHANNEL_NAME, GEOFENCE_CHANNEL_DESCRIPTION)
        initGeofences()
    }

    override fun onDestroy() {
        OttoBus.bus.unregister(this)
        super.onDestroy()
    }

    private fun initToolBar() {
        mainActivityBinding = MainActivityBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        setSupportActionBar(mainActivityBinding.toolbar)
        val navView: BottomNavigationView = mainActivityBinding.navView

        navView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.movieListsFragment -> {
                    openFragment(MoviesListsFragment.newInstance(), getString(R.string.movieListsFragmentTitle))
                    true
                }
                R.id.movieHistoryFragment -> {
                    openFragment(MoviesHistoryFragment.newInstance(), getString(R.string.movieHistoryFragmentTitle))
                    true
                }
                R.id.movieFavoriteFragment -> {
                    openFragment(MoviesFavoriteFragment.newInstance(), getString(R.string.moviesFavoriteFragmentTitle))
                    true
                }
                R.id.phonesBookFragment -> {
                    openFragment(PhoneBookFragment.newInstance(), getString(R.string.phoneBookFragmentTitle))
                    true
                }
                R.id.mapsFragment -> {
                    openFragment(MapsFragment.newInstance())
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun <T : Fragment> openFragment(fragmentInstance: T, title: String = getString(R.string.app_name)) {
        clearBackStack()
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragmentInstance)
            .setTransition(TRANSIT_FRAGMENT_FADE)
            .commitAllowingStateLoss()
        this.title = title
    }

    private fun clearBackStack() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            val entry = fragmentManager.getBackStackEntryAt(0)
            fragmentManager.popBackStack(entry.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    fun showHomeButton() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    fun hideHomeButton() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        hideHomeButton()
        super.onBackPressed()
    }

    private fun initGeofences() {
        getSelfLocation() //чтоб геозоны работали во всех элементах этого активити
        geofencingClient = LocationServices.getGeofencingClient(this)
        fillGeofencesList()
        addGeofences()
    }

    private fun fillGeofencesList() {
        for (movieTheaterCoordinates in MovieTheaterCoordinates.getMovieTheaters()) {
            geofenceList.add(
                Geofence.Builder()
                    .setRequestId(movieTheaterCoordinates.name)
                    .setCircularRegion(
                        movieTheaterCoordinates.lat,
                        movieTheaterCoordinates.lng,
                        MovieTheaterCoordinates.GEOFENCE_RADIUS
                    )
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(MovieTheaterCoordinates.GEOFENCE_LOITERING_DELAY_IN_MS)
                    .build()
            )
        }
    }

    private fun addGeofences() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent).run {
                addOnSuccessListener {
                    Log.i(GEOFENCE_LOG_TAG, ON_SUCCESS_ADD_GEOFENCE_MESSAGE)
                }
                addOnFailureListener {
                    Log.e(GEOFENCE_LOG_TAG, "$ON_FAILURE_ADD_GEOFENCE_MESSAGE: ${it.message}")
                    it.printStackTrace()
                }
            }
        } else {
            Log.e(GEOFENCE_LOG_TAG, NOT_PERMITTED_MESSAGE)
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
            addGeofences(geofenceList)
        }.build()
    }

    private fun getSelfLocation() {
        this.let {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val locationManager = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val criteria = Criteria().apply { accuracy = Criteria.ACCURACY_COARSE }
                val provider = locationManager.getBestProvider(criteria, true)
                provider?.let {
                    locationManager.requestLocationUpdates(provider, REFRESH_PERIOD, MINIMAL_DISTANCE, object : LocationListener {
                        override fun onLocationChanged(location: Location) {}
                        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    })
                }
            }
        }
    }

    @Subscribe
    fun showGeofenceChangeNotificationFromBus(answer: String?) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.near_movie_theater_text))
            .setMessage("${getString(R.string.near_movie_theater_text)}: $answer")
            .setNeutralButton(getString(R.string.ok_button_text), null)
            .create()
            .show()
        showNotification(
            applicationContext,
            getString(R.string.near_movie_theater_text),
            "${getString(R.string.near_movie_theater_text)}: $answer",
            GEOFENCE_CHANNEL_ID,
            GEOFENCE_CHANNEL_NOTIFICATION_ID
        )
    }
}
