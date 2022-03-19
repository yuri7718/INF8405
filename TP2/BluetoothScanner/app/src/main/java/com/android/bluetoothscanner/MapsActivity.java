package com.android.bluetoothscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.cardemulation.HostNfcFService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import com.google.android.gms.common.util.ArrayUtils;
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

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import model.BluetoothScanner;
import model.DatabaseHelper;
import model.DbController;
import model.DeviceAdapter;
import model.DeviceViewHolder;
import model.GPSTracker;
import model.GoogleDirections;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private GPSTracker gpsTracker;
    private Location mLocation;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private static final long MIN_TIME = 5000; // 5s
    private static final long MIN_DIST = 10;   // 10m

    private BluetoothScanner bluetoothScanner;
    ListView deviceListView;
    List<String> deviceListMacAddresses = new ArrayList<>();
    List<String> deviceListNames = new ArrayList<>();
    List<Integer> deviceListIcons = new ArrayList<>();
    List<String> deviceListTypes = new ArrayList<>();
    List<String> deviceListPositions = new ArrayList<>();
    Map<String, Integer> indices = new HashMap<>();
    DeviceAdapter adapter;

    DatabaseHelper db;
    Map<String, Map> deviceList = new HashMap<>();

    private static final String MY_POSITION = "My Position";
    private Marker myPositionMarker;

    // popup materials
    private Marker mMarker;
    private PopupWindow mPopupWindow;
    private int mWidth;
    private int mHeight;
    private View popupView;

    private static final String ADD_TO_FAVORITES = "Ajouter aux favoris";
    private static final String REMOVE_FROM_FAVORITES = "Retirer des favoris";

    private GoogleDirections mGoogleDirections;

    private Button shareBtn;
    private Button swapTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get current location
        gpsTracker = new GPSTracker(getApplicationContext());
        mLocation = gpsTracker.getLocation();

        // define deviceListView and adapter
        deviceListView = (ListView) findViewById(R.id.device_list);
        adapter = new DeviceAdapter(this, deviceListMacAddresses, deviceListNames, deviceListIcons, deviceListTypes, deviceListPositions);
        deviceListView.setAdapter(adapter);

        // initialize database
        db = new DatabaseHelper(this);

        bluetoothScanner = new BluetoothScanner(this);
        bluetoothScanner.scan(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));

        mGoogleDirections = null;

        shareBtn = findViewById(R.id.share);
        swapTheme = findViewById(R.id.swap_theme);

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share();
            }
        });

        swapTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        final Handler hanlder = new Handler();
        hanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDevicesList();
            }
        }, MIN_TIME);


        LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        myPositionMarker = addMarker(MY_POSITION, latLng, R.drawable.ic_standing_up_man_svgrepo_com);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateDevicesList();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                myPositionMarker = addMarker(MY_POSITION, latLng, R.drawable.ic_standing_up_man_svgrepo_com);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                
                if (marker.getTitle().equals(MY_POSITION)) return false;

                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }

                popupView = getLayoutInflater().inflate(R.layout.default_marker_info_window, null);
                ViewFlipper markerInfoContainer = (ViewFlipper) popupView.findViewById(R.id.markerInfoContainer);
                View viewContainer = getLayoutInflater().inflate(R.layout.default_marker_info_layout, null);
                TextView tvTitulo = (TextView) viewContainer.findViewById(R.id.tvTitulo);
                TextView tvCuerpo = (TextView) viewContainer.findViewById(R.id.tvCuerpo);
                Button directions_button = (Button) viewContainer.findViewById(R.id.direction_buttons);
                Button favourites_button = (Button) viewContainer.findViewById(R.id.favourites_button);


                if (getFavorites().contains(marker.getTitle())){
                    favourites_button.setText(REMOVE_FROM_FAVORITES);
                } else {
                    favourites_button.setText(ADD_TO_FAVORITES);
                }


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

                //PopupWindow popupWindow = new PopupWindow(mainView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //popupWindow.showAtLocation(findViewById(R.id.map), Gravity.CENTER_HORIZONTAL, 0, 0); //map is the fragment on the activity layout where I put the map

                updatePopup();


                directions_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        getDirections(marker);
                    }
                });

                favourites_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        boolean res = addRemoveFavourites(favourites_button, marker);
                        if (res){
                            favourites_button.setText(REMOVE_FROM_FAVORITES);
                        } else {
                            favourites_button.setText(ADD_TO_FAVORITES);
                        }
                    }
                });

                return true;
            }
        });
    }

    /**
     *
     */
    private void updateDevicesList() {

        // get devices from database
        Cursor cursor = db.readAllData();

        while (cursor.moveToNext()) {
            String addr = cursor.getString(0);
            String name = cursor.getString(1);
            double lat = cursor.getDouble(2);
            double lng = cursor.getDouble(3);
            int type = cursor.getInt(4);
            int favorite = cursor.getInt(5);

            String pos = Double.toString(lat) + " " + Double.toString(lng);
            if (indices.containsKey(addr)) {
                int i = indices.get(addr);
                deviceListPositions.set(i, pos);
            } else {
                // put <key, value> pair which is mac address and its index in the list
                indices.put(addr, deviceListMacAddresses.size());

                deviceListMacAddresses.add(addr);

                name = name == null ? "Device" + deviceListMacAddresses.size() : name;
                deviceListNames.add(name);

                if (favorite == 0) {
                    deviceListIcons.add(R.drawable.heart_grey);
                } else {
                    deviceListIcons.add(R.drawable.heart);
                }

                String deviceType = "type" + type;
                deviceListTypes.add(deviceType);
                deviceListPositions.add(pos);
            }
        }
        adapter.notifyDataSetChanged();
        pinDevicesToMap();
    }



    private boolean addRemoveFavourites(Button button, Marker marker) {
        int i = indices.get(marker.getTitle());
        if (deviceListIcons.get(i) == R.drawable.heart) {
            deviceListIcons.set(i, R.drawable.heart_grey);
        } else {
            deviceListIcons.set(i, R.drawable.heart);
        }
        adapter.notifyDataSetChanged();
        return db.updateFavorite(marker.getTitle());
    }

    /**
     * Plot detected devices
     * myPositionMarker needs to be set manually after calling this method
     */
    public void pinDevicesToMap() {

        mMap.clear();   // remove all the pins

        // read data from database
        Cursor cursor = db.readAllData();
        while (cursor.moveToNext()) {
            String addr = cursor.getString(0);

            // latLng
            double lat = cursor.getDouble(2);
            double lng = cursor.getDouble(3);

            LatLng latLng = addSpaceBetweenDetectedDevices(lat, lng);
            addMarker(addr, latLng, R.drawable.ic_pushpin_svgrepo_com);
        }

    }

    /**
     * Add space between pins since all the detected devices have the same lat and lng
     */
    private LatLng addSpaceBetweenDetectedDevices(double lat, double lng) {
        double n;
        double divisor = 100000.0;

        // randomly generate a small number
        Random rand = new Random();
        n = rand.nextInt(10) / divisor;
        lat = Math.random() < 0.5 ? lat - n : lat + n;

        n = rand.nextInt(10) / divisor;
        lng = Math.random() < 0.5 ? lng - n : lng + n;

        return new LatLng(lat, lng);
    }

    /**
     * Return mac addresses of all favorites devices
     */
    private List<String> getFavorites() {
        List<String> favorites = new ArrayList<>();

        Cursor cursor = db.readFavorites();
        while (cursor.moveToNext()) {
            favorites.add(cursor.getString(0));
        }

        return favorites;
    }

    /**
     *  Share all information stored in database
     */
    private void share() {
        String msg = "";
        Cursor cursor = db.readAllData();
        while (cursor.moveToNext()) {
            String addr = "MAC ADDRESS: " + cursor.getString(0) + "\n";
            msg += addr;
            String name = "DEVICE NAME: " + cursor.getString(1) + "\n";
            msg += name;
            String lat = "LAT: " + cursor.getDouble(2) + "\n";
            msg += lat;
            String lng = "LNG: " + cursor.getDouble(3) + "\n";
            msg += lng;
            String type = "TYPE: type" + cursor.getInt(4) + "\n";
            msg += type;
            String favorite = cursor.getInt(5) == 0 ? "false" : "true";
            msg += "FAVORITE: " + favorite + "\n";
            msg += "=============================\n";
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
        sendIntent.setType("text/plain");

        Intent sharedIntent = Intent.createChooser(sendIntent, null);
        startActivity(sharedIntent);
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
        /*
        if (title.equals("My Position")){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
        }
        */
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
