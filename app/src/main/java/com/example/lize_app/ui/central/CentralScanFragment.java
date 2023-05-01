package com.example.lize_app.ui.central;

import com.example.lize_app.ui.base.BasePresenter;

import android.os.Handler;
import android.util.Log;

public class CentralScanFragment extends CentralDevicesFragment implements CentralMvpView {

    private final int BLE_SCAN_PEROID = 10000;

    @Override
    public void onStart() {
        super.onStart();
        mCentralPresenter.attachView(this);
        mCentralPresenter.initForBLEDatas();
        mCentralPresenter.startReadRssi();
        scanLeDevice(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCentralPresenter.getRemoteDevices();
    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        // getmLeDeviceAdapter().clearDevices();
        // getmLeDeviceAdapter().notifyDataSetChanged();
        mCentralPresenter.detachView();
    }

    private void scanLeDevice(final boolean enable) {

        try {
            mCentralPresenter.scanBLEPeripheral(enable);
        } catch (BasePresenter.MvpViewNotAttachedException e) {
            Log.e(CentralScanFragment.class.getName(), e.toString());
            return;
        }

        if (enable) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                mRefreshLayout.setRefreshing(false);
                scanLeDevice(false);
                }
            }, BLE_SCAN_PEROID);
        }
    }

    @Override
    public void onRefreshSwipLayout() {
        scanLeDevice(true);
    }
}
