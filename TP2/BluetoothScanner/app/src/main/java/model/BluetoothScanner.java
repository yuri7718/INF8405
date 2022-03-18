package model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.bluetoothscanner.MapsActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class BluetoothScanner {

    private final Context context;
    private static final int REQUEST_BLUETOOTH_CONNECT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private DatabaseHelper db;
    private final JSONObject detectedDevices;

    public BluetoothScanner(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.detectedDevices = new JSONObject();
        this.db = new DatabaseHelper(this.context);
    }

    public void scan(LatLng latLng) {

        if (bluetoothAdapter == null) {
            CharSequence text = "Device doesn't support Bluetooth";
            Toast.makeText(this.context, text, Toast.LENGTH_LONG).show();

        } else {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT);
            }

            bluetoothAdapter.startDiscovery();
            BroadcastReceiver mReceiver = new BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                public void onReceive(Context context, Intent intent) {

                    if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                        // Discovery has found a device
                        // Get BluetoothDevice object and its info from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


                        // Add the name and address to an array adapter to show in a ListView
                        Log.i("testing", "name:" + device.getName() + " addr:" + device.getAddress()
                            + " class:" + device.getClass() + " type" + device.getType());
                        JSONObject deviceInfo = new JSONObject();
                        try {
                            deviceInfo.put("name", device.getName());
                            deviceInfo.put("class", device.getClass());
                            deviceInfo.put("type", device.getType());
                            deviceInfo.put("latLng", latLng);
                            detectedDevices.put((String)(device.getAddress()),deviceInfo);
                            //((MapsActivity)context).pinDevicesToMap(detectedDevices1);

                            String address = device.getAddress();
                            String name = device.getName();
                            int type = device.getType();
                            double lat = latLng.latitude;
                            double lng = latLng.longitude;

                            db.addDevice(address, name, lat, lng, type);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            context.registerReceiver(mReceiver, filter);
        }
    }

    public JSONObject getDetectedDevices() {
        return detectedDevices;
    }
}