package com.example.lize_app.ui.central;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.lize_app.ui.base.BaseViewpagerAdapter;

public class CentralViewpagerAdapter extends BaseViewpagerAdapter {

    public CentralViewpagerAdapter(FragmentManager fm, String[] Titles, Fragment[] Fragments) {
        super(fm, Titles, Fragments);
    }

}