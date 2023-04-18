package com.example.lize_app.ui.base;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class BaseViewpagerAdapter extends FragmentStatePagerAdapter {

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

//  Issue: mFragments[0].getActivity() == null
//    private ViewpagerAdapterComponent mViewpagerAdapterComponent;
//
//    public ViewpagerAdapterComponent getViewpagerAdapterComponent() {
//        if (mViewpagerAdapterComponent == null && mFragments[0] != null) {
//            mViewpagerAdapterComponent = DaggerViewpagerAdapterComponent.builder()
//                .applicationComponent(((BLEApplication)mFragments[0].getActivity().getApplication()).getApplicationComponent())
//                .viewpagerAdapterModule(new ViewpagerAdapterModule(this))
//                .build();
//        }
//
//        return mViewpagerAdapterComponent;
//    }

}