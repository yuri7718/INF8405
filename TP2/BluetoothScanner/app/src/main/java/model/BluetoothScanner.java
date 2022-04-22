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
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import model.database.DatabaseHelper;

public class BluetoothScanner {

    private final Context context;
    private static final int REQUEST_BLUETOOTH_CONNECT = 1;
    private final BluetoothAdapter bluetoothAdapter;
    private final DatabaseHelper db;

    public BluetoothScanner(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.db = new DatabaseHelper(this.context);

        if (bluetoothAdapter == null) {
            CharSequence text = "Device doesn't support Bluetooth";
            Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();
        }
    }

    public void scan(LatLng latLng) {

        if (bluetoothAdapter != null) {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{ android.Manifest.permission.BLUETOOTH_CONNECT }, REQUEST_BLUETOOTH_CONNECT);
            }

            bluetoothAdapter.startDiscovery();
            BroadcastReceiver mReceiver = new BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                public void onReceive(Context context, Intent intent) {

                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                    // Discovery has found a device
                    // Get BluetoothDevice object and its info from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String address = device.getAddress();
                    String name = device.getName();
                    int type = device.getType();
                    double lat = latLng.latitude;
                    double lng = latLng.longitude;

                    boolean res = db.addDevice(address, name, lat, lng, type);
                    if (!res) {
                        Toast.makeText(context, "Failed to add device to database", Toast.LENGTH_SHORT).show();
                    }
                }
                }
            };
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            context.registerReceiver(mReceiver, filter);
        }
    }
}