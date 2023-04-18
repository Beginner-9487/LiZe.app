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

import com.example.lize_app.SampleGattAttributes;
import com.example.lize_app.injector.ApplicationContext;
import com.example.lize_app.utils.Log;
import com.example.lize_app.utils.MyNamingStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            // Ignore all devices without name.
            if(result.getDevice().getName() == null) { return; }

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

                    // Ignore all devices without name.
                    if(r.getDevice().getName() == null) { return; }

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

    // TODO mGattCallback
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.e(gatt.getDevice().getName() + ": newState: " + newState);

            super.onConnectionStateChange(gatt, status, newState);
            BLEData d = findBLEData(gatt);
            List<ObservableEmitter<BLEData>> emitters = findObservableEmitter(gatt);

            d.connectedState = newState;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            }

            for (ObservableEmitter<BLEData> s : emitters) {
                s.onNext(d);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            BLEData d = findBLEData(gatt);
            List<ObservableEmitter<BLEData>> subscribers = findObservableEmitter(gatt);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                d.services = gatt.getServices();

                // Add CCCD to all Characteristics
                for (BluetoothGattService service:gatt.getServices()) {
                    for (BluetoothGattCharacteristic characteristic:service.getCharacteristics()) {
                        if(SampleGattAttributes.checkSubscribed(String.valueOf(characteristic.getUuid()))) {
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

                for (ObservableEmitter<BLEData> s : subscribers) {
                    s.onNext(d);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                whenFetchingData(gatt, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            whenFetchingData(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
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

    // TODO Otherfunction

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

    final boolean AutoConnect = true;
    Observable<BLEData> connect(final BluetoothDevice device) {
        // if device is already connected,
        return Observable.create(new ObservableOnSubscribe<BLEData>() {
            @Override
            public void subscribe(ObservableEmitter<BLEData> e) throws Exception {
            BluetoothGatt gatt = findBluetoothGatt(device);

            if (gatt == null) {
                gatt = device.connectGatt(mContext, AutoConnect, mGattCallback);
            } else {
                gatt.connect();
                e.onNext(findBLEData(gatt));
            }

            for (Map.Entry<ObservableEmitter<BLEData>, BluetoothGatt> entry:mGattMap.entrySet()) {
                if(entry.getValue().equals(gatt)) {
                    mGattMap.remove(entry.getKey());
                }
            }
            mGattMap.put(e, gatt);
            }
        });
    }

    void disconnect(final BluetoothDevice device) {
        BluetoothGatt gatt = findBluetoothGatt(device);
        if (gatt != null) {
            BLEData bleData = findBLEData(gatt);
            if(bleData.connectedState != BluetoothProfile.STATE_DISCONNECTED && bleData.connectedState != BluetoothProfile.STATE_DISCONNECTING) {
                gatt.disconnect();
            }
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

    public void whenFetchingData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        BLEData d = findBLEData(gatt);
        List<ObservableEmitter<BLEData>> subscribers = findObservableEmitter(gatt);

        if(!d.Values.containsKey(characteristic.getService())) {
            d.Values.put(characteristic.getService(), new HashMap());
        }
        if(!d.Values.get(characteristic.getService()).containsKey(characteristic)) {
            d.Values.get(characteristic.getService()).put(characteristic, new ArrayList<BLEData.Dataset>());
        }
        d.addNewDataIntoDataset(characteristic, characteristic.getValue());

        for (ObservableEmitter<BLEData> s : subscribers) {
            s.onNext(d);
        }
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
            if (d.getDevice().equals(device)) {
                return d;
            }
        }

        return null;
    }

    private List<ObservableEmitter<BLEData>> findObservableEmitter(BluetoothGatt gatt) {
        List<ObservableEmitter<BLEData>> emitters = new ArrayList<>();

        for(ObservableEmitter<BLEData> s : mGattMap.keySet()) {
            if (mGattMap.get(s).equals(gatt)) {
                emitters.add(s);
            }
        }

        return emitters;
    }

    private BLEData findBLEData(BluetoothGatt gatt) {
        for (BLEData d : mBLEDatas) {
            if (gatt.getDevice().equals(d.device)) {
                return d;
            }
        }

        BLEData d = new BLEData(gatt.getDevice());
        mBLEDatas.add(d);

        return d;
    }

    // TODO 不知道要 public 好，還是 private 好
    public BLEData findBLEDataByDevice(BluetoothDevice device) {
        for (BLEData d : mBLEDatas) {
            if (d.device.equals(device)) {
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

                        if(SampleGattAttributes.checkInput(String.valueOf(characteristic.getUuid()))) {
                            characteristic.setValue(command);
                            gatt.writeCharacteristic(characteristic);
                        }

                    }
                }
            }

            gatt.executeReliableWrite();

        }
    }

    public void removeDataByLabelname(BLEDataServer.BLEData bleData, String labelName) {
        bleData.removeDataByLabelname(labelName);
        if (mLEScanEmitter != null) {
            mLEScanEmitter.onNext(bleData);
        }
    }

    public void SetAllNameBuffer(String labelName) {
        for (BluetoothGatt gatt:mGattMap.values()) {
            findBLEData(gatt).labelNameBuffer = labelName;
        }
    }

    public class BLEData {
        public BluetoothDevice device;
        public int rssi;
        public String labelNameBuffer;
        public int connectedState = BluetoothProfile.STATE_DISCONNECTED;
        public List<BluetoothGattService> services; // 從這裡讀取 UUID, Properties, Value, Descriptor

        public BLEData(BluetoothDevice device) {
            this.device = device;
        }

        // Store the value for each time points
        public class Dataset {
            public String labelname;
            public ArrayList<byte[]> data;
            public Dataset(String Labelname) {
                labelname = Labelname;
                data = new ArrayList<>();
            }
            public void addData(byte[] bytes) {
                data.add(bytes);
            }
        }
        public Dataset createNewDataset() { return new Dataset(labelNameBuffer); }
        public void addNewDataIntoDataset(BluetoothGattCharacteristic characteristic, byte[] bytes) {
            for (Dataset d : Values.get(characteristic.getService()).get(characteristic)) {
                // If the dataset does exist.
                if(d.labelname.equals(labelNameBuffer)) {
                    d.addData(bytes);
                    return;
                }
            }
            // If the dataset doesn't exist.
            Dataset d = createNewDataset();
            d.addData(bytes);
            Values.get(characteristic.getService()).get(characteristic).add(d);
        }
        public void addNewDataIntoDataset(BluetoothGattService service, byte[] bytes) {
            for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                addNewDataIntoDataset(c, bytes);
            }
        }
        public void addNewDataIntoDataset(byte[] bytes) {
            for (BluetoothGattService s : Values.keySet()) {
                addNewDataIntoDataset(s, bytes);
            }
        }
        public HashMap<BluetoothGattService, HashMap<BluetoothGattCharacteristic, ArrayList<Dataset>>> Values = new HashMap<>();

        public boolean in_Emitter() {
            return (findObservableEmitter(findBluetoothGatt(device)).size()>0) ? true : false;
        }

        public ArrayList<byte[]> getDataByLabelname(String LabelName) {
            // Log.e("getDataByLabelname0");
            for (HashMap<BluetoothGattCharacteristic, ArrayList<Dataset>> v:Values.values()) {
                // Log.e("getDataByLabelname1");
                for (ArrayList<Dataset> datasets:v.values()) {
                    int index = 0;
                    for (Dataset d : datasets) {
                        // Log.e("d.labelname: " + d.labelname);
                        if(d.labelname.equals(LabelName)) {
                            return d.data;
                        }
                        index++;
                    }
                }
            }
            return null;
        }

        public void removeDataByLabelname(String LabelName) {
            for (HashMap<BluetoothGattCharacteristic, ArrayList<Dataset>> v:Values.values()) {
                for (ArrayList<Dataset> datasets:v.values()) {
                    int index = 0;
                    for (Dataset d : datasets) {
                        if(d.labelname.equals(LabelName)) {
                            datasets.remove(index);
                            break;
                        }
                        index++;
                    }
                }
            }
        }
    }

    public ArrayList<byte[]> getDataByLabelname(String LabelName) {
        for (BLEData d : mBLEDatas) {
            ArrayList<byte[]> data = d.getDataByLabelname(LabelName);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

}