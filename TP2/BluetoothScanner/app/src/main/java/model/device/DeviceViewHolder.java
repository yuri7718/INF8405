package model.device;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.bluetoothscanner.R;

/**
 * Class for storing information about each detected device
 */
public class DeviceViewHolder {
    ImageView favoriteIcon;
    TextView deviceName;
    TextView macAddress;

    DeviceViewHolder(View v) {
        favoriteIcon = v.findViewById(R.id.favorite_icon);
        deviceName = v.findViewById(R.id.device_name);
        macAddress = v.findViewById(R.id.mac_address);
    }
}
