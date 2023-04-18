package com.example.lize_app.adapter;

import com.example.lize_app.R;
import com.example.lize_app.data.BLEDataServer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class LeDeviceAdapter extends RecyclerView.Adapter<LeDeviceAdapter.DeviceViewHolder> {

    private final List<BluetoothDevice> mLeDevices = new ArrayList<>();
    private final Map<BluetoothDevice, BLEDataServer.BLEData> mBLEDataMap = new HashMap<>();

    private DeviceItemClickListener mListener;

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_device, parent, false);

        DeviceViewHolder viewHolder = new DeviceViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final DeviceViewHolder holder, final int position) {
        BluetoothDevice bt = mLeDevices.get(position);
        BLEDataServer.BLEData data = mBLEDataMap.get(bt);
        // Log.d("LeDeviceAdapter", "position(" + position + "), " + bt.toString());

        holder.deviceName.setText(holder.resources.getString(R.string.DeviceName) + ": " + bt.getName());
        holder.deviceAddress.setText(holder.resources.getString(R.string.DeviceAddress) + ": " + bt.getAddress());

        // Log.e("LeDeviceAdapter", bt.getName() + ": connectedState: " + String.valueOf(data.connectedState));

        if (data != null) {

            if (data.connectedState == BluetoothProfile.STATE_CONNECTED) {
                holder.deviceCard.setBackgroundResource(R.color.Connected_Card);
                holder.deviceState.setText(holder.resources.getString(R.string.State) + ": " + holder.resources.getString(R.string.Connected));
            } else {
                holder.deviceCard.setBackgroundResource(R.color.Disconnected_Card);
                holder.deviceState.setText(holder.resources.getString(R.string.State) + ": " + holder.resources.getString(R.string.Disconnected));
            }

            holder.deviceRSSI.setText(holder.resources.getString(R.string.State) + ": " + data.rssi);
        }

        if (data == null || data.connectedState == BluetoothProfile.STATE_DISCONNECTED) {
            holder.Connect_Button.setText(R.string.listitem_device_Btn_Connect);
            holder.connect_button_state = true;
        } else {
            holder.Connect_Button.setText(R.string.listitem_device_Btn_Disconnect);
            holder.connect_button_state = false;
        }

        if (data == null || data.device.getBondState() == BluetoothDevice.BOND_NONE) {
//            holder.Pair_Button.setText(R.string.listitem_device_Btn_Pair);
            holder.Pair_Button.setVisibility(View.VISIBLE);
            holder.pair_button_state = true;
        } else {
//            holder.Pair_Button.setText(R.string.listitem_device_Btn_Unpair);
            holder.Pair_Button.setVisibility(View.GONE);
            holder.pair_button_state = false;
        }

        holder.deviceCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null && mLeDevices.size() > position) {
                    mListener.onItemClicked(mLeDevices.get(position), position);
                }
            }
        });

        // connectGATT
        holder.Connect_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null && mLeDevices.size() > position) {
//                    holder.change_connect_button();
                    mListener.onItemConnectionButtonClicked(mLeDevices.get(position), position, holder.connect_button_state);
                }
            }
        });

        // pair
        holder.Pair_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null && mLeDevices.size() > position) {
                    mListener.onItemPairButtonClicked(mBLEDataMap.get(mLeDevices.get(position)), position, holder.pair_button_state);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLeDevices.size();
    }

    public BluetoothDevice getBtDeviceAtIndex(int index) {
        if (index < mLeDevices.size()) {
            return mLeDevices.get(index);
        }

        return null;
    }

    public void addDevice(BluetoothDevice device) {
        if (device != null && !mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }

    }

    public void clearDevices() {
        mLeDevices.clear();
    }

    public void showBLEData(BLEDataServer.BLEData data) {
        mBLEDataMap.put(data.device, data);
    }

    public void setListener(DeviceItemClickListener listener) {
        mListener = listener;
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {

        Resources resources;

        TextView deviceName;
        TextView deviceAddress;
        TextView deviceState;
        TextView deviceRSSI;
        CardView deviceCard;
        Button Connect_Button;
        boolean connect_button_state;
        Button Pair_Button;
        boolean pair_button_state;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            resources = itemView.getResources();

            deviceCard = (CardView) itemView.findViewById(R.id.device_card);
            deviceName = (TextView) itemView.findViewById(R.id.device_name);
            deviceAddress = (TextView) itemView.findViewById(R.id.device_address);
            deviceState = (TextView) itemView.findViewById(R.id.connection_state);
            deviceRSSI = (TextView) itemView.findViewById(R.id.rssi);
            Connect_Button = (Button) itemView.findViewById(R.id.Connect_Button);
            Pair_Button = (Button) itemView.findViewById(R.id.Pair_Button);
        }

        public String getDeviceName() {
            return deviceName.getText().toString();
        }
    }

    public interface DeviceItemClickListener {
        void onItemClicked(BluetoothDevice device, int position);
        void onItemConnectionButtonClicked(BluetoothDevice device, int position, boolean connection_state_setting);
        void onItemPairButtonClicked(BLEDataServer.BLEData data, int position, boolean bonding_state_setting);
    }
}