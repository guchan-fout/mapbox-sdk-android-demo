package com.example.mapbox.demo

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mapbox.demo.databinding.AnnotationBinding
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import org.w3c.dom.Text
import timber.log.Timber

class MapLoadSymbolStyleActivity : AppCompatActivity(), OnMapClickListener {
    private lateinit var mapView: MapView
    private lateinit var viewAnnotationManager: ViewAnnotationManager

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        viewAnnotationManager = mapView.viewAnnotationManager
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder().center(
                Point.fromLngLat(
                    LONGITUDE,
                    LATITUDE
                )
            ).zoom(ZOOM).build()
        )
        setContentView(mapView)

        mapView.getMapboxMap().loadStyle(
            style(styleUri = Style.LIGHT) {
                +geoJsonSource(STATION_GEOJSON_SOURCE_ID) {
                    url("asset://N02-20_Station.geojson")
                }
                +symbolLayer(STATION_GEOJSON_LAYER_ID, STATION_GEOJSON_SOURCE_ID) {
                    textField(get("N02_005"))
                    textColor(Color.GREEN)
                    textSize(12.0)
                    iconAllowOverlap(true)
                }
                +circleLayer(layerId = "station_dot", sourceId = STATION_GEOJSON_SOURCE_ID) {
                    circleRadius(2.0)
                    circleColor(Color.RED)
                    circleOpacity(0.3)
                    circleStrokeColor(Color.WHITE)
                }
            }
        ) { // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
            mapView.getMapboxMap().addOnMapClickListener(this)
        }
    }

    override fun onMapClick(point: Point): Boolean {
        Timber.d("onMapClick:%s", point.toString())
        mapView.getMapboxMap().queryRenderedFeatures(
            RenderedQueryGeometry(mapView.getMapboxMap().pixelForCoordinate(point)),
            RenderedQueryOptions(listOf(STATION_GEOJSON_LAYER_ID), null)
        ) { expectedFeatures ->
            if (expectedFeatures.isValue && expectedFeatures.value?.size!! > 0) {
                expectedFeatures.value?.get(0)?.feature?.let { feature ->
                    Timber.d("feature:%s", feature.toString())
/*
                    val stationName:TextView = findViewById(R.id.annotation)
                    stationName.textSize = 15f
                    stationName.text = "123"

                    viewAnnotationManager.addViewAnnotation(
                        view = stationName,
                        options = viewAnnotationOptions {
                            geometry(point)
                        }
                    )
*/
                }
            }
        }
        return true
    }


    public override fun onDestroy() {
        super.onDestroy()
        mapView.getMapboxMap().removeOnMapClickListener(this)
    }

    companion object {
        private const val STATION_GEOJSON_LAYER_ID = "train_line_layer"
        private const val STATION_GEOJSON_SOURCE_ID = "train_line_source"
        private const val LATITUDE = 35.78735289961631
        private const val LONGITUDE = 139.70111409343542

        //private const val LONGITUDE = -122.486052
        //private const val LATITUDE = 37.830348
        private const val ZOOM = 7.5
        private const val GEOJSON_SOURCE_ID = "line"
    }
}