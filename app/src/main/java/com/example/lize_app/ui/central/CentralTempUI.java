package com.example.lize_app.ui.central;

import static java.lang.Integer.parseInt;

import com.example.lize_app.R;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.ui.base.BaseFragment;
import com.example.lize_app.utils.MyNamingStrategy;
import com.example.lize_app.utils.OtherUsefulFunction;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
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


import java.util.Calendar;
import java.util.HashMap;

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
        mCentralPresenter.initForBLEDatas();
        mCentralPresenter.setCentralTempUI(this);
        mCentralPresenter.setCurrentView(this.getActivity());
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
                if(OtherUsefulFunction.checkExternalStoragePermission(getActivity())) {
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

    public void SetNamingStrategy() {
        switch(senior_RadioGroup.getCheckedRadioButtonId()) {
            case R.id.RadioLiZe:
                mCentralPresenter.SetAllNamingStrategy(new MyNamingStrategy().setNormal(dataname_text.getText().toString()));
                break;
            case R.id.RadioXieZhiLong:
                int limit = 1;
                try {
                    limit = parseInt(fileIndexLimit_text.getText().toString());
                } catch (Exception e) {}
                mCentralPresenter.SetAllNamingStrategy(new MyNamingStrategy().setXieZhiLong(dataname_text.getText().toString(), null, limit));
                break;
        }
    }

}

