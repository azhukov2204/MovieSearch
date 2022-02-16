package ru.androidlearning.moviesearch.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import ru.androidlearning.moviesearch.utils.OttoBus

private const val GEOFENCE_LOG_TAG = "Geofence"
private const val UNEXPECTED_TYPE_TRANSITION_MESSAGE = "Unexpected type:"

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(GEOFENCE_LOG_TAG, errorMessage)
            return
        }
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            val triggeringGeofence = geofencingEvent.triggeringGeofences
            val geofenceTransitionDetails = getGeofenceTransitionDetails(triggeringGeofence)
            Log.i(GEOFENCE_LOG_TAG, geofenceTransitionDetails)
            OttoBus.bus.post(geofenceTransitionDetails)
        } else {
            Log.d(GEOFENCE_LOG_TAG, "$UNEXPECTED_TYPE_TRANSITION_MESSAGE $geofenceTransition")
        }
    }

    private fun getGeofenceTransitionDetails(triggeringGeofence: List<Geofence>): String {
        val triggeringGeofenceIdsList = ArrayList<String>()
        for (geofence in triggeringGeofence) {
            triggeringGeofenceIdsList.add(geofence.requestId)
        }
        return TextUtils.join(", ", triggeringGeofenceIdsList)
    }
}
