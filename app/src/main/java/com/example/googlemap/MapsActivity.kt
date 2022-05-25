package com.example.googlemap

import android.location.LocationListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationListenerCompat
import androidx.lifecycle.lifecycleScope
import com.example.googlemap.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding
    private var userList = ArrayList<User>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync {
            setClusterManager(it)

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                it.animateCamera(
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
    }

    private fun setClusterManager(mMap: GoogleMap) {
        val clusterManager = ClusterManager<User>(this, mMap)
        mMap.setOnCameraIdleListener(clusterManager)

        clusterManager.setOnClusterClickListener { cluster ->
            val builder = LatLngBounds.Builder()
            cluster.items.forEach { user ->
                builder.include(user.position)
            }
            val bounds = builder.build()
            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val padding = (width * 0.10).toInt()
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
            mMap.animateCamera(cu)
            true
        }
        lifecycleScope.launchWhenStarted {
            lifecycleScope.launch(Dispatchers.IO) {
                val result = async { getAllItems() }
                val users = result.await()
                withContext(Dispatchers.Main) {
                    clusterManager.addItems(users)
                }
            }
        }

    }

    private fun getAllItems(): ArrayList<User> {
        val users = ArrayList<User>()

        var lat = Random.nextDouble()

        var lng = Random.nextDouble()

// Set the title and snippet strings.
        val title = "This is the title"
        val snippet = "and this is the snippet."

        // Add ten cluster items in close proximity, for purposes of this example.
        for (i in 0..50) {
            val offset = i / 60.0
            lat += offset
            lng += offset
            val user = User(
                "$title $i",
                "$snippet $i",
                LatLng(Random.nextDouble(15.35, 22.02), Random.nextDouble(72.7, 80.25))
            )
            users.add(user)
        }
        return users
    }
}