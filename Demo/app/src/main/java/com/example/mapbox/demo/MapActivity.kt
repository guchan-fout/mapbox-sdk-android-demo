package com.example.mapbox.demo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.api.tilequery.MapboxTilequery
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapIdleEventData
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.delegates.listeners.OnMapIdleListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class MapActivity : AppCompatActivity(),OnMapIdleListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mapView: MapView
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private lateinit var retrieveBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        mapView = MapView(this)
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        //mapView.logo.enabled = false
        mapView.attribution.enabled = false
        mapView.getMapboxMap().addOnMapIdleListener(this)

        val cameraBoundsOptions = CameraBoundsOptions.Builder()
            .maxPitch(10.0)
            .build()
        mapView.getMapboxMap().setBounds(cameraBoundsOptions)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            var source = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                LocationRequest.QUALITY_HIGH_ACCURACY,
                source.token
            ).addOnSuccessListener { it: Location ->
                // todo:success called 3 times, will check this as , use lastLocation as template
                Timber.d("location is long:" + it.longitude + "lat:" + it.latitude)
                longitude = it.longitude
                latitude = it.latitude
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .zoom(11.0)
                        .center(com.mapbox.geojson.Point.fromLngLat(it.longitude, it.latitude))
                        .build()
                )



                var params: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                this.addContentView(mapView, params)

            }


/*
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    Timber.d("location is long:" + location?.longitude + "lat:" + location?.latitude)

                    if (location != null) {
                        longitude = location.longitude
                        latitude = location.latitude
                        mapView.getMapboxMap().setCamera(
                            CameraOptions.Builder()
                                .zoom(11.0)
                                .center(
                                    com.mapbox.geojson.Point.fromLngLat(
                                        location.longitude,
                                        location.latitude
                                    )
                                )
                                .build()
                        )
                    }
                    var params: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    this.addContentView(mapView, params)
                }

 */
        }
        var params: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        retrieveBtn = Button(this)
        //retrieveBtn.layoutParams = params
        this.addContentView(retrieveBtn, params)
        retrieveBtn.setOnClickListener {
            getTileQuery()
        }
        retrieveBtn.text = "Retrieve tilequery"
        retrieveBtn.setTextColor(Color.BLUE)
    }


    fun getTileQuery() {
        Timber.d("startToReivev" + longitude + "lat" + latitude)
        val tilequery = MapboxTilequery.builder()
            .accessToken(resources.getString(R.string.mapbox_access_token))
            .tilesetIds("mapbox.mapbox-streets-v8")
            .query(Point.fromLngLat(longitude, latitude))
            .radius(100)
            //.limit(5)
            //.geometry(geoJsonGeometryString) // "point", "linestring", or "polygon"
            //.dedupe(boolean)
            //.layers(singleOrListOfMapLayerIds) // layer name within a tileset, not a style
            .build()

        tilequery.enqueueCall(object : Callback<FeatureCollection> {
            override fun onResponse(
                call: Call<FeatureCollection>,
                response: Response<FeatureCollection>
            ) {
                Timber.d("onResponse of tile query")
                val featureList = response.body()?.features()
                Timber.d(featureList.toString())

            }

            override fun onFailure(call: Call<FeatureCollection>, throwable: Throwable) {
                Timber.d("Request failed: %s", throwable.message)
                //Log.d(TAG, "Request failed: %s", throwable.message)

            }
        })
    }

    override fun onMapIdle(eventData: MapIdleEventData) {
        Timber.d("onMapIdle: %s", eventData.toString())
    }


}