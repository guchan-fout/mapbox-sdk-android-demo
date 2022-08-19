package com.example.mapbox.demo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mapbox.geojson.Point;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.gestures.GesturesPluginImpl;
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

public class MapByJavaActivity extends AppCompatActivity {
    private MapView mapView;
    private LocationComponentPlugin plugin;
    private Context context;

    private OnMapLongClickListener mapLongClickListener = new OnMapLongClickListener() {
        @Override
        public boolean onMapLongClick(@NonNull Point point) {
            Log.d("mapLongClickListener", point.toString());
            return false;
        }
    };

    private OnIndicatorPositionChangedListener indicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            Log.d("onIndicatorPositionChanged", point.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mapView = new MapView(this);
        setContentView(mapView);
        context = this;


        mapView.getMapboxMap().loadStyleUri(
                Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        //for set Location update
                        plugin = LocationComponentUtils.getLocationComponent(mapView);
                        plugin.setEnabled(true);
                        //for custom puck
                        LocationPuck2D customepuck = new LocationPuck2D();
                        customepuck.setTopImage(ContextCompat.getDrawable(context, R.drawable.custompuck));
                        plugin.setLocationPuck(customepuck);

                        //for get location
                        plugin.addOnIndicatorPositionChangedListener(indicatorPositionChangedListener);

                        //for long click
                        GesturesPluginImpl gesturesPlugin = mapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
                        gesturesPlugin.addOnMapLongClickListener(mapLongClickListener);
                    }
                }
        );
    }
}