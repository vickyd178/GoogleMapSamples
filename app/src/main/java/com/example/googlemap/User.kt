package com.example.googlemap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class User(val username: String, val snipppet: String, val latLng: LatLng) : ClusterItem {
    override fun getPosition(): LatLng {
        return latLng
    }

    override fun getTitle(): String? {
        return username
    }

    override fun getSnippet(): String? {
        return snipppet
    }
}