package com.example.lize_app.data;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 *
 */
@Singleton
public class DataManager {

    private BLEDataServer mBLEServer;
    public BLEDataServer getmBLEServer() {
        return mBLEServer;
    }

    @Inject
    public DataManager(BLEDataServer bleServer) {
        mBLEServer = bleServer;
    }

    public void startCentralMode() {
        mBLEServer.startCentralMode();
    }

    public void stopCentralMode() {
        mBLEServer.stopCentralMode();
    }

    public List<BLEDataServer.BLEData> getRemoteBLEDatas() {
        return mBLEServer.getRemoteBLEDatas();
    }

    public Observable<BLEDataServer.BLEData> scanBLEPeripheral(boolean enabled) {
        return mBLEServer.scanBLEPeripheral(enabled);
    }

    public Observable<BLEDataServer.BLEData> connectGatt(BluetoothDevice device) {
        return mBLEServer.connect(device);
    }

    public void disconnectGatt(BluetoothDevice device) {
        mBLEServer.disconnect(device);
    }
//    public BLEDataServer.BLEData disconnectGatt(BluetoothDevice device) {
//        return mBLEServer.disconnect(device);
//    }

    public void createBond(BluetoothDevice device) {
        mBLEServer.createBond(device);
    }

    public boolean readRemoteRssi(BluetoothDevice device) {
        return mBLEServer.readRemoteRssi(device);
    }

    public boolean supportAdvertiser() {
        return mBLEServer.supportLEAdvertiser();
    }

    public void startPeripheralMode(String name) {
        mBLEServer.startPeripheralMode(name);
    }

    public void stopPeripheralMode() {
        mBLEServer.stopPeripheralMode();
    }

    public List<BLEDataServer.BLEData> getAllBondedDevices() {
        return mBLEServer.getAllBondedDevices();
    }

    // =================================================================================================
    // Temp UI
    public void Send_All_C(byte[] command) {
        mBLEServer.Send_All_C(command);
    }
    public void SetAllNameBuffer(String labelName) {
        mBLEServer.SetAllNameBuffer(labelName);
    }

    public void removeDataByLabelname(BLEDataServer.BLEData bleData, String labelName) {
        mBLEServer.removeDataByLabelname(bleData, labelName);
    }

    public ArrayList<byte[]> getDataByLabelname(String LabelName) {
        return mBLEServer.getDataByLabelname(LabelName);
    }

}
