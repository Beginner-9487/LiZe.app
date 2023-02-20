/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.lize_app.ui.central;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.lize_app.R;
import com.example.lize_app.ui.base.BaseActivity;
import com.example.lize_app.ui.central.CentralChartFragment;

import java.util.ArrayList;

import javax.inject.Inject;

//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.Unbinder;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class CentralActivity extends BaseActivity {

    @Inject
    BluetoothAdapter mBluetoothAdapter;

//    @BindView(R.id.toolbar)
    Toolbar mToolbar;

//    @BindView(R.id.ViewPager)
    ViewPager mViewPager;

//    private Unbinder unbinder;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivityComponent().inject(this);

        setContentView(R.layout.central_mode);

//        unbinder = ButterKnife.bind(this);
        mToolbar = findViewById(R.id.toolbar);
        mViewPager = findViewById(R.id.ViewPager);

        if (mToolbar != null) {
            mToolbar.setTitle(R.string.Central_mode);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Use this check to determine whether BLE is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Checks if Bluetooth is enabled
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        setup_ViewPager();

//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.content, new CentralScanFragment(), "scan")
//                .commit();

    }

    @Override
    public void onDestroy() {
//        unbinder.unbind();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void setup_ViewPager() {

        // ---------------------------------------------------------------------------------------------------------------------------------------------------------
        // 顯示
        // http://uirate.net/?p=10958

        //1.宣告<ViewPager>標籤為viewPager
        //ViewPager viewPager;

        //2.宣告使用轉換器
        CentralViewpagerAdapter adapter;

        //3.宣告變數為fragments
        String[] titles;
        Fragment[] fragments;

        //4.指定activity_main.xml內標籤

        //5.初始化三個Fragment分頁
        int page = 4;
        titles = new String[page];
        fragments = new Fragment[page];

        //6.陣列內容
        for(int i=0; i<page; i++) {
            titles[i] = getResources().getStringArray(R.array.ViewPager_PageTitle)[i];
        }
        fragments[0] = new CentralChartFragment();
        fragments[1] = new CentralBondFragment();
        fragments[2] = new CentralScanFragment();
        fragments[3] = new CentralTempUI();

        //7.初始化轉換器
        adapter = new CentralViewpagerAdapter(getSupportFragmentManager(), titles, fragments);

        //8.<ViewPager>標籤設定轉換器
        mViewPager.setAdapter(adapter);

        /*
        //----------------------------------------------------------------------------------------
        // 為了其他頁面瀏覽資料時，顯示資訊的列表位置可以同步
        ViewPager finalViewPager = mViewPager;
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int mScrollState = ViewPager.SCROLL_STATE_IDLE;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                mScrollState = state;
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    fragments[finalViewPager.getCurrentItem()].set_for_sync_view(fragments[prev_page].get_for_sync_view(true), fragments[prev_page].get_for_sync_view(false));
                }
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    prev_page = finalViewPager.getCurrentItem();
                }
            }

        });
         */

    }
}