package com.example.lize_app.injector.component;

import com.example.lize_app.injector.PerViewpagerAdapter;
import com.example.lize_app.injector.module.ViewpagerAdapterModule;
import com.example.lize_app.ui.central.CentralViewpagerAdapter;

import dagger.Component;

/**
 *
 */
@PerViewpagerAdapter
@Component(dependencies = ApplicationComponent.class, modules = ViewpagerAdapterModule.class)
public interface ViewpagerAdapterComponent {
    void inject(CentralViewpagerAdapter viewpagerAdapter);
}