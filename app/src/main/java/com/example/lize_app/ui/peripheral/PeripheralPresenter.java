package com.example.lize_app.ui.peripheral;

import com.example.lize_app.data.DataManager;
import com.example.lize_app.ui.base.BasePresenter;

import javax.inject.Inject;

/**
 * Created by jacobsu on 5/10/16.
 */
public class PeripheralPresenter extends BasePresenter<PeripheralMvpView> {

    private DataManager mDataManager;

    @Inject
    public PeripheralPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    public void startLeAdvertise(String name) {
        if (mDataManager.supportAdvertiser()) {
            mDataManager.startPeripheralMode(name);
        } else {
            getMvpView().showSupportLEAdvertiser(false);
        }
    }

    public void stopLeAdvertise() {
        if (mDataManager.supportAdvertiser()) {
            mDataManager.stopPeripheralMode();
        }
    }
}
