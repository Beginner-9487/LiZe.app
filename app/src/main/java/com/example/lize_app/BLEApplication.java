package com.example.lize_app;

import android.app.Application;

import com.example.lize_app.injector.component.ApplicationComponent;
import com.example.lize_app.injector.component.DaggerApplicationComponent;
import com.example.lize_app.injector.module.ApplicationModule;

/**
 * Created by jacobsu on 4/21/16.
 */
public class BLEApplication extends Application {

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    public ApplicationComponent getApplicationComponent() {
        if (mApplicationComponent == null) {
            mApplicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }

        return mApplicationComponent;
    }
}
