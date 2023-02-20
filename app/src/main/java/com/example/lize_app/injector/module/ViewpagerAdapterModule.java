package com.example.lize_app.injector.module;

import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import dagger.Module;

/**
 *
 */
@Module
public class ViewpagerAdapterModule {

    private FragmentStatePagerAdapter mFragmentStatePagerAdapter;

    public ViewpagerAdapterModule(FragmentStatePagerAdapter fragmentStatePagerAdapter) {
        mFragmentStatePagerAdapter = fragmentStatePagerAdapter;
    }

}
