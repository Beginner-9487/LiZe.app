package com.example.lize_app.ui.central;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.data.DataManager;
import com.example.lize_app.ui.base.BasePresenter;
import com.example.lize_app.utils.Log;

import java.util.HashMap;
import java.util.List;

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

    private Disposable mScanDisposable;
    private final HashMap<BluetoothDevice, Disposable> mConnectedDisposable;

    @Inject
    public CentralPresenter(DataManager dataManager) {
        mDataManager = dataManager;
        mConnectedDisposable = new HashMap<>();
    }

    @Override
    public void attachView(CentralMvpView centralView) {
        super.attachView(centralView);
    }

    public void attach_for_Data() {
        // send all deivces to view here;
        //List<BluetoothDevice> devices = mDataManager.getRemoteDevices();
        List<BLEDataServer.BLEData> datas = mDataManager.getRemoteBLEDatas();

        Log.e(String.valueOf(datas.size()));
        for(BLEDataServer.BLEData data: datas) {
            getMvpView().showBLEData(data);
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

            List<BLEDataServer.BLEData> datas = mDataManager.getRemoteBLEDatas();

            for(BLEDataServer.BLEData data: datas) {
                getMvpView().showBLEDevice(data.device);
                getMvpView().showBLEData(data);
            }
        } catch (Exception e) {
            Log.e(e.getMessage());
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
    private final long READING_RSSI_TASK_FREQENCY = 2000;

    private final Handler mHandler = new Handler() {
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
                            READING_RSSI_TASK_FREQENCY
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

    public boolean readRemoteValues(BluetoothDevice device) {
        return mDataManager.readRemoteValues(device);
    }


    public List<BLEDataServer.BLEData> getRemoteBLEDatas() {
        return mDataManager.getRemoteBLEDatas();
    }

    public void getAllBondedDevices() {
        try {
            checkViewAttached();

            // send all deivces to view here;
            List<BLEDataServer.BLEData> datas = mDataManager.getAllBondedDevices();

            for(BLEDataServer.BLEData data: datas) {
                getMvpView().showBLEDevice(data.device);
                getMvpView().showBLEData(data);
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

}
