package com.example.googlemap.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.googlemap.R
import com.example.googlemap.User
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator


/**
 * Draws profile photos inside markers (using IconGenerator).
 * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
 */
class PersonRenderer(
    private val context: Context,
    private val map: GoogleMap,
    private val mClusterManager: ClusterManager<User>
) : DefaultClusterRenderer<User>(
    context, map, mClusterManager
) {
    private val mIconGenerator = IconGenerator(context)
    private val mClusterIconGenerator = IconGenerator(context)
    private val mImageView: ImageView
    private val mClusterImageView: ImageView
    private val mDimension: Int

    init {
        val multiProfile: View = LayoutInflater.from(context).inflate(R.layout.multi_profile, null)
        mClusterIconGenerator.setContentView(multiProfile)
        mClusterImageView = multiProfile.findViewById(R.id.imageview)
        mImageView = ImageView(context)
        mDimension = 200
        mImageView.layoutParams = ViewGroup.LayoutParams(mDimension, mDimension)
        val padding = 50
        mImageView.setPadding(padding, padding, padding, padding)
        mIconGenerator.setContentView(mImageView)
    }

    override fun onBeforeClusterItemRendered(person: User, markerOptions: MarkerOptions) {
        // Draw a single person - show their profile photo and set the info window to show their name
        markerOptions.icon(getItemIcon(person)).title(person.title)
    }

    override fun onClusterItemUpdated(person: User, marker: Marker) {
        // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
        marker.setIcon(getItemIcon(person))
        marker.title = person.title
    }

    /**
     * Get a descriptor for a single person (i.e., a marker outside a cluster) from their
     * profile photo to be used for a marker icon
     *
     * @param person person to return an BitmapDescriptor for
     * @return the person's profile photo as a BitmapDescriptor
     */
    private fun getItemIcon(person: User): BitmapDescriptor {
        mImageView.setImageResource(person.profilePhoto)
        val icon = mIconGenerator.makeIcon()
        return BitmapDescriptorFactory.fromBitmap(icon)
    }

    override fun onBeforeClusterRendered(cluster: Cluster<User>, markerOptions: MarkerOptions) {
        // Draw multiple people.
        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
        markerOptions.icon(getClusterIcon(cluster))
    }

    override fun onClusterUpdated(cluster: Cluster<User>, marker: Marker) {
        // Same implementation as onBeforeClusterRendered() (to update cached markers)
        marker.setIcon(getClusterIcon(cluster))
    }

    /**
     * Get a descriptor for multiple people (a cluster) to be used for a marker icon. Note: this
     * method runs on the UI thread. Don't spend too much time in here (like in this example).
     *
     * @param cluster cluster to draw a BitmapDescriptor for
     * @return a BitmapDescriptor representing a cluster
     */
    private fun getClusterIcon(cluster: Cluster<User>): BitmapDescriptor {
        val tvMarker = TextView(context)
        val layoutParams = RelativeLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        tvMarker.layoutParams = layoutParams
        tvMarker.layout(15, 15, 100, 100);
        tvMarker.setPadding(15, 15, 15, 15);
        tvMarker.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        tvMarker.setTypeface(null, Typeface.BOLD);
        tvMarker.background = ContextCompat.getDrawable(context, R.drawable.cluster_background);
        tvMarker.gravity = Gravity.CENTER;
        tvMarker.textAlignment
        tvMarker.text = "${cluster.size}";

        val smallMarker: Bitmap = getBitmapFromView(tvMarker)
        return BitmapDescriptorFactory.fromBitmap(smallMarker)
    }


    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    override fun shouldRenderAsCluster(cluster: Cluster<User>): Boolean {
        return cluster.size > 1

    }
}
