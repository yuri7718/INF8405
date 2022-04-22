package sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import com.android.bluetoothscanner.R;

public class StepCounterService implements SensorEventListener {

    private final SensorManager mSensorManager;
    private final Sensor sensor;
    private final TextView stepCounterSteps;
    private int steps = 0;

    public StepCounterService(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        stepCounterSteps = ((Activity) context).findViewById(R.id.step_count);
    }

    public void register() {
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            this.steps++;
            stepCounterSteps.setText(String.valueOf(this.steps));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    public void resetSteps() {
        this.steps = 0;
        stepCounterSteps.setText(String.valueOf(this.steps));
    }
}
