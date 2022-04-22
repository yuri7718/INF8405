package com.android.bluetoothscanner;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import model.BluetoothScanner;
import model.database.DatabaseHelper;
import model.device.DeviceAdapter;
import model.GPSTracker;
import model.GoogleDirections;
import sensors.ShakeService;
import sensors.StepCounterService;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private GPSTracker gpsTracker;
    private Location mLocation;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private static final long MIN_TIME = 1000; // 1s
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

    private static String ADD_TO_FAVORITES = "Add to favourites";
    private static String REMOVE_FROM_FAVORITES = "Remove from favourites";

    private GoogleDirections mGoogleDirections;

    private Button shareBtn;
    private Button swapTheme;
    private boolean isDarkMode;
    private ShakeService mShakeService;
    private StepCounterService mStepCounterService;
    private SharedPreferences sharedPreferences;


    ImageView profilePictureV1;
    ImageView profilePictureV2;
    TextView usernameV1;
    TextView usernameV2;
    Button changeUsernameBtn;
    private String usernameInput = "";

    TextView stepCounterSteps;
    Button stepCounterStartBtn;
    Button stepCounterStopBtn;
    Button stepCounterResetBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Dark Mode Activation
        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        this.isDarkMode
                = sharedPreferences
                .getBoolean(
                        "isDarkModeOn", false);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get current location
        gpsTracker = new GPSTracker(getApplicationContext());
        mLocation = gpsTracker.getLocation();
        mShakeService = new ShakeService(this);


        // define deviceListView and adapter
        deviceListView = (ListView) findViewById(R.id.device_list);
        adapter = new DeviceAdapter(this, isDarkMode, deviceListMacAddresses, deviceListNames, deviceListIcons, deviceListTypes, deviceListPositions);
        deviceListView.setAdapter(adapter);


        // initialize database
        db = new DatabaseHelper(this);

        bluetoothScanner = new BluetoothScanner(this);
        if (mLocation == null){
            mLocation = gpsTracker.getLocation();
        }
        if( mLocation != null){
            bluetoothScanner.scan(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        }

        mGoogleDirections = null;

        shareBtn = findViewById(R.id.share);
        Button languageBtn = findViewById(R.id.language);
        swapTheme = findViewById(R.id.swap_theme);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share();
            }
        });
        //Swap Theme config

        swapTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Saving state of our app
                // using SharedPreferences
                SharedPreferences sharedPreferences
                        = getSharedPreferences(
                        "sharedPrefs", MODE_PRIVATE);
                final SharedPreferences.Editor editor
                        = sharedPreferences.edit();
                final boolean isDarkModeOn
                        = sharedPreferences
                        .getBoolean(
                                "isDarkModeOn", false);
                if(isDarkModeOn){
                    editor.putBoolean(
                            "isDarkModeOn", false);
                    editor.apply();
                    AppCompatDelegate
                            .setDefaultNightMode(
                                    AppCompatDelegate
                                            .MODE_NIGHT_NO);
                } else {
                    editor.putBoolean(
                            "isDarkModeOn", true);
                    editor.apply();
                    AppCompatDelegate
                            .setDefaultNightMode(
                                    AppCompatDelegate
                                            .MODE_NIGHT_YES);
                }
            }
        });

        //change language
        if (sharedPreferences.getString("language", "en").equals("fr")){
            ADD_TO_FAVORITES = "Ajouter aux favoris";
            REMOVE_FROM_FAVORITES = "Retirer des favoris";
        }

        // setting up on click listener event over the button
        // in order to change the language with the help of
        // LocaleHelper class
        languageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String languageCode = "en";
                if (languageBtn.getText().equals("Fr")){
                    languageCode = "fr";
                }
                updateLanguage(languageCode);
            }
        });
        initializeStepCounter();
        initializeUserProfile();

    }

    private void initializeStepCounter() {
        stepCounterStartBtn = findViewById(R.id.step_counter_start);
        stepCounterStopBtn = findViewById(R.id.step_counter_stop);
        stepCounterResetBtn = findViewById(R.id.step_counter_reset);

        mStepCounterService = new StepCounterService(this);


        stepCounterSteps = findViewById(R.id.step_count);

        stepCounterStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStepCounterService.register();
            }
        });

        stepCounterStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStepCounterService.unregister();
            }
        });

        stepCounterResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStepCounterService.resetSteps();
            }
        });
    }

    private void updateLanguage(String languageCode) {
        final SharedPreferences.Editor editor
                = sharedPreferences.edit();
        editor.putString(
                "language", languageCode);
        editor.apply();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //LocaleManager.setLocale(this);
    }

    @Override
    protected void onResume() {
        mShakeService.register();
        super.onResume();
    }
    @Override
    protected void onPause() {
        mShakeService.unregister();
        super.onPause();
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
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        final Handler hanlder = new Handler();
        hanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDevicesList();
            }
        }, MIN_TIME);


        if (mLocation == null){
            mLocation = gpsTracker.getLocation();
        }
        if( mLocation != null) {
            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

            if(myPositionMarker!= null){
                myPositionMarker.remove();
            }
            myPositionMarker = addMarker(MY_POSITION, latLng, R.drawable.ic_standing_up_man_svgrepo_com);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
        }
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateDevicesList();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if(myPositionMarker!= null){
                    myPositionMarker.remove();
                }
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
        //some devices using WIFI don't support GPS
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                
                if (marker.getTitle().equals(MY_POSITION)) {

                    popupView = getLayoutInflater().inflate(R.layout.user_info_window, null);
                    ViewFlipper markerInfoContainer = (ViewFlipper) popupView.findViewById(R.id.markerInfoContainer);
                    View viewContainer = getLayoutInflater().inflate(R.layout.user_info_layout, null);

                    String username = sharedPreferences.getString("username", "USERNAME");
                    Log.i("test-profile", username);
                    usernameV2 = viewContainer.findViewById(R.id.username_v2);
                    usernameV2.setText(username);

                    profilePictureV2 = viewContainer.findViewById(R.id.profile_pic_v2);
                    loadProfilePictureV2();

                    markerInfoContainer.addView(viewContainer);

                    // adjust the window position
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

                    updatePopup();

                    return true;
                }

                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }

                //The popup window of the marker ("comment y arriver" and "Favoris")
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


                // adjust the window position
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

                updatePopup();


                //directions
                directions_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        getDirections(marker);
                    }
                });

                //favourites
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

    private void initializeUserProfile() {
        profilePictureV1 = findViewById(R.id.profile_pic_v1);


        profilePictureV1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { pickImage(); }
        });
        loadProfilePictureV1();
        usernameV1 = findViewById(R.id.username_v1);


        String username = sharedPreferences.getString("username", "USERNAME");
        usernameV1.setText(username);


        changeUsernameBtn = findViewById(R.id.change_username);
        changeUsernameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUsernameInputPopup();
            }
        });
    }

    private void createUsernameInputPopup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter your username");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                usernameInput = input.getText().toString();
                usernameV1.setText(usernameInput);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", usernameInput);
                editor.apply();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void pickImage() {
        CropImage.activity().start(this);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    InputStream stream = getContentResolver().openInputStream(resultUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
                    profilePictureV1.setImageBitmap(bitmap);
                    profilePictureV2.setImageBitmap(bitmap);

                    // save picture
                    String directory = saveProfilePicture(bitmap);
                    Log.i("test-picture", directory);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private String saveProfilePicture(Bitmap bitmapImg) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("profile", Context.MODE_PRIVATE);
        File path = new File(directory, "profile.png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            bitmapImg.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private boolean loadProfilePictureV1() {
        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("profile", Context.MODE_PRIVATE);
            String path = directory.getAbsolutePath();
            File file = new File(path, "profile.png");
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                profilePictureV1.setImageBitmap(bitmap);
            } else {
                profilePictureV1.setImageResource(R.drawable.default_profile_pic);
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadProfilePictureV2() {
        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("profile", Context.MODE_PRIVATE);
            String path = directory.getAbsolutePath();
            File file = new File(path, "profile.png");
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                profilePictureV2.setImageBitmap(bitmap);
            } else {
                profilePictureV2.setImageResource(R.drawable.default_profile_pic);
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     *
     */
    private void updateDevicesList() {

        Cursor cursor = db.readAllData();
        //iterate in the devices list


        while (cursor.moveToNext()) {
            String addr = cursor.getString(0);
            String name = cursor.getString(1);
            double lat = cursor.getDouble(2);
            double lng = cursor.getDouble(3);
            int type = cursor.getInt(4);
            int favorite = cursor.getInt(5);

            String pos = lat + " " + lng;
            if (indices.containsKey(addr)) {
                int i = indices.get(addr);
                deviceListPositions.set(i, pos);
                if (favorite == 0) {
                    deviceListIcons.set(i,R.drawable.white_heart);
                } else {
                    deviceListIcons.set(i,R.drawable.red_heart);
                }
            } else {
                // put <key, value> pair which is mac address and its index in the list
                indices.put(addr, deviceListMacAddresses.size());

                deviceListMacAddresses.add(addr);

                name = name == null ? "Device" + deviceListMacAddresses.size() : name;
                deviceListNames.add(name);

                String deviceType = "type" + type;
                deviceListTypes.add(deviceType);
                deviceListPositions.add(pos);
                if (favorite == 0) {
                    deviceListIcons.add(R.drawable.white_heart);
                } else {
                    deviceListIcons.add(R.drawable.red_heart);
                }
            }

        }
        adapter.notifyDataSetChanged();
        //add markers to the map
        pinDevicesToMap();
    }


    private boolean addRemoveFavourites(Button button, Marker marker) {
        int i = indices.get(marker.getTitle());
        if (deviceListIcons.get(i) == R.drawable.red_heart) {
            deviceListIcons.set(i, R.drawable.white_heart);
        } else {
            deviceListIcons.set(i, R.drawable.red_heart);
        }
        adapter.notifyDataSetChanged();
        return db.toggleFavorite(marker.getTitle());
    }

    /**
     * Plot detected devices
     * myPositionMarker needs to be set manually after calling this method
     */
    public void pinDevicesToMap() {

        mMap.clear();   // remove all the pins
        if (myPositionMarker != null){
            myPositionMarker = addMarker(MY_POSITION, myPositionMarker.getPosition(), R.drawable.ic_standing_up_man_svgrepo_com);
        }

        // read data from database
        Cursor cursor = db.readAllData();
        while (cursor.moveToNext()) {
            String addr = cursor.getString(0);

            // latLng
            double lat = cursor.getDouble(2);
            double lng = cursor.getDouble(3);

            LatLng latLng = addSpaceBetweenDetectedDevices(lat, lng);
            if (isDarkMode){
                addMarker(addr, latLng, R.drawable.ic_pushpin_svgrepo_com_dark);
            } else {
                addMarker(addr, latLng, R.drawable.ic_pushpin_svgrepo_com);
            }
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
