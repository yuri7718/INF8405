package sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.bluetoothscanner.R;

public class StepCounterService implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor sensor;
    TextView stepCounterSteps;
    private float steps = 0f;

    public StepCounterService(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        Activity activity = (Activity) context;
        stepCounterSteps = activity.findViewById(R.id.step_count);
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
        Log.i("test-sensor", String.valueOf(sensorEvent.sensor.getType()));
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            Log.i("test-sensor", Float.toString(steps));
            steps++;
            stepCounterSteps.setText(Float.toString(steps));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void resetSteps() {
        this.steps = 0f;
        stepCounterSteps.setText(Float.toString(steps));
    }
}
