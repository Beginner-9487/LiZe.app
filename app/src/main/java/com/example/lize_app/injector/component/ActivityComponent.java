package com.example.lize_app.injector.component;

import android.bluetooth.le.BluetoothLeAdvertiser;

import com.example.lize_app.injector.PerActivity;
import com.example.lize_app.injector.module.ActivityModule;
import com.example.lize_app.ui.central.CentralActivity;
import com.example.lize_app.ui.central.CentralDetailsActivity;
import com.example.lize_app.ui.main.MainActivity;
import com.example.lize_app.ui.peripheral.PeripheralActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {
    void inject(MainActivity activity);
    void inject(CentralActivity activity);
    void inject(CentralDetailsActivity activity);
    void inject(PeripheralActivity activity);

}
