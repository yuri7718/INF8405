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

public class BluetoothScanner {
    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_BLUETOOTH_CONNECT = 1;
    private final JSONObject detectedDevices;

    public BluetoothScanner(Context context) {
        this.context = context;
        this.detectedDevices = new JSONObject();
    }

    public void scan( LatLng latLng) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            CharSequence text = "Device doesn't support Bluetooth";
            Toast.makeText(this.context, text, Toast.LENGTH_LONG).show();
        } else {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT);
            }
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);


            /*
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                }
            }
            */

            bluetoothAdapter.startDiscovery();
            BroadcastReceiver mReceiver = new BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    //Finding devices
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Get the BluetoothDevice object from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        // Add the name and address to an array adapter to show in a ListView
                        Log.i("device-test-broadcast", device.getName() + " " + device.getAddress());
                        JSONObject deviceInfo = new JSONObject();
                        try {
                            deviceInfo.put("name", device.getName());
                            deviceInfo.put("class", device.getClass());
                            deviceInfo.put("type", device.getType());
                            deviceInfo.put("latLng", latLng);
                            detectedDevices.put((String)(device.getAddress()),deviceInfo);
                            ((MapsActivity)context).pinDevicesToMap(detectedDevices);
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

    /*private void askForPermissions(){

    }*/

    public JSONObject getDetectedDevices() {
        return detectedDevices;
    }
}