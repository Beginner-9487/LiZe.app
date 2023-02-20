package com.example.lize_app.data;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.widget.Toast;

import com.example.lize_app.injector.ApplicationContext;
import com.example.lize_app.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 *
 */
public class BLEDataServer {

    private Context mContext;
    private BluetoothLeScanner mLeScanner;
    private BLEPeripheralServer mPeripheralServer;

    private BluetoothAdapter mbluetoothAdapter;

    private ObservableEmitter<BLEDataServer.BLEData> mLEScanEmitter;

    // BlueGatt -> BLEData
    // BlueGatt -> ObservableEmitter
    private List<BLEData> mBLEDatas = new ArrayList<>();
    private Map<ObservableEmitter<BLEData>, BluetoothGatt> mGattMap = new HashMap<>();

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BLEData d = findBLEDataByDevice(result.getDevice());
            d.rssi = result.getRssi();
            if (mLEScanEmitter != null) {
                mLEScanEmitter.onNext(d);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            if (mLEScanEmitter != null) {
                for (ScanResult r : results) {
                    BLEData d = findBLEDataByDevice(r.getDevice());
                    d.rssi = r.getRssi();
                    mLEScanEmitter.onNext(d);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            if (mLEScanEmitter != null) {
                // mLEScanEmitter.onError(new Throwable("scan failed with errorCode: " + errorCode));
                mLEScanEmitter.onComplete();
            }
        }
    };


    // TODO 不知道有沒有反應
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Toast.makeText(mContext, "onConnectionStateChange: " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();

            super.onConnectionStateChange(gatt, status, newState);
            BLEData d = findBLEData(gatt);
            List<ObservableEmitter<BLEData>> emitters = findObservableEmitter(gatt);

            d.connectedState = newState;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();

                // Add CCCD to all Characteristics
                for (BluetoothGattService service:gatt.getServices()) {
                    for (BluetoothGattCharacteristic characteristic:service.getCharacteristics()) {
                        boolean success = gatt.setCharacteristicNotification(characteristic, true);
                        if (success) {
                            // 来源：http://stackoverflow.com/questions/38045294/oncharacteristicchanged-not-called-with-ble
                            for(BluetoothGattDescriptor dp: characteristic.getDescriptors()){
                                if (dp != null) {
                                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                        dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                                        dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                    }
                                    gatt.writeDescriptor(dp);
                                }
                            }
                        }
                    }
                }
            }

