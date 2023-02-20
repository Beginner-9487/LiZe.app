package com.example.lize_app.ui.central;

import android.bluetooth.BluetoothDevice;

import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.ui.base.MvpView;

/**
 *
 */
public interface CentralMvpView extends MvpView {
    void showBLEDevice(BluetoothDevice bt);
    void showBLEData(BLEDataServer.BLEData data);
}
