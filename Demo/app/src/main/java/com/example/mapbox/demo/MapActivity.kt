package com.example.mapbox.demo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData
import com.mapbox.maps.extension.observable.eventdata.MapIdleEventData
import com.mapbox.maps.extension.observable.eventdata.MapLoadedEventData
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.delegates.listeners.OnMapIdleListener
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadedListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.location2
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.animation.camera
import timber.log.Timber


class MapActivity : AppCompatActivity(), OnMapIdleListener, OnMapLoadedListener,
    OnCameraChangeListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mapView: MapView
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private lateinit var retrieveBtn: Button

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)

        Timber.d( "now zoom is" + mapView.getMapboxMap().cameraState.zoom)


    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        mapView = MapView(this)


        // mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        mapView.getMapboxMap().loadStyleUri(
            Style.DARK,
            //"mapbox://styles/chan-gu/clbnb2ak1000514o6w0euv8ni",
            // After the style is loaded, initialize the Location component.

            object : Style.OnStyleLoaded {

                override fun onStyleLoaded(style: Style) {
                    var params: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    addContentView(mapView, params)
                    //initLocationComponent()
                    setupGesturesListener()

                    mapView.location.updateSettings {
                        enabled = true

                        this.locationPuck = LocationPuck2D(
                            bearingImage = AppCompatResources.getDrawable(
                                this@MapActivity,
                                R.drawable.mapbox_user_puck_icon,
                            ),
                            shadowImage = AppCompatResources.getDrawable(
                                this@MapActivity,
                                R.drawable.mapbox_user_icon_shadow,
                            ),
                            scaleExpression = interpolate {
                                linear()
                                zoom()
                                stop {
                                    literal(0.0)
                                    literal(0.6)
                                }
                                stop {
                                    literal(20.0)
                                    literal(1.0)
                                }
                            }.toJson()
                        )
                    }
                    mapView.location.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
                    mapView.location.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
                }
            }
        )

        //mapView.attribution.getMapAttributionDelegate().telemetry().setUserTelemetryRequestState()
        //mapView.logo.enabled = false
        mapView.getMapboxMap().addOnMapLoadedListener(this)
        mapView.getMapboxMap().addOnCameraChangeListener(this)


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
                        .zoom(15.0)
                        .center(Point.fromLngLat(it.longitude, it.latitude))
                        .build()
                )


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
            //getTileQuery()
            mapView.location.updateSettings {
                //enabled = true
                Timber.d("updateSettings2")
            }
        }
        retrieveBtn.text = "Retrieve tilequery"
        retrieveBtn.setTextColor(Color.BLUE)
    }

/*
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
*/
    override fun onMapIdle(eventData: MapIdleEventData) {
        Timber.d("onMapIdle: %s", eventData.toString())
    }

    override fun onMapLoaded(eventData: MapLoadedEventData) {
        Timber.d("onMapLoaded: %s", eventData.toString())
        val locationComponentPlugin = mapView.location2
        locationComponentPlugin.updateSettings {
            enabled = true
        }
        locationComponentPlugin.updateSettings2 {

        }
        mapView.getMapboxMap().addOnMapIdleListener(this)
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }




    override fun onCameraChanged(eventData: CameraChangedEventData) {
        //Timber.d("onCameraChanged: %s", eventData.toString())
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

}