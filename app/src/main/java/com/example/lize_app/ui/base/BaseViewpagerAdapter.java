package com.example.lize_app.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.lize_app.BLEApplication;
import com.example.lize_app.R;
import com.example.lize_app.injector.component.ActivityComponent;
import com.example.lize_app.injector.component.FragmentComponent;
import com.example.lize_app.injector.component.ViewpagerAdapterComponent;
import com.example.lize_app.injector.module.ActivityModule;
import com.example.lize_app.injector.module.FragmentModule;
import com.example.lize_app.injector.module.ViewpagerAdapterModule;
import com.example.lize_app.ui.central.CentralScanFragment;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class BaseViewpagerAdapter extends FragmentStatePagerAdapter {

    // 這只是 ViewPage 的 Adapter 的容器，沒甚麼重要的東西
    // http://uirate.net/?p=10958

    //2.宣告變數為mFragments
    private String[] mTitles;
    private Fragment[] mFragments;

    //3.初始化
    public BaseViewpagerAdapter(FragmentManager fm, String[] Titles, Fragment[] Fragments) {
        super(fm);
        mTitles = Titles;
        mFragments = Fragments;
    }

    //4.分頁內容
    @Override
    public Fragment getItem(int position) { return mFragments[position]; }

    //5.分頁數量
    @Override
    public int getCount() {
        return mFragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    private ViewpagerAdapterComponent mFragmentComponent;

//    public ViewpagerAdapterComponent getViewpagerAdapterComponent() {
//        if (mFragmentComponent == null && mFragments[0] != null) {
//            mFragmentComponent = DaggerPagerAdapterComponent.builder()
//                .applicationComponent(((BLEApplication)mFragments[0].getActivity().getApplication()).getApplicationComponent())
//                .viewpagerAdapterModule(new ViewpagerAdapterModule(this))
//                .build();
//        }
//
//        return mFragmentComponent;
//    }

}