            for (ObservableEmitter<BLEData> s : emitters) {
                s.onNext(d);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            Toast.makeText(mContext, "onServicesDiscovered: " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();

            super.onServicesDiscovered(gatt, status);
            BLEData d = findBLEData(gatt);
            List<ObservableEmitter<BLEData>> subscribers = findObservableEmitter(gatt);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                d.services = gatt.getServices();

                for (ObservableEmitter<BLEData> s : subscribers) {
                    s.onNext(d);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            Toast.makeText(mContext, "onCharacteristicRead: " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();

            super.onCharacteristicRead(gatt, characteristic, status);
            BLEData d = findBLEData(gatt);
            List<ObservableEmitter<BLEData>> subscribers = findObservableEmitter(gatt);

            if (status == BluetoothGatt.GATT_SUCCESS) {

                if(!d.Values.containsKey(characteristic.getService())) {
                    d.Values.put(characteristic.getService(), new HashMap());
                }
                if(!d.Values.get(characteristic.getService()).containsKey(characteristic)) {
                    ArrayList arrayList = new ArrayList();
                    d.Values.get(characteristic.getService()).put(characteristic, arrayList);
                }
                d.Values.get(characteristic.getService()).get(characteristic).add(characteristic.getValue());

                for (ObservableEmitter<BLEData> s : subscribers) {
                    s.onNext(d);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            Toast.makeText(mContext, "onCharacteristicWrite: " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();

            super.onCharacteristicWrite(gatt, characteristic, status);
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            Toast.makeText(mContext, "onCharacteristicChanged: " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();

            super.onCharacteristicChanged(gatt, characteristic);
            BLEData d = findBLEData(gatt);
            List<ObservableEmitter<BLEData>> subscribers = findObservableEmitter(gatt);

            if(!d.Values.containsKey(characteristic.getService())) {
                d.Values.put(characteristic.getService(), new HashMap());
            }
            if(!d.Values.get(characteristic.getService()).containsKey(characteristic)) {
                ArrayList arrayList = new ArrayList();
                d.Values.get(characteristic.getService()).put(characteristic, arrayList);
            }
            d.Values.get(characteristic.getService()).get(characteristic).add(characteristic.getValue());

            for (ObservableEmitter<BLEData> s : subscribers) {
                s.onNext(d);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            Toast.makeText(mContext, "onDescriptorRead: " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();

            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            Toast.makeText(mContext, "onDescriptorWrite: " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();

            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

            Toast.makeText(mContext, "onReliableWriteCompleted: " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();

            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

            Toast.makeText(mContext, "onReadRemoteRssi: " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();

            super.onReadRemoteRssi(gatt, rssi, status);
            BLEData d = findBLEData(gatt);
            List<ObservableEmitter<BLEData>> emitters = findObservableEmitter(gatt);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                d.rssi = rssi;

                for (ObservableEmitter<BLEData> s : emitters) {
                    s.onNext(d);
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {

            Toast.makeText(mContext, "onMtuChanged: " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();

            super.onMtuChanged(gatt, mtu, status);
        }
    };

    @Inject
    public BLEDataServer(@ApplicationContext Context context, BluetoothLeScanner leScanner, BLEPeripheralServer peripheralServer, BluetoothAdapter bluetoothAdapter) {
        mContext = context;
        mLeScanner = leScanner;
        mPeripheralServer = peripheralServer;
        mbluetoothAdapter = bluetoothAdapter;
    }

    Observable<BLEDataServer.BLEData> scanBLEPeripheral(final boolean enabled) {

        return Observable.create(new ObservableOnSubscribe<BLEDataServer.BLEData>() {
            @Override
            public void subscribe(ObservableEmitter<BLEDataServer.BLEData> e) throws Exception {
                if (enabled) {
                    mLEScanEmitter = e;
                    mLeScanner.startScan(mScanCallback);
                } else {
                    e.onComplete();
                    mLeScanner.stopScan(mScanCallback);
                }

            }
        });
    }

    Observable<BLEData> connect(final BluetoothDevice device) {
        // if device is already connected,
        return Observable.create(new ObservableOnSubscribe<BLEData>() {
            @Override
            public void subscribe(ObservableEmitter<BLEData> e) throws Exception {
                BluetoothGatt gatt = findBluetoothGatt(device);

                if (gatt == null) {
                    gatt = device.connectGatt(mContext, false, mGattCallback);
                } else {
                    e.onNext(findBLEData(gatt));
                }

                mGattMap.put(e, gatt);
            }
        });
    }

    void disconnect(final BluetoothDevice device) {
        BluetoothGatt gatt = findBluetoothGatt(device);
        if (gatt != null && gatt.getConnectionState(device) != BluetoothProfile.STATE_DISCONNECTED) {
            gatt.disconnect();
        }
    }

    public List<BLEData> getRemoteBLEDatas() {
        return mBLEDatas;
    }

    public boolean readRemoteRssi(BluetoothDevice device) {
        BluetoothGatt gatt = findBluetoothGatt(device);

        if (gatt != null) {
            return gatt.readRemoteRssi();
        }

        return false;
    }

    public void startCentralMode() {
        // stop Peripheral Mode first
        stopPeripheralMode();
    }

    public void stopCentralMode() {
        // stop scan
        // disconnect from all gatt
    }

    public boolean supportLEAdvertiser() {
        return mPeripheralServer.supportPeripheralMode();
    }

    public void startPeripheralMode(String name) {
        // stop Central mode first
        stopCentralMode();

        mPeripheralServer.startPeripheralMode(name);
    }

    public void stopPeripheralMode() {
        mPeripheralServer.stopPeripheralMode();
    }

    private BluetoothGatt findBluetoothGatt(BluetoothDevice device) {
        for (BluetoothGatt d : mGattMap.values()) {
            if (d.getDevice() == device) {
                return d;
            }
        }

        return null;
    }

    private List<ObservableEmitter<BLEData>> findObservableEmitter(BluetoothGatt gatt) {
        List<ObservableEmitter<BLEData>> emitters = new ArrayList<>();

        for(ObservableEmitter<BLEData> s : mGattMap.keySet()) {
            if (mGattMap.get(s) == gatt) {
                emitters.add(s);
            }
        }

        return emitters;
    }

    private BLEData findBLEData(BluetoothGatt gatt) {
        for (BLEData d : mBLEDatas) {
            if (gatt.getDevice() == d.device) {
                return d;
            }
        }

        BLEData d = new BLEData(gatt.getDevice());
        mBLEDatas.add(d);

        return d;
    }

    public class BLEData {
        public BluetoothDevice device;
        public int rssi;
        public String data; // TODO 正在想可以存甚麼
        public int connectedState = BluetoothProfile.STATE_DISCONNECTED;
        public List<BluetoothGattService> services; // 從這裡讀取 UUID, Properties, Value, Descriptor

        public BLEData(BluetoothDevice device) {
            this.device = device;
        }

        // Store the value for each time points
        public HashMap<BluetoothGattService, HashMap<BluetoothGattCharacteristic, ArrayList<byte[]>>> Values = new HashMap<>();

        public boolean in_Emitter() {
            return (findObservableEmitter(findBluetoothGatt(device)).size()>0) ? true : false;
        }
    }

    public boolean readRemoteValues(BluetoothDevice device) {
        BluetoothGatt gatt = findBluetoothGatt(device);

        if (gatt != null) {
            for (BluetoothGattService gattService: findBLEDataByDevice(device).services) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    return gatt.readCharacteristic(gattCharacteristic);
                }
            }
        }

        return false;
    }

    // TODO 不知道要 public 好，還是 private 好
    public BLEData findBLEDataByDevice(BluetoothDevice device) {
        for (BLEData d : mBLEDatas) {
            if (device == d.device) {
                return d;
            }
        }

        BLEData d = new BLEData(device);
        mBLEDatas.add(d);

        return d;
    }

//    public ArrayList<ArrayList<byte[]>> readValues(BluetoothDevice device) {
//        return findBLEDataByDevice(device).Values;
//    }

    public void createBond(BluetoothDevice device) {
        findBLEDataByDevice(device);    // 沒有就加入
        if(device.getBondState() == BluetoothDevice.BOND_NONE) {
            device.createBond();
        }
    }

    public List<BLEData> getAllBondedDevices() {
        List<BLEData> bl = new ArrayList<>();
        for (BluetoothDevice b:mbluetoothAdapter.getBondedDevices()) {
            bl.add(findBLEDataByDevice(b));
        }
        return bl;
    }


    // =================================================================================================
    // Temp UI
    public void Send_All_C(byte[] command) {
        for (BluetoothGatt gatt:mGattMap.values()) {
            for (BluetoothGattService service:gatt.getServices()) {
                for (BluetoothGattCharacteristic characteristic:service.getCharacteristics()) {
                    if (((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                            (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
                        characteristic.setValue(command);
                        gatt.writeCharacteristic(characteristic);
                    }
                }
            }
        }
    }

}