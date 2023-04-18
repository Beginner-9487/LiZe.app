package com.example.lize_app.ui.central;

import com.example.lize_app.R;
import com.example.lize_app.SampleGattAttributes;
import com.example.lize_app.adapter.ChartDataDisplayerAdapter;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.ui.base.BaseFragment;
import com.example.lize_app.utils.Log;
import com.example.lize_app.utils.MyLineChart;
import com.example.lize_app.utils.OtherUsefulFunction;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

//import butterknife.BindView;
//import butterknife.ButterKnife;

public class CentralChartFragment extends BaseFragment implements CentralMvpView {

    Button mZoomOut_button;
    Button mShowLabelSelector;
    Button mDeleteLabelSelector;
    MyLineChart mLineChart;
    EditText mHighlightSelector_edit;
    RecyclerView mDataDisplayer;
    ChartDataDisplayerAdapter mDataDisplayerAdapter;

    @Inject
    CentralPresenter mCentralPresenter;
    public CentralPresenter getCentralPresenter() {
        return mCentralPresenter;
    }

    private final List<BluetoothDevice> mRemoteDevices = new ArrayList<>();

    private static final int READ_VALUES_REPEAT = 1;
    private final long READING_VALUES_TASK_FREQENCY = 100;

    private Handler mHandler;
    private void setUpHandler() {
        if(Looper.myLooper() != null) {
            mHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    // Log.e("handleMessage");
                    switch (msg.what) {
                        case READ_VALUES_REPEAT:
                            synchronized (CentralChartFragment.this) {
                                mCentralPresenter.getRemoteDevices();
                            }

                            sendMessageDelayed(
                                    obtainMessage(READ_VALUES_REPEAT),
                                    READING_VALUES_TASK_FREQENCY
                            );
                            break;
                    }
                }
            };
        }
    }

    public void startReadValues() {
        if (mHandler.hasMessages(READ_VALUES_REPEAT)) {
            return;
        }

        mHandler.sendEmptyMessage(READ_VALUES_REPEAT);
    }

    public void stopReadValues() {
        mHandler.removeMessages(READ_VALUES_REPEAT);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("onAttach");
        getFragmentComponent().inject(this);
        mCentralPresenter.attachView(this);
        setUpHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("onCreateView");
        View view = inflater.inflate(R.layout.central_chart, container, false);

        mShowLabelSelector = view.findViewById(R.id.ShowLabelSelector);
        mDeleteLabelSelector = view.findViewById(R.id.DeleteLabelSelector);
        mZoomOut_button = view.findViewById(R.id.ZoomOut_Button);
        mZoomOut_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeHighlight();
                mLineChart.zoomOut();
            }
        });
        mLineChart = view.findViewById(R.id.LineChart);
        mLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHighlightedData();
            }
        });
        mLineChart.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // Check if the view's size has changed
                if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
                    // View size has changed, do something
                    // For example, update the layout or perform some calculations
                    mLineChart.refreshChart();
                    // Log.e(String.valueOf(left) + ", " + top + ", " + right + ", " + bottom + ", " + oldLeft + ", " + oldTop + ", " + oldRight + ", " + oldBottom);
                }
            }
        });
        mHighlightSelector_edit = view.findViewById(R.id.HighlightSelector);
        mHighlightSelector_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    setHighlight(Float.valueOf(charSequence.toString()));
                } catch (Exception e) {
                    Log.e(e.getMessage());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        mDataDisplayerAdapter = new ChartDataDisplayerAdapter();
        mDataDisplayer = view.findViewById(R.id.DataDisplayer);
        mDataDisplayer.setAdapter(mDataDisplayerAdapter);
        mDataDisplayer.setHasFixedSize(true);
        mDataDisplayer.setItemAnimator(new DefaultItemAnimator());
        mDataDisplayer.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDataDisplayerAdapter.setListener(new ChartDataDisplayerAdapter.DataItemClickListener() {});

        return view;
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("onResume");
        startReadValues();
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        Log.d("onPause");
//    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d("onStop");
        stopReadValues();
//        mCentralPresenter.detachView();
    }

    public class LabelData {
        // TODO label
        public String labelName;
        public boolean show = true;
        public boolean isDownloaded = false;
        public int type = -1;
        public int numberOfDatas = 0;
        public String xLabel;
        public String yLabel;
        public float xPrecision;
        public float yPrecision;
        public class XY {
            float x;
            float y;
            public XY(byte[] bytesVoltageInteger, byte[] bytesVoltageDecimal, byte[] bytesCurrentInteger, byte[] bytesCurrentDecimal) {
                x = OtherUsefulFunction.byteArrayToSignedInt(bytesVoltageInteger) + OtherUsefulFunction.byteArrayToSignedInt(bytesVoltageDecimal) / xPrecision;
                y = OtherUsefulFunction.byteArrayToSignedInt(bytesCurrentInteger) + OtherUsefulFunction.byteArrayToSignedInt(bytesCurrentDecimal) / yPrecision;
            }
            public XY(float X, float Y) {
                x = X;
                y = Y;
            }
            @Override
            public String toString() {
                // return "(" + String.valueOf(x) + ", " + String.valueOf(y) + ")";
                return String.valueOf(x) + ", " + String.valueOf(y);
            }
        }
        public HashMap<Integer, XY> datas;
        public LabelData(String LabelName) {
            labelName = LabelName;
            datas = new HashMap<>();
        }
        public void addNewData(byte[] bytes) {
            if(type == -1) {
                type = OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[0]});
                switch (type) {
                    case 1 :
                    case 2 :
                        xLabel = getResources().getString(R.string.Voltage);
                        break;
                    case 3 :
                        xLabel = getResources().getString(R.string.Time);
                        break;
                    default :
                        xLabel = getResources().getString(R.string.unknown_X_label);
                }
                yLabel = getResources().getString(R.string.Current);
                xPrecision = 1000.0f;
                yPrecision = 1000000.0f;
            } else if (type != OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[0]})) {
                Toast.makeText(getContext(), "It is a different Type of data!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(numberOfDatas == 0) {
                numberOfDatas = OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[1], bytes[2]});
            } else if (numberOfDatas != OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[1], bytes[2]})) {
                Toast.makeText(getContext(), "It is a different Number Of Datas!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Log.e("Chart: Size: " + String.valueOf(datas.values().size()));
            datas.put(
                OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[3], bytes[4]}),
                new XY(
                    new byte[]{bytes[5], bytes[6]},
                    new byte[]{bytes[7], bytes[8]},
                    new byte[]{bytes[9], bytes[10]},
                    new byte[]{bytes[11], bytes[12], bytes[13], bytes[14]}
                )
            );

            // Automatically save files when final data is received.
            if(!isDownloaded && numberOfDatas == OtherUsefulFunction.byteArrayToSignedInt(new byte[]{bytes[3], bytes[4]})) {
                isDownloaded = true;
                mCentralPresenter.saveMyFile(labelName, xLabel, yLabel, getAllDataString());
            }

        }
        public ArrayList<Float> getYByX (float x) {
            ArrayList<Float> yList = new ArrayList<>();
            XY lowerX = null;
            XY greaterX = null;
            for (XY xy:datas.values()) {
                if(xy.x <= x) { lowerX = new XY(xy.x, xy.y); }
                if(xy.x >= x) { greaterX = new XY(xy.x, xy.y); }
                if(lowerX != null && greaterX != null) {
                    yList.add((greaterX.x == lowerX.x) ? lowerX.y : (lowerX.y + (x - lowerX.x) * (greaterX.y - lowerX.y) / (greaterX.x - lowerX.x)));
                    lowerX = null;
                    greaterX = null;
                }
            }
            return yList;
        }
        public String getAllDataString() {
            String string = "";
            for (Map.Entry<Integer, XY> v:datas.entrySet()) {
                string += v.getKey() + ": " + v.getValue().toString() + "\n";
            }
            return string;
        }
    }

    public class DeviceData {
        // LabelData
        ArrayList<LabelData> labelDatas = new ArrayList<>();
        // get dataset Entry for Chart
        public ArrayList<ArrayList<Entry>> getEntryList() {
            ArrayList<ArrayList<Entry>> entryList = new ArrayList<>();
            int index = 0;
            for (LabelData labelData:labelDatas) {
                entryList.add(new ArrayList<Entry>());
                for (LabelData.XY v:labelData.datas.values()) {
                    // Log.e(labelData.labelName + ": " + v.toString());
                    entryList.get(index).add(new Entry(v.x, v.y));
                }
                // Log.e(labelData.getAllDataString());
                index++;
            }
            return entryList;
        }
        public DeviceData() { }
        public ArrayList<String> getLabelnameArray() {
            ArrayList<String> arrayList = new ArrayList<>();
            for (LabelData l:labelDatas) {
                arrayList.add(l.labelName);
            }
            return arrayList;
        }
        public ArrayList<Boolean> getShowArray() {
            ArrayList<Boolean> arrayList = new ArrayList<>();
            for (LabelData l:labelDatas) {
                arrayList.add(l.show);
                // Log.e("getShowArray: " + String.valueOf(l.labelName) + ": " + String.valueOf(l.show));
            }
            return arrayList;
        }
        public HashMap<String, Boolean> getShowHash() {
            HashMap<String, Boolean> hashMap = new HashMap<>();
            for (LabelData l:labelDatas) {
                hashMap.put(l.labelName, l.show);
            }
            return hashMap;
        }
        public HashMap<String, Boolean> getDownloadHash() {
            HashMap<String, Boolean> hashMap = new HashMap<>();
            for (LabelData l:labelDatas) {
                // Log.d(String.valueOf(labelDatas.size()) + ": " + l.labelName + ": " + String.valueOf(l.isDownloaded));
                hashMap.put(l.labelName, l.isDownloaded);
            }
            return hashMap;
        }
    }
    public ArrayList<String> getAllLabelnameArray() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (DeviceData d:deviceDatas.values()) {
            for (String s:d.getLabelnameArray()) {
                arrayList.add(s);
            }
        }
        return arrayList;
    }
    public ArrayList<Boolean> getAllShowArray() {
        ArrayList<Boolean> arrayList = new ArrayList<>();
        for (DeviceData d:deviceDatas.values()) {
            for (boolean b:d.getShowArray()) {
                arrayList.add(b);
            }
        }
        return arrayList;
    }
    public void setAllShowArray(ArrayList<Boolean> booleans) {
        int index = 0;
        for (DeviceData d:deviceDatas.values()) {
            for(LabelData l: d.labelDatas) {
                l.show = booleans.get(index);
                // Log.e("d.labelDatas.size(): " + String.valueOf(d.labelDatas.size()));
                // Log.e("setAllShowArray: " + String.valueOf(booleans.get(index)));
                index++;
            }
        }
        mLineChart.setAllShowArray(booleans);
    }
    public void deleteSelectedData(ArrayList<Boolean> booleans) {
        int index = 0;
        for (Map.Entry<BLEDataServer.BLEData, DeviceData> v:deviceDatas.entrySet()) {
            for(LabelData l: v.getValue().labelDatas) {
                if(booleans.get(index)) {
                    mCentralPresenter.removeDataByLabelname(v.getKey(), l.labelName);
                    lineDataSets.remove(l);
                }
                index++;
            }
        }
    }
    public ArrayList<LabelData> getAllLabelData() {
        ArrayList<LabelData> labelDatas = new ArrayList<>();
        for (DeviceData d:deviceDatas.values()) {
            for(LabelData l: d.labelDatas) {
                labelDatas.add(l);
            }
        }
        return labelDatas;
    }

    LinkedHashMap<BLEDataServer.BLEData, DeviceData> deviceDatas = new LinkedHashMap<>();
    // set Chart
    LinkedHashMap<LabelData, ILineDataSet> lineDataSets = new LinkedHashMap<>();
    public void updataChart(BLEDataServer.BLEData bleData) {
        try {

            // ==================================================================================
            // Updata new data (Skip this step if bleData == null)
            if(bleData != null) {

                // Init
                HashMap<String, Boolean> showHash;
                HashMap<String, Boolean> downloadHash;
                if(!deviceDatas.containsKey(bleData)) {
                    showHash = new HashMap<>();
                    downloadHash = new HashMap<>();
                    deviceDatas.put(bleData, new DeviceData());
                } else {
                    showHash = deviceDatas.get(bleData).getShowHash();  // Get last show information
                    downloadHash = deviceDatas.get(bleData).getDownloadHash();  // Get is downloaded information
                    // Log.d(String.valueOf(showHash.size()));

                    deviceDatas.get(bleData).labelDatas.clear();    // Clear to avoid removed data still exists
                }

                // Update all data contained in BLEData
                for (Map.Entry<BluetoothGattService, HashMap<BluetoothGattCharacteristic, ArrayList<BLEDataServer.BLEData.Dataset>>> s:bleData.Values.entrySet()) {
                    for (Map.Entry<BluetoothGattCharacteristic, ArrayList<BLEDataServer.BLEData.Dataset>> c:s.getValue().entrySet()) {
                        if(SampleGattAttributes.checkSubscribed(String.valueOf(c.getKey().getUuid()))) {
                            for (BLEDataServer.BLEData.Dataset dataset:c.getValue()) {
                                LabelData labelData = new LabelData(dataset.labelname);
                                // Set last show information
                                for (Map.Entry<String, Boolean> e:showHash.entrySet()) {
                                    if(labelData.labelName.equals(e.getKey())) {
                                        labelData.show = e.getValue();
                                    }
                                }
                                // Set is downloaded information
                                for (Map.Entry<String, Boolean> e:downloadHash.entrySet()) {
                                    if(labelData.labelName.equals(e.getKey())) {
                                        labelData.isDownloaded = e.getValue();
                                    }
                                }
                                // Reload all data
                                for (byte[] d:dataset.data) {
                                    labelData.addNewData(d);
                                    // Log.e("Chart: " + OtherUsefulFunction.byteArrayToHexString(d, ", "));
                                }
                                deviceDatas.get(bleData).labelDatas.add(labelData);
                                // Log.e("deviceDatas.get(bleData).labelDatas.size(): " + String.valueOf(deviceDatas.get(bleData).labelDatas.size()));
                            }
                        }
                    }
                }

                // Restart to create all LineDataSet contained in BLEData
                int index = 0;

                ArrayList<ArrayList<Entry>> entryList = deviceDatas.get(bleData).getEntryList();
                // Log.e("entryList.size(): " + String.valueOf(entryList.size()));
                // Log.e("deviceDatas.get(bleData).labelDatas.size(): " + String.valueOf(deviceDatas.get(bleData).labelDatas.size()));
                for (LabelData l:deviceDatas.get(bleData).labelDatas) {

                    // Add data
                    LineDataSet lineDataSet = new LineDataSet(entryList.get(index), l.labelName);

                    // Make sure that the data order of other devices has not changed.
                    LinkedHashMap<LabelData, ILineDataSet> before = new LinkedHashMap<>();
                    LinkedHashMap<LabelData, ILineDataSet> after = new LinkedHashMap<>();
                    boolean lock = true;
                    // Log.e("lineDataSets.keySet().size(): " + String.valueOf(lineDataSets.keySet().size()));
                    for (LabelData labelData:lineDataSets.keySet()) {
                        if(labelData.labelName.equals(l.labelName)) {
                            lock = false;
                        } else if(lock) {
                            before.put(labelData, lineDataSets.get(labelData));
                        } else {
                            after.put(labelData, lineDataSets.get(labelData));
                        }
                    }
                    // Set this dataset
                    before.put(l, lineDataSet);
                    for (LabelData labelData:after.keySet()) {
                        before.put(labelData, after.get(labelData));
                    }
                    lineDataSets = before;
                    // Log.e("lineDataSets.keySet().size(): " + String.valueOf(lineDataSets.keySet().size()));

                    index++;
                }

            }

            // ==================================================================================
            // Start drawing chart
            // Log.e("shownILineDataSet.size(): " + String.valueOf(shownILineDataSet.size()));
            // Log.e("lineDataSets.size(): " + String.valueOf(lineDataSets.size()));
            // Log.e("getDataSetCount(): " + String.valueOf(new LineData(new ArrayList<ILineDataSet>(lineDataSets.values())).getDataSetCount()));
            mLineChart.setMyData(new LineData(new ArrayList<ILineDataSet>(lineDataSets.values())));

            // setAllShowArray
            mLineChart.setAllShowArray(getAllShowArray());

            // Refresh Chart
            mLineChart.refreshChart();

            // ==================================================================================
            // Set selector
            if(mShowLabelSelector != null) {
                mShowLabelSelector.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getMyMultipleSelector(1).show();
                    }
                });
            }
            if(mDeleteLabelSelector != null) {
                mDeleteLabelSelector.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getMyMultipleSelector(2).show();
                    }
                });
            }

            // mLabel_spinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, deviceDatas.get(bleData).getLabelnameArray().toArray()));
            // Log.e("Chart");

        } catch (Exception e) {
//             Log.e("chart ERROR: " + e.getMessage());
        }
    }

    public void showHighlightedData() {
        // mData_ListView
        if(mLineChart.getHighlighted() == null) {
            if(mDataDisplayerAdapter != null) {
                // mLineChart.zoomOut();    // auto zoom out
            }
        } else {
            if(mDataDisplayerAdapter != null) {
                mDataDisplayerAdapter.setX(mLineChart.getHighlighted()[0].getX());
                mDataDisplayerAdapter.clearDatas();
                for (LabelData l:getAllLabelData()) {
                    if(l.show) {
                        mDataDisplayerAdapter.addData(l);
                    }
                }
                mDataDisplayerAdapter.notifyDataSetChanged();
            }
        }
    }
    public void removeHighlight() {
        mLineChart.highlightValue(null);
        showHighlightedData();
    }
    public void setHighlight(float x, int dataSetIndex) {
        mLineChart.highlightValue(x, dataSetIndex);
        showHighlightedData();
    }
    public void setHighlight(float x) {
        mLineChart.highlightValue(x);
        showHighlightedData();
    }

    @Override
    public void showBLEDevice(BluetoothDevice bt) {

    }

    @Override
    public void showBLEData(BLEDataServer.BLEData bleData) {
        updataChart(bleData);
        showHighlightedData();
    }

    boolean[] selectionArrayBuffer;
    private AlertDialog getMyMultipleSelector(final int type) {
        String Title;
        boolean Cancelable;

        // Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        switch (type) {
            case 1:
                Title = getResources().getString(R.string.AboutShowLabelSelector);
                Cancelable = true;
                break;
            case 2:
                Title = getResources().getString(R.string.AboutDeleteLabelSelector);
                Cancelable = true;
                break;
            default:
                Title = "";
                Cancelable = true;
        }

        // Bulid
        selectionArrayBuffer = new boolean[getAllShowArray().size()];
        String[] labelnameArrayBuffer = new String[getAllLabelnameArray().size()];
        for (int i=0; i<getAllShowArray().size(); i++) {
            switch(type) {
                case 1:
                    selectionArrayBuffer[i] = getAllShowArray().get(i).booleanValue();
                    break;
                case 2:
                    selectionArrayBuffer[i] = false;
                    break;
                default:
            }
            labelnameArrayBuffer[i] = getAllLabelnameArray().get(i);
        }
        final AlertDialog dialog = builder
                .setTitle(Title)
                .setCancelable(Cancelable)
                .setPositiveButton(getResources().getString(android.R.string.ok), null)
                .setNegativeButton(getResources().getString(R.string.Fragment_SelectAll), null)
                .setNeutralButton(R.string.Fragment_Reset, null)
                .setMultiChoiceItems(labelnameArrayBuffer, selectionArrayBuffer, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                // =====================================================================================
                // MultiChoiceItems
                dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        // Log.e("selectionArrayBuffer.length: " + String.valueOf(selectionArrayBuffer.length));
                        selectionArrayBuffer[i] = !selectionArrayBuffer[i];
                        for (boolean b: selectionArrayBuffer) {
                            if(b) {
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setText(getResources().getString(R.string.Fragment_ClearAll));
                                break;
                            } else {
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setText(getResources().getString(R.string.Fragment_SelectAll));
                            }
                        }
                    }
                });

                // =====================================================================================
                // Reset
                Button buttonReset = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                buttonReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (int i=0; i<getAllShowArray().size(); i++) {
                            selectionArrayBuffer[i] = getAllShowArray().get(i).booleanValue();
                            dialog.getListView().setItemChecked(i, getAllShowArray().get(i).booleanValue());
                        }
                        for (boolean b: selectionArrayBuffer) {
                            if(b) {
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setText(getResources().getString(R.string.Fragment_ClearAll));
                                break;
                            } else {
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setText(getResources().getString(R.string.Fragment_SelectAll));
                            }
                        }
                    }
                });

                // =====================================================================================
                // Change All
                final Button buttonChangeAll = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                for (boolean b: selectionArrayBuffer) {
                    if(b) {
                        buttonChangeAll.setText(getResources().getString(R.string.Fragment_ClearAll));
                        break;
                    }
                }
                buttonChangeAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean b = buttonChangeAll.getText().equals(getResources().getString(R.string.Fragment_SelectAll));
                        for (int i=0; i<getAllShowArray().size(); i++) {
                            selectionArrayBuffer[i] = b;
                            dialog.getListView().setItemChecked(i, b);
                        }
                        buttonChangeAll.setText((b)?getResources().getString(R.string.Fragment_ClearAll):getResources().getString(R.string.Fragment_SelectAll));
                    }
                });

                // =====================================================================================
                // OK
                Button buttonOk = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<Boolean> bs = new ArrayList<>();
                        for (boolean b: selectionArrayBuffer) {
                            bs.add(b);
                        }
                        switch(type) {
                            case 1:
                                setAllShowArray(bs);
                                break;
                            case 2:
                                deleteSelectedData(bs);
                                break;
                            default:
                        }
                        updataChart(null);
                        //Dismiss once everything is OK.
                        dialog.dismiss();
                    }
                });
            }
        });

        return dialog;

    }
}
