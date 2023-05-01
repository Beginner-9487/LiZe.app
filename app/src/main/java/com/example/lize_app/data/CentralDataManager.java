package com.example.lize_app.data;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.lize_app.R;
import com.example.lize_app.SampleGattAttributes;
import com.example.lize_app.utils.Log;
import com.example.lize_app.utils.MyNamingStrategy;
import com.example.lize_app.utils.My_Excel_File;
import com.example.lize_app.utils.OtherUsefulFunction;
import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CentralDataManager {

    @Inject
    public CentralDataManager() {}

    public ArrayList<DeviceData> deviceData = new ArrayList<>();

    public String[] DataTypes;

    public FragmentActivity activity;

    public class LabelData {
        public String labelName;
        public boolean show = true;
        public byte isDownloaded = 0;
        public int type = -1;
        public int numberOfData = 0;
        public String xLabel;
        public String yLabel;
        public String specialLabel;
        public float xPrecision;
        public float yPrecision;

        public Entry CreateNewEntryByBytes(byte[] bytesVoltageInteger, byte[] bytesVoltageDecimal, byte[] bytesCurrentInteger, byte[] bytesCurrentDecimal) {
            return new Entry(
                    OtherUsefulFunction.byteArrayToSignedInt(bytesVoltageInteger) + OtherUsefulFunction.byteArrayToSignedInt(bytesVoltageDecimal) / xPrecision,
                    OtherUsefulFunction.byteArrayToSignedInt(bytesCurrentInteger) + OtherUsefulFunction.byteArrayToSignedInt(bytesCurrentDecimal) / yPrecision
            );
        }

        public HashMap<Integer, Entry> data;

        public LabelData(String LabelName, boolean Show, byte IsDownloaded) {
            labelName = LabelName;
            data = new HashMap<>();
            show = Show;
            isDownloaded = IsDownloaded;
        }
        public LabelData(String LabelName) {
            labelName = LabelName;
            data = new HashMap<>();
        }

        public byte addNewData(Resources resources, byte[] bytes) {
//            Log.e("addNewData: " + OtherUsefulFunction.byteArrayToHexString(bytes, ", "));
            if(bytes == null) {
                // Log.e("Data is null.");
                return 0x00;
            }
            if (type == -1) {
                type = OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[0]});
                switch (type) {
                    case 1:
                    case 2:
                        xLabel = resources.getString(R.string.Voltage);
                        break;
                    case 3:
                    case 4:
                    case 5:
                        xLabel = resources.getString(R.string.Time);
                        break;
                    default:
                        xLabel = resources.getString(R.string.unknown_X_label);
                }
                switch (type) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        yLabel = resources.getString(R.string.Current);
                        break;
                    default:
                        yLabel = resources.getString(R.string.unknown_Y_label);
                }
                switch (type) {
                    case 4:
                    case 5:
                        specialLabel = resources.getString(R.string.Concentration);
                        break;
                    default:
                        specialLabel = yLabel;
                }
                xPrecision = 1000.0f;
                yPrecision = 1000000.0f;
            } else if (type != OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[0]})) {
                Log.e("It is a different Type of Data!");
                return 0x00;
            }

            if (numberOfData == 0) {
                numberOfData = OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[1], bytes[2]});
            } else if (numberOfData != OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[1], bytes[2]})) {
                Log.e("It is a different Number of Data!");
                return 0x00;
            }
