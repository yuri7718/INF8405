package model.device;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.android.bluetoothscanner.R;
import java.util.List;

/**
 * Show and update list of devices
 */
public class DeviceAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final boolean isDarkMode;

    private final List<String> macAddresses;
    private final List<String> deviceNames;
    private final List<Integer> icons;
    private final List<String> types;
    private final List<String> positions;

    public DeviceAdapter(Context context, boolean isDarkMode, List<String> macAddresses, List<String> deviceNames,
                         List<Integer> icons, List<String> types, List<String> positions) {
        super(context, R.layout.single_device, R.id.device_name, deviceNames);
        this.context = context;
        this.isDarkMode = isDarkMode;
        this.macAddresses = macAddresses;
        this.deviceNames = deviceNames;
        this.icons = icons;
        this.types = types;
        this.positions = positions;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View singleDevice = convertView;
        DeviceViewHolder holder;

        if (singleDevice == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            singleDevice = layoutInflater.inflate(R.layout.single_device, parent, false);
            holder = new DeviceViewHolder(singleDevice);
            singleDevice.setTag(holder);
        } else {
            holder = (DeviceViewHolder) singleDevice.getTag();
        }

        adjustTheme(holder);

        holder.favoriteIcon.setImageResource(icons.get(position));
        holder.deviceName.setText(deviceNames.get(position));
        holder.macAddress.setText(macAddresses.get(position));

        singleDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show device info when clicked
                String deviceInfo = getDeviceInfo(position);
                Toast.makeText(context, deviceInfo, Toast.LENGTH_LONG).show();
            }
        });

        return singleDevice;
    }

    /**
     * Change text color based on theme
     */
    private void adjustTheme(DeviceViewHolder holder) {
        if (isDarkMode) {
            holder.deviceName.setTextColor(Color.WHITE);
            holder.macAddress.setTextColor(Color.WHITE);
        } else {
            holder.deviceName.setTextColor(Color.BLACK);
            holder.macAddress.setTextColor(Color.BLACK);
        }
    }

    /**
     * Create and return device information
     */
    private String getDeviceInfo(int position) {
        String deviceName = this.deviceNames.get(position);
        String macAddress = this.macAddresses.get(position);
        String type = this.types.get(position);
        String coordinates = this.positions.get(position);

        return String.format("%s\n%s\n%s\n%s", deviceName, macAddress, type, coordinates);
    }

}
