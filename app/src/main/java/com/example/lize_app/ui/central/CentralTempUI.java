package com.example.lize_app.ui.central;

import static java.lang.Integer.parseInt;

import com.example.lize_app.R;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.ui.base.BaseFragment;
import com.example.lize_app.utils.MyNamingStrategy;
import com.example.lize_app.utils.My_Excel_File;
import com.example.lize_app.utils.OtherUsefulFunction;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class CentralTempUI extends BaseFragment implements CentralMvpView {

    RadioGroup senior_RadioGroup;
    EditText command_edit;
    Button c3_btn;
    EditText dataname_text;
    EditText fileIndexLimit_text;
    //Button save_btn;
    TextView time_text;


    int UPDATE_TIME_FREQENCY = 100;
    long startingClock;
    private Handler mHandler;
    private void setUpHandler() {
        if(Looper.myLooper() != null) {
            mHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    // Log.e("handleMessage");
                    switch (msg.what) {
                        case 0:
                            synchronized (CentralTempUI.this) {
                                time_text.setText(String.valueOf((Calendar.getInstance().getTimeInMillis() - startingClock) / 1000.0f));
                            }

                            sendMessageDelayed(
                                obtainMessage(0),
                                UPDATE_TIME_FREQENCY
                            );
                            break;
                    }
                }
            };
        }
    }
    public void startReadValues() {
        if (mHandler.hasMessages(0)) {
            return;
        }

        mHandler.sendEmptyMessage(0);
    }


    HashMap<BluetoothDevice, BLEDataServer.BLEData> mData = new HashMap<>();

    @Inject
    CentralPresenter mCentralPresenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getFragmentComponent().inject(this);
        mCentralPresenter.attachView(this);
        mCentralPresenter.attach_for_Data();
        mCentralPresenter.setCentralTempUI(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        com.example.lize_app.utils.Log.d("onCreateView");
        View view = inflater.inflate(R.layout.central_temp_ui, container, false);

        senior_RadioGroup = view.findViewById(R.id.SeniorRadioGroup);
        command_edit = view.findViewById(R.id.Command_Edit);
        c3_btn = view.findViewById(R.id.C3_Send_Button);
        dataname_text = view.findViewById(R.id.DataName_Text);
        fileIndexLimit_text = view.findViewById(R.id.FileIndexLimit_Edit);
        //save_btn = view.findViewById(R.id.Save_Excel_Button);
        time_text = view.findViewById(R.id.TimeText);

        startingClock = Calendar.getInstance().getTimeInMillis();

        senior_RadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                SetNamingStrategy();
            }
        });
        c3_btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkExternalStoragePermission()) {
                    SetNamingStrategy();
                    mCentralPresenter.Send_All_C(OtherUsefulFunction.hexStringToByteArray(command_edit.getText().toString()));
                    command_edit.setText("");
                    startingClock = Calendar.getInstance().getTimeInMillis();
                }
            }
        });

        dataname_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SetNamingStrategy();
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });

        fileIndexLimit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SetNamingStrategy();
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });

        setUpHandler();
        startReadValues();

        return view;
    }

    @Override
    public void showBLEDevice(BluetoothDevice bt) {

    }

    @Override
    public void showBLEData(BLEDataServer.BLEData data) {
        // Log.e(data.device.getName());
        mData.put(data.device, data);
        // Log.e(String.valueOf(data.device == null));
        // Log.e(String.valueOf(mData.size()));
        // data_text.setText(data.device.getName());
        // data_text.setText(String.valueOf(new Date().getTime()));
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

    public MyNamingStrategy myNamingStrategy = new MyNamingStrategy();
    public void SetNamingStrategy() {
        switch(senior_RadioGroup.getCheckedRadioButtonId()) {
            case R.id.RadioLiZe:
                myNamingStrategy.setNormal(dataname_text.getText().toString());
                break;
            case R.id.RadioXieZhiLong:
                int limit = 1;
                try {
                    limit = parseInt(fileIndexLimit_text.getText().toString());
                } catch (Exception e) {}
                myNamingStrategy.setXieZhiLong(dataname_text.getText().toString(), null, limit);
                break;
        }
        mCentralPresenter.SetAllNameBuffer(myNamingStrategy.getName());
    }
    public void SetRadioButtonForTest(int Mode) {
        switch(Mode) {
            case 0:
                senior_RadioGroup.setId(R.id.RadioLiZe);
                break;
            case 1:
                senior_RadioGroup.setId(R.id.RadioXieZhiLong);
                break;
        }
    }

    public boolean saveExcelFile(String LabelName, String XLabel, String YLabel, String AllDataString) {

        if(checkExternalStoragePermission()) {

            mCentralPresenter.getRemoteDevices();
            // Log.e("LabelName: " + String.valueOf(LabelName));

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH-mm-ss");
            String currentTime = sdf.format(calendar.getTime());

            My_Excel_File file = new My_Excel_File();
            String sdCardPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator;
            file.createExcelWorkbook(sdCardPath + LabelName + ".xls");
            file.create_new_sheet(LabelName);

            BluetoothDevice bt = null;
            ArrayList<byte[]> rawData = null;
            for (Map.Entry<BluetoothDevice, BLEDataServer.BLEData> e : mData.entrySet()) {
                rawData = e.getValue().getDataByLabelname(LabelName);
                if(rawData != null) {
                    bt = e.getKey();
                    break;
                }
            }
            // Log.e(getResources().getString(R.string.LabelName) + ": " + LabelName + ", " + getResources().getString(R.string.RawData) + ": " + rawData.toString());
            if(rawData == null) {return false;}

            // Add value in the cell
            int rowIndex = 0;
            file.write_file(0, rowIndex, 0, getResources().getString(R.string.DeviceName) + ": " + ((bt!=null)?bt.getName():"Null"));
            rowIndex++;
            file.write_file(0, rowIndex, 0, getResources().getString(R.string.DeviceAddress) + ": " + ((bt!=null)?bt.getAddress():"Null"));
            rowIndex++;
            file.write_file(0, rowIndex, 0, getResources().getString(R.string.LabelName) + ": " + LabelName);
            rowIndex++;
            file.write_file(0, rowIndex, 0, getResources().getString(R.string.SaveFileTime) + ": " + currentTime);
            rowIndex++;
            file.write_file(0, rowIndex, 0, getResources().getString(R.string.Number) + ": ");
            file.write_file(0, rowIndex, 1, XLabel);
            file.write_file(0, rowIndex, 2, YLabel);
            file.write_file(0, rowIndex, 3, getResources().getString(R.string.RawData) + ": ");

            String [] eachData = AllDataString.split("\n");
            for(int i=0; i<eachData.length; i++) {
                int index = parseInt(eachData[i].split(": ")[0]);
                file.write_file(0, rowIndex+1+index, 0, String.valueOf(index));
                file.write_file(0, rowIndex+1+index, 1, eachData[i].split(": ")[1].split(", ")[0]);
                file.write_file(0, rowIndex+1+index, 2, eachData[i].split(": ")[1].split(", ")[1]);
                file.write_file(0, rowIndex+1+index, 3, OtherUsefulFunction.byteArrayToHexString(rawData.get(i), "", "", ""));
            }

            // Save as Excel XLSX file
            if (file.exportDataIntoWorkbook()) {
                // Log.i(getResources().getString(R.string.Temp_UI_save_toast));
                Toast.makeText(getView().getContext(), LabelName + ": " + getResources().getString(R.string.Temp_UI_save_toast), Toast.LENGTH_SHORT).show();
                mCentralPresenter.SetAllNameBuffer(myNamingStrategy.getName());
                return true;
            }

        }

        return false;

    }
}

