package com.example.lize_app.injector.module;

import android.app.Activity;

import com.example.lize_app.data.CentralDataManager;
import com.example.lize_app.data.DataManager;
import com.example.lize_app.ui.central.CentralPresenter;
import com.example.lize_app.ui.main.MainPresenter;
import com.example.lize_app.ui.peripheral.PeripheralPresenter;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class ActivityModule {

    private Activity mActivity;

    public ActivityModule(Activity activity) {
        mActivity = activity;
    }

    @Provides
    public MainPresenter provideMainPresenter() {
        return new MainPresenter();
    }

    @Provides
    public PeripheralPresenter providePeripheralPresenter(DataManager dataManager) {
        return new PeripheralPresenter(dataManager);
    }

    @Provides
    public CentralPresenter provideCentralPresenter(DataManager dataManager, CentralDataManager centralDataManager) {
        return new CentralPresenter(dataManager, centralDataManager);
    }

}
