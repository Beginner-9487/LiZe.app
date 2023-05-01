package com.example.lize_app.ui.central;

import static java.lang.Integer.parseInt;

import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.fragment.app.FragmentActivity;

import com.example.lize_app.R;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.data.CentralDataManager;
import com.example.lize_app.data.DataManager;
import com.example.lize_app.ui.base.BasePresenter;
import com.example.lize_app.utils.Log;
import com.example.lize_app.utils.MyNamingStrategy;
import com.example.lize_app.utils.My_Excel_File;
import com.example.lize_app.utils.OtherUsefulFunction;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by jacobsu on 4/27/16.
 */
public class CentralPresenter extends BasePresenter<CentralMvpView> {

    private DataManager mDataManager;
    private CentralDataManager mCentralDataManager;

    private Disposable mScanDisposable;
    private final HashMap<BluetoothDevice, Disposable> mConnectedDisposable;

    @Inject
    public CentralPresenter(DataManager dataManager, CentralDataManager centralDataManager) {
        mDataManager = dataManager;
        mCentralDataManager = centralDataManager;
        mConnectedDisposable = new HashMap<>();
    }

    @Override
    public void attachView(CentralMvpView centralView) {
        super.attachView(centralView);
    }

    public void initForBLEDatas() {
        List<BLEDataServer.BLEData> data = mDataManager.getRemoteBLEDatas();

        for(BLEDataServer.BLEData d: data) {
            getMvpView().showBLEData(d);
        }
    }

    @Override
    public void detachView() {
        super.detachView();

//        for (BluetoothDevice device:mConnectedDisposable.keySet()) {
//            disconnectGatt(device);
//        }
//
//        if (mScanDisposable != null && !mScanDisposable.isDisposed()) {
//            mScanDisposable.dispose();
//        }
//
//        for (Disposable s : mConnectedDisposable.values()) {
//            if (!s.isDisposed()) {
//                s.dispose();
//            }
//        }
//
//        mConnectedDisposable.clear();
//        stopReadRssi();
    }

    public void getRemoteDevices() {
        try {
            checkViewAttached();

            List<BLEDataServer.BLEData> data = mDataManager.getRemoteBLEDatas();

            for(BLEDataServer.BLEData d: data) {
                getMvpView().showBLEDevice(d.device);
                getMvpView().showBLEData(d);
            }
        } catch (Exception e) {
            // Log.e(e.getMessage());
        }
    }

    public void scanBLEPeripheral(boolean enabled) {
        try {
            checkViewAttached();

            if (mScanDisposable != null && !mScanDisposable.isDisposed()) {
                mScanDisposable.dispose();
            }

            mScanDisposable = mDataManager.scanBLEPeripheral(enabled)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<BLEDataServer.BLEData>() {
                    @Override
                    public void accept(BLEDataServer.BLEData bleData) throws Exception {
                        getMvpView().showBLEDevice(bleData.device);
                        if (!mConnectedDisposable.containsKey(bleData.device)) { getMvpView().showBLEData(bleData); }
                    }
                });
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }

    public void connectGatt(BluetoothDevice device) {
        try {
            checkViewAttached();

            // debugs here! if connect same bluetoothDevice multi times
            for (BluetoothDevice s : mConnectedDisposable.keySet()) {
                if (s == device) {
                    mConnectedDisposable.get(s).dispose();
                    mConnectedDisposable.remove(s);
                    break;
                }
            }

            Disposable s = mDataManager.connectGatt(device)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<BLEDataServer.BLEData>() {
                    @Override
                    public void accept(BLEDataServer.BLEData bleData) throws Exception {
                        if (isViewAttached()) {
                            getMvpView().showBLEData(bleData);
                        }
                    }
                });

            mConnectedDisposable.put(device, s);
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }

    public void disconnectGatt(BluetoothDevice device) {
        try {
            mDataManager.disconnectGatt(device);
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }

    public void createBond(BLEDataServer.BLEData data) {
        try {
            checkViewAttached();
            mDataManager.createBond(data.device);
//            getMvpView().showBLEData(data);
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }

    private static final int READ_RSSI_REPEAT = 1;
    private final long READING_RSSI_TASK_FREQUENCY = 2000;

    private final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READ_RSSI_REPEAT:
                    synchronized (CentralPresenter.this) {
                        for (BLEDataServer.BLEData d : mDataManager.getRemoteBLEDatas()) {
                            readRemoteRssi(d.device);
                        }
                    }

                    sendMessageDelayed(
                            obtainMessage(READ_RSSI_REPEAT),
                            READING_RSSI_TASK_FREQUENCY
                    );
                    break;
            }
        }
    };

    public void startReadRssi() {
        if (mHandler.hasMessages(READ_RSSI_REPEAT)) {
            return;
        }

        mHandler.sendEmptyMessage(READ_RSSI_REPEAT);
    }

    private void stopReadRssi() {
        mHandler.removeMessages(READ_RSSI_REPEAT);
    }

    public boolean readRemoteRssi(BluetoothDevice device) {
        return mDataManager.readRemoteRssi(device);
    }



    public List<BLEDataServer.BLEData> getRemoteBLEData() {
        return mDataManager.getRemoteBLEDatas();
    }

    public void getAllBondedDevices() {
        try {
            checkViewAttached();

            // send all devices to view here;
            List<BLEDataServer.BLEData> data = mDataManager.getAllBondedDevices();

            for(BLEDataServer.BLEData d: data) {
                getMvpView().showBLEDevice(d.device);
                getMvpView().showBLEData(d);
            }

        } catch (Exception e) {
            Log.e(e.getMessage());
        }

    }



    // =================================================================================================
    // Temp UI

    public void Send_All_C(byte[] command) {
        mDataManager.Send_All_C(command);
    }

    public void removeLabelDataOfBLE(BLEDataServer.BLEData bleData, String labelName) {
        mCentralDataManager.findDeviceDataByBle(bleData).removeLabelDataOfBLE(labelName);
    }

    public void SetAllNamingStrategy(MyNamingStrategy labelNameStrategy) {
        for(CentralDataManager.DeviceData d: mCentralDataManager.deviceData) {
            d.labelNamingStrategy = labelNameStrategy;
        }
    }

    static CentralTempUI centralTempUI;
    public void setCentralTempUI(CentralTempUI c) {
        centralTempUI = c;
    }
    public void updateLabelData(Resources resources, BLEDataServer.BLEData bleData) {
        mCentralDataManager.updateLabelData(resources, bleData);
    }
    public ArrayList<CentralDataManager.DeviceData> getDeviceData() {
        return mCentralDataManager.deviceData;
    }
    public String[] getDataTypes(Resources resources) {
        mCentralDataManager.DataTypes = resources.getStringArray(R.array.DataTypes);
        return mCentralDataManager.DataTypes;
    }

    public void setCurrentView(FragmentActivity activity) {
        mCentralDataManager.activity = activity;
    }

    public boolean saveMyFile(FragmentActivity activity, CentralDataManager.LabelData labelData) {
        setCurrentView(activity);
        return labelData.saveMyFile();
    }

    // =================================================================================================
    // Test
    public BLEDataServer getBLEDataServer() {
        return mDataManager.getBLEServer();
    }

}
