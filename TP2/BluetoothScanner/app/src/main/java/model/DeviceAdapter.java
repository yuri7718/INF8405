package model;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.bluetoothscanner.R;

import java.util.List;

public class DeviceAdapter extends ArrayAdapter<String> {
    Context context;

    List<String> macAddresses;
    List<String> deviceNames;
    List<Integer> icons;
    List<String> types;
    List<String> positions;

    public DeviceAdapter(Context context, List<String> macAddresses, List<String> deviceNames, List<Integer> icons,
                         List<String> types, List<String> positions) {
        super(context, R.layout.single_device, R.id.device_name, deviceNames);
        this.context = context;

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
        DeviceViewHolder holder = null;

        if (singleDevice == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            singleDevice = layoutInflater.inflate(R.layout.single_device, parent, false);
            holder = new DeviceViewHolder(singleDevice);
            singleDevice.setTag(holder);
        } else {
            holder = (DeviceViewHolder) singleDevice.getTag();
        }
        holder.favoriteIcon.setImageResource(icons.get(position));
        holder.deviceName.setText(deviceNames.get(position));
        holder.macAddress.setText(macAddresses.get(position));

        singleDevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String deviceInfo =
                        deviceNames.get(position) + "\n" +
                        macAddresses.get(position) + "\n" +
                        types.get(position) + "\n" +
                        positions.get(position);
                Toast toast = Toast.makeText(context, deviceInfo, Toast.LENGTH_LONG);
                toast.show();
                Log.i("test", "LIST ITEM CLICKED");
            }
        });

        return singleDevice;
    }


}