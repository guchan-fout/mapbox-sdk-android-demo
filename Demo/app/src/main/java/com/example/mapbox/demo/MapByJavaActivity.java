package com.example.mapbox.demo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mapbox.api.directions.v5.models.RouteOptions;
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
import com.mapbox.navigation.base.extensions.RouteOptionsExtensions;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;

import java.util.Arrays;
import java.util.List;

public class MapByJavaActivity extends AppCompatActivity {
    private MapView mapView;
    private LocationComponentPlugin plugin;
    private Context context;

    private OnMapLongClickListener mapLongClickListener = new OnMapLongClickListener() {
        @Override
        public boolean onMapLongClick(@NonNull Point point) {
            //Log.d("mapLongClickListener", point.toString());
            return false;
        }
    };

    private OnIndicatorPositionChangedListener indicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            //Log.d("onIndicatorPositionChanged", point.toString());
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
                        createNavigation();


                    }
                }
        );
    }

    public void createNavigation() {
        NavigationOptions options = new NavigationOptions.Builder(this).accessToken(getString(R.string.mapbox_access_token)).build();

        MapboxNavigation navigation = new MapboxNavigation(options);


        //Location originLocation = new Location(37.7627,-122.4192,new Timestamp((System.currentTimeMillis())));

        Point originPoint = Point.fromLngLat(-72.50185583052199, 41.283207697489);
        Point destinationPoint = Point.fromLngLat(-72.50983506798882, 41.29909991879592);
        List<Point> list = Arrays.asList(originPoint, destinationPoint);


        RouteOptions.Builder builder = RouteOptions.builder();
        RouteOptionsExtensions.applyDefaultNavigationOptions(builder);
        RouteOptionsExtensions.applyLanguageAndVoiceUnitOptions(builder, this);


        navigation.requestRoutes(builder.coordinatesList(list).build(), new NavigationRouterCallback() {
            @Override
            public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
                Log.d("onRoutesReady", list.toString());
            }

            @Override
            public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                Log.d("onFailure", list.toString());

            }

            @Override
            public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {
                Log.d("onCanceled", list.toString());

            }
        });


    }
}