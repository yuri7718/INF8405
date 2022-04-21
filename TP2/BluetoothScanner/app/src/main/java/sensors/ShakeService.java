package sensors;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.bluetoothscanner.MainActivity;

public class ShakeService  implements SensorEventListener {
    private Context context;
    private SensorManager mSensorManager;
    private Sensor sensor;
    private static final int SHAKE_THRESHOLD = 800;
    private long lastUpdate;
    private float last_x;
    private float last_y;
    private float last_z;
    private ConnectivityManager cm;
    private BatteryManager bm;
    private SharedPreferences sharedPreferences;
    private int initialBatteryLevel;


    public ShakeService(Context context) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.context = context;
        this.cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        this.sharedPreferences
                = context.getSharedPreferences(
                "sharedPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor
                = sharedPreferences.edit();
        this.initialBatteryLevel
                = sharedPreferences
                .getInt(
                        "initialBatteryLevel", bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        editor.putInt(
                "initialBatteryLevel",  this.initialBatteryLevel);
        editor.apply();


    }


    public Sensor getSensor() {
        return sensor;
    }
    public SensorManager getSensorManager() {
        return mSensorManager;
    }

    public void register() {
        mSensorManager.registerListener(this,  sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister() {
        mSensorManager.unregisterListener(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Log.d("sensor", "shake detected w/ speed: " + speed);
                    NetworkInfo netInfo = cm.getActiveNetworkInfo();
                    if (netInfo == null){
                        Toast.makeText(this.context, "Mode avion active " + speed, Toast.LENGTH_SHORT).show();
                    } else {
                        //should check null because in airplane mode it will be null
                        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                        int downSpeed = nc.getLinkDownstreamBandwidthKbps();
                        int upSpeed = nc.getLinkUpstreamBandwidthKbps();
                        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                        Boolean isCharging = bm.isCharging();
                        Toast.makeText(this.context, "Statistiques: \n Network: \n    Uplink: " + upSpeed + "Kbs Downlink: " + downSpeed + "Kbs"
                                + "\n Batterie: \n    Niveau: " + batLevel + "%\n    Entrain de charger: " + isCharging +
                                        "\n    Consomation d'energie depuis le debut: " + (initialBatteryLevel - batLevel) + "%"
                                , Toast.LENGTH_SHORT).show();
                    }


                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
