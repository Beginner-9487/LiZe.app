package com.example.lize_app.injector.component;

import com.example.lize_app.injector.PerFragment;
import com.example.lize_app.injector.module.FragmentModule;
import com.example.lize_app.ui.central.CentralBondFragment;
import com.example.lize_app.ui.central.CentralChartFragment;
import com.example.lize_app.ui.central.CentralDevicesFragment;
import com.example.lize_app.ui.central.CentralScanFragment;
import com.example.lize_app.ui.central.CentralTempUI;

import dagger.Component;

/**
 *
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class}, modules = FragmentModule.class)
public interface FragmentComponent {
    void inject(CentralDevicesFragment fragment);
    void inject(CentralChartFragment fragment);
    void inject(CentralBondFragment fragment);
    void inject(CentralScanFragment fragment);
    void inject(CentralTempUI fragment);
}
