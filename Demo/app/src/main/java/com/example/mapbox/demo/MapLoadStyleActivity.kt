package com.example.mapbox.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import timber.log.Timber

class MapLoadStyleActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    val observer = Observer { event ->
        Timber.d("type ${event.type}, data ${event.data.toJson()}")
        when (event.type) {
            MapEvents.SOURCE_DATA_LOADED -> {

            }
            MapEvents.MAP_LOADING_ERROR -> {

            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        mapView.getMapboxMap().subscribe(
            observer, listOf(// Camera events
                MapEvents.CAMERA_CHANGED,
                // Map events
                MapEvents.MAP_IDLE,
                MapEvents.MAP_LOADING_ERROR,
                MapEvents.MAP_LOADED,
                // Style events
                MapEvents.STYLE_DATA_LOADED,
                MapEvents.STYLE_LOADED,
                MapEvents.STYLE_IMAGE_MISSING,
                MapEvents.STYLE_IMAGE_REMOVE_UNUSED,
                // Render frame events
                MapEvents.RENDER_FRAME_STARTED,
                MapEvents.RENDER_FRAME_FINISHED,
                // Source events
                MapEvents.SOURCE_ADDED,
                MapEvents.SOURCE_DATA_LOADED,
                MapEvents.SOURCE_REMOVED
            )
        )
        setContentView(mapView)
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder().center(
                Point.fromLngLat(
                    LATITUDE,
                    LONGITUDE
                )
            ).zoom(ZOOM).build()
        )
        mapView.getMapboxMap().loadStyle(
            style(styleUri = Style.MAPBOX_STREETS) {
                +geoJsonSource(GEOJSON_SOURCE_ID) {
                    url("asset://from_crema_to_council_crest.geojson")
                }
                +lineLayer("linelayer", GEOJSON_SOURCE_ID) {
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                    lineOpacity(0.7)
                    lineWidth(8.0)
                    lineColor("#888")
                }
            },
            object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {
                    // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
                    Timber.d("is Fully loaded:" + mapView.getMapboxMap().isFullyLoaded())
                    Timber.d("is style loaded:" + mapView.getMapboxMap().getStyle()?.isStyleLoaded)
                }
            }
        )
    }

    public override fun onDestroy() {
        super.onDestroy()
        mapView.getMapboxMap().unsubscribe(observer)
    }

    companion object {
        private const val GEOJSON_SOURCE_ID = "line"
        private const val LATITUDE = -122.486052
        private const val LONGITUDE = 37.830348
        private const val ZOOM = 14.0
    }
}