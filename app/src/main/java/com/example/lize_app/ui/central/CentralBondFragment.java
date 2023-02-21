package com.example.lize_app.ui.central;

public class CentralBondFragment extends CentralDevicesFragment implements CentralMvpView {

    @Override
    public void onStart() {
        super.onStart();
        mCentralPresenter.attachView(this);
        mCentralPresenter.getAllBondedDevices();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCentralPresenter.getAllBondedDevices();
    }

    @Override
    public void onRefreshSwipLayout() {
        mCentralPresenter.getAllBondedDevices();
        mRefreshLayout.setRefreshing(false);
    }

}
