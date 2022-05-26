package com.example.googlemap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class User(val username: String, val snipppet: String, val latLng: LatLng,val profilePhoto : Int  = R.drawable.baseline_person_pin_black_24dp) : ClusterItem {
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