package com.example.lize_app.injector.module;

import androidx.fragment.app.Fragment;

import com.example.lize_app.data.DataManager;
import com.example.lize_app.ui.central.CentralPresenter;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class FragmentModule {

    private Fragment mFragment;

    public FragmentModule(Fragment fragment) {
        mFragment = fragment;
    }

}
