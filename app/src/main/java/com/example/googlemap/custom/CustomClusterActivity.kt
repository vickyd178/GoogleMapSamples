package com.example.googlemap.custom

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.googlemap.R
import com.example.googlemap.User
import com.example.googlemap.databinding.ActivityCustomClusterBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import kotlin.random.Random


class CustomClusterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomClusterBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var googleMap: GoogleMap

    companion object {
        const val MIN_CLUSTER_LIMIT = 3

        fun defaultMapSettings(googleMap: GoogleMap) {
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isZoomGesturesEnabled = true
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.uiSettings.isRotateGesturesEnabled = true
            googleMap.uiSettings.isTiltGesturesEnabled = true
            googleMap.uiSettings.isCompassEnabled = false
            googleMap.isBuildingsEnabled = true

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomClusterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { mMap ->
            googleMap = mMap
            setupClusterManager(mMap)
            if (hasLocationPermissions(
                    context = this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            ) {
                accessCurrentLocationAndZoom()
            } else {
                permissionRequestLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun accessCurrentLocationAndZoom() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ),
                    5.0f
                )
            )
        }
    }

    private fun setupClusterManager(mMap: GoogleMap) {
        defaultMapSettings(mMap)
        val clusterManager = ClusterManager<User>(this, mMap)
        val clusterRenderer = MarkerClusterRenderer(this, mMap, clusterManager)
        clusterRenderer.minClusterSize = MIN_CLUSTER_LIMIT
        clusterManager.renderer = clusterRenderer
        clusterManager.addItems(getAllItems())
        clusterManager.cluster()

    }


    //get random user locations
    private fun getAllItems(): ArrayList<User> {
        val users = ArrayList<User>()
        val title = "This is the title"
        val snippet = "and this is the snippet."

        // Add ten cluster items in close proximity, for purposes of this example.
        for (i in 0..25) {
            val user = User(
                "$title $i",
                "$snippet $i",
                LatLng(Random.nextDouble(15.35, 22.02), Random.nextDouble(72.7, 80.25))
            )
            users.add(user)
        }
        return users
    }


    private fun hasLocationPermissions(context: Context, permissions: Array<String>): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }

            if (granted) {
                accessCurrentLocationAndZoom()
            }
        }

}