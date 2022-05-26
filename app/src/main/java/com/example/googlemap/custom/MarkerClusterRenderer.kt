package com.example.googlemap.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.googlemap.R
import com.example.googlemap.User
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlin.Boolean
import kotlin.getValue
import kotlin.lazy


@SuppressLint("PotentialBehaviorOverride")
class MarkerClusterRenderer(
    private val context: Context, private val map: GoogleMap, clusterManager: ClusterManager<User>
) : DefaultClusterRenderer<User>(context, map, clusterManager),
    ClusterManager.OnClusterClickListener<User>, GoogleMap.OnInfoWindowClickListener {

    override fun onClusterRendered(cluster: Cluster<User>, marker: Marker) {
        super.onClusterRendered(cluster, marker)

        println("marker : ${marker.id}, $marker")
    }

    private val iconGenerator: IconGenerator = IconGenerator(context)
    private val clusterMarkerView =
        LayoutInflater.from(context).inflate(R.layout.item_layout_cluster_marker, null)

    private val clusterItemMarkerView =
        LayoutInflater.from(context).inflate(R.layout.item_layout_cluster_item_marker, null)


    private val drawable = ContextCompat.getDrawable(context, android.R.color.transparent)


    init {
        clusterManager.setOnClusterClickListener(this);
        map.setOnMarkerClickListener(clusterManager);
        map.setOnCameraIdleListener(clusterManager);
        map.setInfoWindowAdapter(clusterManager.markerManager);
        clusterManager.markerCollection.setInfoWindowAdapter(MyItemInfoView(context))
        map.setOnInfoWindowClickListener(this);

    }

    override fun onBeforeClusterItemRendered(item: User, markerOptions: MarkerOptions) {
        markerOptions.icon(getMarkerIcon(item))
    }

    override fun onClusterItemUpdated(item: User, marker: Marker) {
        marker.setIcon(getMarkerIcon(item))

    }

    override fun setOnClusterInfoWindowClickListener(listener: ClusterManager.OnClusterInfoWindowClickListener<User>?) {
        super.setOnClusterInfoWindowClickListener(listener)
    }

    override fun onBeforeClusterRendered(cluster: Cluster<User>, markerOptions: MarkerOptions) {
        markerOptions.icon(getClusterIcon(cluster))
    }


    override fun onClusterUpdated(cluster: Cluster<User>, marker: Marker) {
        marker.setIcon(getClusterIcon(cluster))
    }

    override fun shouldRenderAsCluster(cluster: Cluster<User>): Boolean {
        return cluster.size > CustomClusterActivity.MIN_CLUSTER_LIMIT - 1 // setting more than two users should be clustered
    }

    private fun getClusterIcon(cluster: Cluster<User>): BitmapDescriptor? {
        iconGenerator.setBackground(drawable)
        iconGenerator.setContentView(clusterMarkerView)
        val singleClusterMarkerSizeTextView: TextView =
            clusterMarkerView.findViewById(R.id.singleClusterMarkerSizeTextView)
        singleClusterMarkerSizeTextView.text = cluster.size.toString()
        val icon = iconGenerator.makeIcon()

        return BitmapDescriptorFactory.fromBitmap(icon)
    }


    private fun getMarkerIcon(cluster: User): BitmapDescriptor? {
        iconGenerator.setBackground(drawable)
        iconGenerator.setContentView(clusterItemMarkerView)
        val singleClusterMarkerSizeTextView: TextView =
            clusterItemMarkerView.findViewById(R.id.textViewClusterItem)
        singleClusterMarkerSizeTextView.text = cluster.username
        val icon = iconGenerator.makeIcon()
        return BitmapDescriptorFactory.fromBitmap(icon)
    }


    override fun onClusterClick(cluster: Cluster<User>?): Boolean {

        if (cluster == null) return false

        println("marker : ${cluster.size})")
        val builder = LatLngBounds.Builder()
        cluster.items?.forEach { user ->
            builder.include(user.position)
        }
        val bounds = builder.build()
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        val padding = (width * 0.30).toInt()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        map.animateCamera(cu)

        return true
    }

    override fun onInfoWindowClick(marker: Marker) {
        val user = marker.tag as User

        Toast.makeText(context, user.username, Toast.LENGTH_SHORT).show()
    }

    override fun onClusterItemRendered(clusterItem: User, marker: Marker) {
        marker.tag = clusterItem

    }


    private class MyItemInfoView(context: Context) : InfoWindowAdapter {
        private val clusterItemView: View
        private val textTitle: TextView
        private val textDescription: TextView

        init {
            clusterItemView =
                LayoutInflater.from(context).inflate(R.layout.item_layout_marker_info, null)
            textTitle = clusterItemView.findViewById(R.id.title)
            textDescription = clusterItemView.findViewById(R.id.description)

        }

        override fun getInfoWindow(marker: Marker): View {
            val user: User = marker.tag as User? ?: return clusterItemView
            user.let {
                textTitle.text = user.username
                textDescription.text = user.snipppet
            }
            return clusterItemView
        }

        override fun getInfoContents(marker: Marker): View? {
            return null
        }
    }

}