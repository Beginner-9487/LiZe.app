package com.example.lize_app.ui.central;

import com.example.lize_app.R;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.ui.base.BaseFragment;
import com.example.lize_app.utils.Log;
import com.example.lize_app.utils.My_Excel_File;
import com.example.lize_app.utils.OtherUsefulFunction;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class CentralTempUI extends BaseFragment implements CentralMvpView {

    EditText command_edit;
    Button c3_btn;
    TextView data_text;
    EditText filename_text;
    Button save_btn;

    HashMap<BluetoothDevice, HashMap<BluetoothGattService, HashMap<BluetoothGattCharacteristic, ArrayList<byte[]>>>> mData = new HashMap<>();

    @Inject
    CentralPresenter mCentralPresenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getFragmentComponent().inject(this);
        mCentralPresenter.attachView(this);
        mCentralPresenter.attach_for_Data();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        com.example.lize_app.utils.Log.d("onCreateView");
        View view = inflater.inflate(R.layout.central_temp_ui, container, false);

        command_edit = view.findViewById(R.id.Command_Edit);
        c3_btn = view.findViewById(R.id.C3_Send_Button);
        data_text = view.findViewById(R.id.Data_Text);
        filename_text = view.findViewById(R.id.FileName_Edit);
        save_btn = view.findViewById(R.id.Save_Excel_Button);

        c3_btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCentralPresenter.Send_All_C(hexStringToByteArray(command_edit.getText().toString()));
                command_edit.setText("");
            }
        });

        save_btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkExternalStoragePermission()) {

                    mCentralPresenter.getRemoteDevices();

                    My_Excel_File file = new My_Excel_File();
                    String sdCardPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator;
                    file.createExcelWorkbook(sdCardPath + filename_text.getText());
                    file.create_new_sheet("Test");

                    // Test
                    file.write_file(0, 0, 0, "Test");

                    // Add value in the cell
                    int index_devices = 1;
                    int cursor = 0;
                    Log.e("mData.size(): " + String.valueOf(mData.size()));
                    for (Map.Entry<BluetoothDevice, HashMap<BluetoothGattService, HashMap<BluetoothGattCharacteristic, ArrayList<byte[]>>>> data:mData.entrySet()) {
                        file.write_file(0, 1, cursor, "Device" + String.valueOf(index_devices) + ": " + data.getKey().getName());
                        int index_service = 1;
                        Log.e("mData.data.size(): " + String.valueOf(data.getValue().size()));
                        for (Map.Entry<BluetoothGattService, HashMap<BluetoothGattCharacteristic, ArrayList<byte[]>>>s:data.getValue().entrySet()) {
                            file.write_file(0, 2, cursor, "S" + String.valueOf(index_service));
                            int index_characteristic = 1;
                            Log.e("mData.data.s.size(): " + String.valueOf(s.getValue().size()));
                            for (Map.Entry<BluetoothGattCharacteristic, ArrayList<byte[]>>c:s.getValue().entrySet()) {
                                file.write_file(0, 3, cursor, "C" + String.valueOf(index_characteristic));
                                Log.e("mData.data.s.c.size(): " + String.valueOf(c.getValue().size()));
                                for(int k=0; k<c.getValue().size(); k++) {
                                    file.write_file(0, 4+k, cursor, byteArrayToHexStringTo(c.getValue().get(k)));
                                }
                                cursor++;
                            }
                            index_characteristic++;
                        }
                        index_service++;
                    }

                    // Save as Excel XLSX file
                    if (file.exportDataIntoWorkbook()) {
                        Toast.makeText(getView().getContext(), R.string.Temp_UI_save_toast, Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

        return view;
    }

    @Override
    public void showBLEDevice(BluetoothDevice bt) {

    }

    @Override
    public void showBLEData(BLEDataServer.BLEData data) {
        Log.e(data.device.getName());
        mData.put(data.device, data.Values);
//        data_text.setText(data.device.getName());
//        data_text.setText(String.valueOf(new Date().getTime()));
    }

    public byte[] hexStringToByteArray(String s) {

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;

    }

    public String byteArrayToHexStringTo(byte[] bytes) {

        String s = "";
        for (byte b:bytes) {
            s += ((b<0x10)?"0":"") + Integer.toHexString(b) + ", ";
        }
        return s;
    }

    private boolean checkExternalStoragePermission() {

        return OtherUsefulFunction.checkPermissionList(
                getActivity(),
                true,
                getResources().getString(R.string.ExternalStoragePermissionAgreeFragment),
                new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },
                0,
                getActivity().getSupportFragmentManager(),
                getResources().getString(R.string.ExternalStoragePermissionTag)
        );

    }

}

