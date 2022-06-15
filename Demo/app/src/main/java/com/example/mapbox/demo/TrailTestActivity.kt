package com.example.mapbox.demo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.R
import com.mapbox.maps.extension.style.expressions.dsl.generated.switchCase
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import timber.log.Timber

class TrailTestActivity : AppCompatActivity(), OnMapClickListener {
    var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private lateinit var style_mapbox: Style


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResourceOptionsManager.getDefault(
            this,
            "pk.eyJ1IjoiZGhhcm1hbWFwcyIsImEiOiJjbDB1bG9zOXgwbHRwM2puMXA2dHlreTR4In0.77dkvZOa_CORthcIazLfug"
        )
            .update {
                tileStoreUsageMode(TileStoreUsageMode.READ_ONLY)
            }
        setContentView(com.example.mapbox.demo.R.layout.activity_trail)
        mapView = findViewById(R.id.mapView)
        mapboxMap = mapView?.getMapboxMap()
        mapView?.getMapboxMap()
            ?.loadStyleUri("mapbox://styles/dharmamaps/cl0tyhlie005115p5x32nwa0a") { style ->
                style_mapbox = style
                mapboxMap?.addOnMapClickListener(this)
            }


    }

    fun colorChange(name: String, style_mapbox: Style) {
        val layerHalodev =
            style_mapbox.getLayer(AppConstant.MapBoxConstants.trails_dev_halo) as LineLayer

        val expressiontrail: Expression = switchCase {
            eq {
                get {

                    literal(AppConstant.MapBoxConstants.T_Name)
                }
                literal(name)
            }
            stop {

                literal(AppConstant.MapBoxConstants.TRAIL_SEL_YELLOW)
                literal(AppConstant.MapBoxConstants.TRAIL_SEL_COLOR)


            }


        }
        layerHalodev.lineColor(expressiontrail)


    }

    override fun onMapClick(point: Point): Boolean {

        Timber.d("onMapClicked")
        return handleClickIcon(point)
    }

    @SuppressLint("InflateParams")
    private fun handleClickIcon(screenPoint: Point): Boolean {
        mapboxMap?.getStyle {
            val pixel = mapboxMap?.pixelForCoordinate(screenPoint)
            Timber.d("handleClickIcon,pixel=%s", pixel.toString())

            // change the point to a bbox, please adjust the range, 100 is just for demo
            if (pixel != null) {
                val min = ScreenCoordinate(pixel.x - 100, pixel.y - 100)
                val max = ScreenCoordinate(pixel.x + 100, pixel.y + 100)
                val box = ScreenBox(min, max)
                Timber.d("handleClickIcon,box=%s", box.toString())

                // search a bbox instead of a point
                mapboxMap?.queryRenderedFeatures(
                    box,
                    RenderedQueryOptions(
                        listOf(
                            AppConstant.MapBoxConstants.trails_dev_halo,
                            AppConstant.MapBoxConstants.trails_dev,
                        ), null
                    )
                )
                { expected ->
                    expected.value?.let { value ->
                        val queriedFeatures = value

                        val featurelayer = queriedFeatures.firstOrNull()?.feature?.properties()
                        // I used this log to check if clicked on the trail
                        Timber.d("featurelayer=%s", featurelayer)
                        val trail_name = featurelayer?.get(AppConstant.MapBoxConstants.T_Name)

                        if (trail_name != null) {
                            trail_name.let { trailinfo ->
                                colorChange(trailinfo.asString, style_mapbox)
                            }
                        }
                        return@queryRenderedFeatures
                    }
                }
            }
        }


        return true
    }
}

object AppConstant {

    interface MapBoxConstants {
        companion object {

            val trails_dev_halo: String = "trails-halo"
            val trails_dev: String = "trails"
            val T_Name: String = "trail_name"
            val TRAIL_SEL_COLOR = "#F0F0F0"
            val TRAIL_SEL_YELLOW = "#F0ED3C"

        }
    }
}