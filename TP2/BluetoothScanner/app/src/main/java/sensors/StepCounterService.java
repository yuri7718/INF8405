package sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class StepCounterService implements SensorEventListener {
    private Context context;
    private SensorManager mSensorManager;
    private Sensor sensor;
    // Creating a variable which will give the running status
    // and initially given the boolean value as false
    private boolean running = false;

    // Creating a variable which will counts total steps
    // and it has been given the value of 0 float
    private float totalSteps = 0f;

    // Creating a variable  which counts previous total
    // steps and it has also been given the value of 0 float
    private float previousTotalSteps = 0f;


    public StepCounterService(Context context) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        this.context = context;
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


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