//             Log.e("Chart: Size: " + labelName + ": " + String.valueOf(data.values().size()) + ": " + OtherUsefulFunction.byteArrayToHexString(bytes, ", "));
            data.put(
                    OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[3], bytes[4]}),
                    CreateNewEntryByBytes(
                            new byte[]{bytes[5], bytes[6]},
                            new byte[]{bytes[7], bytes[8]},
                            new byte[]{bytes[9], bytes[10]},
                            new byte[]{bytes[11], bytes[12], bytes[13], bytes[14]}
                    )
            );

            // Final data.
            if (isDownloaded == 0x00 && numberOfData == OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[3], bytes[4]})) {
                isDownloaded = 0x01;
                saveMyFile();
                return 0x02;
            }

            return 0x01;
        }
        public void addNewDataList(Resources resources, ArrayList<byte[]> bs) {
            for (byte[] b:bs) {
                addNewData(resources, b);
            }
        }

        public ArrayList<Float> getYByX(float x) {
            ArrayList<Float> yList = new ArrayList<>();
            Entry lowerX = null;
            Entry greaterX = null;
            for (Entry xy : data.values()) {
                byte GES = 0x00;
                if (xy.getX() <= x) {
                    lowerX = new Entry(xy.getX(), xy.getY());
                    GES += 1;
                }
                if (xy.getX() >= x) {
                    greaterX = new Entry(xy.getX(), xy.getY());
                    GES += 2;
                }
                if (lowerX != null && greaterX != null) {
                    yList.add((greaterX.getX() == lowerX.getX()) ? lowerX.getY() : (lowerX.getY() + (x - lowerX.getX()) * (greaterX.getY() - lowerX.getY()) / (greaterX.getX() - lowerX.getX())));
                    if ((GES & 0x1) > 0) {
                        greaterX = null;
                    }
                    if ((GES & 0x2) > 0) {
                        lowerX = null;
                    }
                }
            }
            return yList;
        }

        public float YtoSpecial(float y) {
            switch (type) {
                case 4:
                    return (y - 0.0096f) / 0.0006f;
                case 5:
                    return (y - 17.92f) / 0.6552f;
                default:
                    return y;
            }
        }

        public ArrayList<Float> getSpecialByX(float x) {
            ArrayList<Float> arrayList = new ArrayList<>();
            for (float y : getYByX(x)) {
                arrayList.add(YtoSpecial(y));
            }
            return arrayList;
        }

        public ArrayList<Entry> getSpecialEntries() {
            ArrayList<Entry> arrayList = new ArrayList<>();
            for (Entry xy : data.values()) {
                // Log.d("X: " + String.valueOf(xy.getX()));
                // Log.d("Y: " + String.valueOf(xy.getY()));
                // Log.d("T: " + String.valueOf(type));
                // Log.d("S: " + String.valueOf(String.valueOf(YtoSpecial(xy.getY()))));
                arrayList.add(new Entry(xy.getX(), YtoSpecial(xy.getY())));
            }
            return arrayList;
        }

        public String getAllEntryString() {
            String string = "";
            for (Map.Entry<Integer, Entry> v : data.entrySet()) {
                string += v.getKey() + "; " + v.getValue().toString() + " Special: " + String.valueOf(YtoSpecial(v.getValue().getY())) + "\n";
            }
            return string;
        }

        public HashMap<Integer, ArrayList<Float>> getAllXYSpecial() {
            HashMap<Integer, ArrayList<Float>> hashMap = new HashMap<>();
            for (Map.Entry<Integer, Entry> data : data.entrySet()) {
                ArrayList<Float> d = new ArrayList<Float>();
                d.add(data.getValue().getX());
                d.add(data.getValue().getY());
                d.add(YtoSpecial(data.getValue().getY()));
                hashMap.put(data.getKey(), d);
            }
            return hashMap;
        }

        public void markAsDownloaded() {
            if(isDownloaded != 0x02) {
                isDownloaded = 0x02;
                findDeviceDataByLabelData(this).labelNamingStrategy.next();
            }
        }

        public boolean saveMyFile() {
//            Log.i("saveMyFile: " + labelName);
            try {
                if(OtherUsefulFunction.checkExternalStoragePermission(activity)) {

                    markAsDownloaded();

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH-mm-ss");
                    String currentTime = sdf.format(calendar.getTime());

                    Log.i(labelName);
                    My_Excel_File file = new My_Excel_File();
                    String sdCardPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator;
                    file.createExcelWorkbook(sdCardPath + labelName + ".xls");
                    file.create_new_sheet(labelName);

                    // Add value in the cell
                    Log.i(specialLabel);
                    int rowIndex = 0;
                    file.write_file(0, rowIndex, 0, activity.getResources().getString(R.string.LabelName) + ": " + labelName);
                    rowIndex++;
                    file.write_file(0, rowIndex, 0, activity.getResources().getString(R.string.SaveFileTime) + ": " + currentTime);
                    rowIndex++;
                    file.write_file(0, rowIndex, 0, activity.getResources().getString(R.string.Number));
                    file.write_file(0, rowIndex, 1, xLabel);
                    file.write_file(0, rowIndex, 2, yLabel);
                    file.write_file(0, rowIndex, 3, specialLabel);

                    Log.i(String.valueOf(getAllXYSpecial().entrySet().size()));
                    for (Map.Entry<Integer, ArrayList<Float>> data:getAllXYSpecial().entrySet()) {
                        file.write_file(0, (int) (rowIndex+1+data.getKey()), 0, String.valueOf(data.getKey()));
                        file.write_file(0, rowIndex+1+data.getKey(), 1, String.valueOf(data.getValue().get(0)));
                        file.write_file(0, rowIndex+1+data.getKey(), 2, String.valueOf(data.getValue().get(1)));
                        file.write_file(0, rowIndex+1+data.getKey(), 3, String.valueOf(data.getValue().get(2)));
                    }

                    // Save as Excel XLSX file
                    if (file.exportDataIntoWorkbook()) {
                        Log.i(activity.getResources().getString(R.string.Temp_UI_save_toast));
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, labelName + ": " + activity.getResources().getString(R.string.Temp_UI_save_toast), Toast.LENGTH_SHORT).show();
                            }
                        });
                        return true;
                    }
                }
            } catch (Exception e) {}
            return false;
        }
    }

    public class DeviceData {

        public BLEDataServer.BLEData bleData;

        public MyNamingStrategy labelNamingStrategy = null;
        public String getCreatedLabelName() {
            try {
                return labelNamingStrategy.getName(bleData);
            } catch (Exception e) {
                return null;
            }
        }

        public ArrayList<LabelData> labelData = new ArrayList<>();

        // get dataset Entry for Chart
        public ArrayList<ArrayList<Entry>> getEntryList() {
            ArrayList<ArrayList<Entry>> entryList = new ArrayList<>();
            int index = 0;
            for (LabelData labelData : labelData) {
                entryList.add(new ArrayList<>(labelData.getSpecialEntries()));
                index++;
            }
            return entryList;
        }

        public DeviceData(BLEDataServer.BLEData BleData) {
            bleData = BleData;
        }

        public ArrayList<String> getLabelNameArray() {
            ArrayList<String> arrayList = new ArrayList<>();
            for (LabelData l : labelData) {
                arrayList.add(l.labelName);
            }
            return arrayList;
        }

        public ArrayList<Boolean> getShowArray() {
            ArrayList<Boolean> arrayList = new ArrayList<>();
            for (LabelData l : labelData) {
                arrayList.add(l.show);
                // Log.e(String.valueOf(l.labelName) + ": " + String.valueOf(l.show));
            }
            return arrayList;
        }

        public void removeLabelDataOfBLE(String labelName) {
            for (LabelData l : labelData) {
                if(l.labelName.equals(labelName)) {
                    labelData.remove(l);
                }
            }
        }
    }

    public void updateLabelData(Resources resources, BLEDataServer.BLEData bleData) {
        try {
            findLabelDataByBleAndLabelName(bleData, resources, findDeviceDataByBle(bleData).getCreatedLabelName()).addNewData(resources, bleData.getLastReceivedData(SampleGattAttributes.subscribed_UUIDs.get(0)));
        } catch (Exception e) {}
    }

    public DeviceData findDeviceDataByBle(BLEDataServer.BLEData bleData) {
        int targetIndex = -1;
        for (int i = 0; i< deviceData.size(); i++) {
            if(deviceData.get(i).bleData.equals(bleData)) {
                targetIndex = i;
            }
        }
//        Log.d(String.valueOf(deviceData.size()));
        if(targetIndex == -1) {
            deviceData.add(new CentralDataManager.DeviceData(bleData));
            targetIndex = deviceData.size() - 1;
        }
        return deviceData.get(targetIndex);
    }

    public LabelData findLabelDataByBleAndLabelName(BLEDataServer.BLEData bleData, Resources resources, String labelName) {
//        Log.d("labelName: " + labelName + ", size: " + String.valueOf(deviceData.size()));
        DeviceData targetDevice = findDeviceDataByBle(bleData);
        int targetIndex = -1;
        for (int i = 0; i<targetDevice.labelData.size(); i++) {
//            Log.d("targetDevice.labelData.get(i).labelName: " + String.valueOf(i) + ": " + targetDevice.labelData.get(i).labelName);
            if(targetDevice.labelData.get(i).labelName.equals(labelName)) {
                targetIndex = i;
            }
        }
//        Log.d("targetIndex: " + String.valueOf(targetIndex));
        if(targetIndex == -1) {
            LabelData data = createLabelData(bleData, targetDevice, resources);
            if (targetDevice.labelNamingStrategy != null && data != null) {
                targetDevice.labelData.add(data);
                targetIndex = deviceData.size() - 1;
            } else {
                return null;
            }
        }
        return targetDevice.labelData.get(targetIndex);
    }

    public DeviceData findDeviceDataByLabelData(LabelData labelData) {
        for (DeviceData d:deviceData) {
            for (LabelData data:d.labelData) {
                if(data.equals(labelData)) {
                    return d;
                }
            }
        }
        return null;
    }

    public LabelData createLabelData(BLEDataServer.BLEData bleData, DeviceData targetDevice, Resources resources) {
        byte[] value = bleData.getLastReceivedData(SampleGattAttributes.subscribed_UUIDs.get(0));
        if(value != null) {
            LabelData data = new LabelData(targetDevice.getCreatedLabelName());
            data.addNewData(resources, value);
            return data;
        }
        return null;
    }

}
