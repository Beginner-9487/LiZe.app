package com.example.lize_app.ui.central;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import com.example.lize_app.data.BLEDataServer;

public class CentralBondFragment extends CentralDevicesFragment implements CentralMvpView {

    @Override
    public void onStart() {
        super.onStart();
        mCentralPresenter.attach_getAllBondedDevices(this);
        mCentralPresenter.getAllBondedDevices();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCentralPresenter.getAllBondedDevices();
    }

    @Override
    public void onRefreshSwipLayout() {
        mCentralPresenter.getAllBondedDevices();
        mRefreshLayout.setRefreshing(false);
    }

}
