package com.example.lize_app.injector.component;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;

import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.data.BLEPeripheralServer;
import com.example.lize_app.data.DataManager;
import com.example.lize_app.injector.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 *
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

//    void inject(BluetoothLeService service);
    void inject(BLEPeripheralServer server);

    BluetoothManager bluetoothManager();
    BluetoothAdapter bluetoothAdapter();
    BluetoothLeScanner bluetoothLeScanner();

    DataManager dataManager();
    BLEDataServer bleDataServer();
}
