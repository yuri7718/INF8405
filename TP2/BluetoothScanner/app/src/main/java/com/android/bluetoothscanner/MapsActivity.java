package com.android.bluetoothscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import model.BluetoothScanner;
import model.DbController;
import model.GoogleDirections;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button darkModeToggle;


    private LocationListener locationListener;
    private LocationManager locationManager;

    private final long MIN_TIME = 1000; // 1 second
    private final long MIN_DIST = 5; // 5 Meters

    private Marker myPositionMarker;
    private JSONObject devicesMarkers;
    private JSONObject allDevices;
    private BluetoothScanner bluetoothScanner;
    private DbController dbController;

    //popup materials
    private Marker mMarker;
    private PopupWindow mPopupWindow;
    private int mWidth;
    private int mHeight;
    private View popupView;
    private GoogleDirections mGoogleDirections;


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
        mGoogleDirections = null;
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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                
                if(marker.getTitle().equals("My Position")){
                    return false;
                }

                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }

                //popupView = getLayoutInflater().inflate(R.layout.default_marker_info_window, null);;// inflate our view here

                popupView = getLayoutInflater().inflate(R.layout.default_marker_info_window, null);
                ViewFlipper markerInfoContainer = (ViewFlipper)popupView.findViewById(R.id.markerInfoContainer);
                View viewContainer = getLayoutInflater().inflate(R.layout.default_marker_info_layout, null);
                TextView tvTitulo = (TextView) viewContainer.findViewById(R.id.tvTitulo);
                TextView tvCuerpo = (TextView) viewContainer.findViewById(R.id.tvCuerpo);
                Button directions_button = (Button) viewContainer.findViewById(R.id.direction_buttons);
                Button favourites_button = (Button) viewContainer.findViewById(R.id.favourites_button);

                if (dbController.getFavourites().has((String) marker.getTag())){
                    favourites_button.setText("Retirer des favoris");
                } else {
                    favourites_button.setText("Ajouter aux favoris");
                }



                Button share_button = (Button) viewContainer.findViewById(R.id.share_button);
                tvTitulo.setText(marker.getTitle());
                tvCuerpo.setVisibility(View.GONE);

                markerInfoContainer.addView(viewContainer);



                mPopupWindow= new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
                mPopupWindow.setOutsideTouchable(true);
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                popupView.measure(size.x, size.y);


                mWidth = popupView.getMeasuredWidth();
                mHeight = popupView.getMeasuredHeight();
                mMarker = marker;
/*
                PopupWindow popupWindow = new PopupWindow(mainView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.showAtLocation(findViewById(R.id.map), Gravity.CENTER_HORIZONTAL, 0, 0); //map is the fragment on the activity layout where I put the map
*/
                updatePopup();


                directions_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        getDirections(marker);
                    }
                });

                share_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        share(marker);
                    }
                });

                favourites_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        addRemoveFavourites(favourites_button, marker);
                    }
                });

                return true;
            }
        });





        darkModeToggle  = findViewById(R.id.darkModeToggle);

        // managing app reopening case if the user applied dark mode before closure

        SharedPreferences sharedPreferences = getSharedPreferences( "sharedPreferences", MODE_PRIVATE);
        final SharedPreferences.Editor modeStatus = sharedPreferences.edit();
        final boolean darkMode = sharedPreferences.getBoolean("darkMode", false);

        if(darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            darkModeToggle.setText("Enable Normal Mode");
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            darkModeToggle.setText("Enable Dark Mode");

        }

        darkModeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (darkMode) {
                    // turn off dark mode and enable normal one
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    // change darkMode verification boolean to false
                    modeStatus.putBoolean("darkMode", false);
                    modeStatus.apply();
                    //change toggle button text
                    darkModeToggle.setText("Enable Dark Mode");
                }
                else {
                    // turn off normal mode and enable dark one
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    // change darkMode verification boolean
                    modeStatus.putBoolean("darkMode", true);
                    modeStatus.apply();
                    //change toggle button text
                    darkModeToggle.setText("Enable Normal Mode");
                }
            }
        });
    }

    //TODO
    private void share(Marker marker) {
    }

    //TODO
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addRemoveFavourites(Button button,Marker marker) {
        JSONObject arr = dbController.getFavourites();
        if (arr.has((String)marker.getTag())){
            arr.remove((String)marker.getTag());
            button.setText("Ajouter aux favoris");
        } else {
            try{
                arr.put((String)marker.getTag(),true);
                button.setText("Retirer des favoris");
            } catch (Exception e){
                Log.e("Favourites", e.getMessage());
            }

        }
        dbController.setFavourites(arr);
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
                marker.setTag(key);
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
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
        if (mGoogleDirections != null){
            mGoogleDirections.reset();
        }
        mGoogleDirections = new GoogleDirections(this, mMap, myPositionMarker.getPosition(), marker.getPosition());
        mGoogleDirections.execute(getDirectionsUrl(myPositionMarker.getPosition(), marker.getPosition()));
    }

    private void updatePopup() {
        if (mMarker != null && mPopupWindow != null) {
            // marker is visible
            if (mMap.getProjection().getVisibleRegion().latLngBounds.contains(mMarker.getPosition())) {
                if (!mPopupWindow.isShowing()) {
                    mPopupWindow.showAtLocation(popupView, Gravity.NO_GRAVITY, 0, 0);
                }
                Point p = mMap.getProjection().toScreenLocation(mMarker.getPosition());
                mPopupWindow.update(p.x - mWidth / 2, p.y - mHeight + 100, -1, -1);
            } else { // marker outside screen
                mPopupWindow.dismiss();
            }
        }
    }



}
