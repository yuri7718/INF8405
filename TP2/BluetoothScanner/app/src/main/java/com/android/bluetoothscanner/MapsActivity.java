package com.android.bluetoothscanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import model.BluetoothScanner;
import model.DbController;
import model.GoogleParser;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationListener locationListener;
    private LocationManager locationManager;

    private final long MIN_TIME = 1000; // 1 second
    private final long MIN_DIST = 5; // 5 Meters

    private Marker myPositionMarker;
    private JSONObject devicesMarkers;
    private JSONObject allDevices;
    private BluetoothScanner bluetoothScanner;
     private DbController dbController;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
// Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bluetoothScanner = new BluetoothScanner(this);
        dbController = new DbController(this);
        devicesMarkers = new JSONObject();
        allDevices = new JSONObject();
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
// Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        JSONObject newDevices = dbController.getDevicesLocations();
        pinDevicesToMap(newDevices);
        
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                try {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (myPositionMarker != null){
                        myPositionMarker.remove();
                    }

                    myPositionMarker = addMarker("My Position", latLng, R.drawable.ic_standing_up_man_svgrepo_com);






                    bluetoothScanner.scan(latLng);
                    pinDevicesToMap(bluetoothScanner.getDetectedDevices());
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            }
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }

    public void pinDevicesToMap(JSONObject devices) {
        Iterator<String> keys = devices.keys();

        while(keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject obj = (JSONObject) devices.get(key);
                //String address = (String) obj.get("address");
                String title = null;
                if (devicesMarkers.has(key)){
                    Marker oldMarker = (Marker) devicesMarkers.get(key);
                    title = oldMarker.getTitle();
                    //remove marker from map
                    oldMarker.remove();
                    //remove marker from list of markers
                    devicesMarkers.remove(key);
                    allDevices.remove(key);
                } else{
                    title = "Device" + Integer.toString( devicesMarkers.length()+1);
                }
                LatLng latLng = null;
                if(obj.get("latLng") instanceof String){
                    String[] strArray = ((String) obj.get("latLng")).split(" ");
                    strArray[1] = (strArray[1]).substring(1,strArray[1].length()-1);
                    String[] latlong = strArray[1].split(",");
                    double latitude = Double.parseDouble(latlong[0]);
                    double longitude = Double.parseDouble(latlong[1]);
                    latLng = new LatLng(latitude, longitude);
                } else {
                    latLng =(LatLng) obj.get("latLng");
                }
                Marker marker = addMarker(title, latLng, R.drawable.ic_pushpin_svgrepo_com);
                //marker.setTag(obj.get("address"));
                devicesMarkers.put(key, marker);
                allDevices.put(key, obj);
                dbController.setDevicesLocations(allDevices);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public Marker addMarker(String title, LatLng latLng, int icon){
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(title).icon(BitmapFromVector(getApplicationContext(), icon)));
        if (title.equals("My Position")){
            myPositionMarker = marker;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
            LatLng newLatlng = new LatLng(45.4953933,-73.5727794);
            Marker newMarker = mMap.addMarker(new MarkerOptions().position(newLatlng).title(title).icon(BitmapFromVector(getApplicationContext(), icon)));
            getDirections(newMarker);
        }
        return marker;
    }

    private String getDirectionsUrl( LatLng origin,  LatLng dest) {
        // Origin of route
        String ow = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String dw = "destination=" + dest.latitude + "," + dest.longitude;
        //setting transportation mode
        String mode = "mode=walking";
        // Building the parameters to the web service
        String parameters = ow + "&"+ dw +"&"+ mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&key=AIzaSyAG3jQ47ggDKOyTfyH_mawEROmzYBAbvt8";
    }

    private void getDirections(Marker marker){
        new GoogleParser(this).execute(getDirectionsUrl(myPositionMarker.getPosition(), marker.getPosition()));
    }

}
