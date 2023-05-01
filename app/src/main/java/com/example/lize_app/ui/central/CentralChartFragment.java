package com.example.lize_app.ui.central;

import com.example.lize_app.R;
import com.example.lize_app.adapter.ChartDataMonitorAdapter;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.data.CentralDataManager;
import com.example.lize_app.ui.base.BaseFragment;
import com.example.lize_app.utils.Log;
import com.example.lize_app.utils.MyLineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.List;

import javax.inject.Inject;

//import butterknife.BindView;
//import butterknife.ButterKnife;

public class CentralChartFragment extends BaseFragment implements CentralMvpView {

    Button mZoomOut_button;
    Button mDataMonitorSelector;
    MyLineChart mLineChart;
    EditText mHighlightSelector_edit;
    RecyclerView mDataMonitor;
    ChartDataMonitorAdapter mDataMonitorAdapter;

    @Inject
    CentralPresenter mCentralPresenter;
    public CentralPresenter getCentralPresenter() {
        return mCentralPresenter;
    }

    private final List<BluetoothDevice> mRemoteDevices = new ArrayList<>();

    private static final int READ_VALUES_REPEAT = 1;
    private static final int UPDATE_CHART = 2;
    private final long READING_VALUES_TASK_FREQUENCY = 10;
    private final long UPDATE_CHART_TASK_FREQUENCY = 100;

    private Handler mHandler;
    private void setUpHandler() {
        if(Looper.myLooper() != null) {
            mHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    // Log.e("handleMessage");
                    switch (msg.what) {
                        case READ_VALUES_REPEAT:
                            mCentralPresenter.getRemoteDevices();

                            sendMessageDelayed(
                                obtainMessage(READ_VALUES_REPEAT),
                                READING_VALUES_TASK_FREQUENCY
                            );
                            break;
                        case UPDATE_CHART:
                            updateChart();

                            sendMessageDelayed(
                                    obtainMessage(UPDATE_CHART),
                                    UPDATE_CHART_TASK_FREQUENCY
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

    public void startUpdateChart() {
        if (mHandler.hasMessages(UPDATE_CHART)) {
            return;
        }

        mHandler.sendEmptyMessage(UPDATE_CHART);
    }
    public void stopUpdateChart() {
        mHandler.removeMessages(UPDATE_CHART);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("onAttach");
        getFragmentComponent().inject(this);
        mCentralPresenter.attachView(this);
        mCentralPresenter.setCurrentView(this.getActivity());
        setUpHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("onCreateView");
        View view = inflater.inflate(R.layout.central_chart, container, false);

        mDataMonitorSelector = view.findViewById(R.id.DataMonitorSelector);
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
        mDataMonitorAdapter = new ChartDataMonitorAdapter();
        mDataMonitor = view.findViewById(R.id.DataMonitor);
        mDataMonitor.setAdapter(mDataMonitorAdapter);
        mDataMonitor.setHasFixedSize(true);
        mDataMonitor.setItemAnimator(new DefaultItemAnimator());
        mDataMonitor.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDataMonitorAdapter.setListener(new ChartDataMonitorAdapter.DataItemClickListener() {});

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
        startUpdateChart();
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
        stopUpdateChart();
//        mCentralPresenter.detachView();
    }

    public ArrayList<String> getAllLabelNameArray() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (CentralDataManager.DeviceData d:mCentralPresenter.getDeviceData()) {
            for (String s:d.getLabelNameArray()) {
                arrayList.add(s);
            }
        }
        return arrayList;
    }
    public ArrayList<Boolean> getAllShowArray() {
        ArrayList<Boolean> arrayList = new ArrayList<>();
        for (CentralDataManager.DeviceData d:mCentralPresenter.getDeviceData()) {
            for (boolean b:d.getShowArray()) {
                arrayList.add(b);
            }
        }
        return arrayList;
    }
    public void setAllShowArray(ArrayList<Boolean> booleans) {
        int index = 0;
        for (CentralDataManager.DeviceData d:mCentralPresenter.getDeviceData()) {
            for(CentralDataManager.LabelData l: d.labelData) {
                l.show = booleans.get(index);
                index++;
            }
        }
        mLineChart.setAllShowArray(booleans);
    }
    public void setTypeShowArray(ArrayList<Boolean> booleans) {
        for (CentralDataManager.DeviceData d:mCentralPresenter.getDeviceData()) {
            for(CentralDataManager.LabelData l: d.labelData) {
                if(booleans.get(l.type)) {
                    l.show = true;
                }
                else {
                    l.show = false;
                }
            }
        }
        mLineChart.setAllShowArray(getAllShowArray());
    }
    public void deleteSelectedData(ArrayList<Boolean> booleans) {
        int index = 0;
        for (CentralDataManager.DeviceData d:mCentralPresenter.getDeviceData()) {
            for(CentralDataManager.LabelData l: d.labelData) {
                if(booleans.get(index)) {
                    mCentralPresenter.removeLabelDataOfBLE(d.bleData, l.labelName);
                    iLineDataSets.remove(l);
                }
                index++;
            }
        }
    }
    public void deleteSelectedType(ArrayList<Boolean> booleans) {
        for (CentralDataManager.DeviceData d:mCentralPresenter.getDeviceData()) {
            for(CentralDataManager.LabelData l: d.labelData) {
                if(booleans.get(l.type)) {
                    mCentralPresenter.removeLabelDataOfBLE(d.bleData, l.labelName);
                    iLineDataSets.remove(l);
                }
            }
        }
    }
    public ArrayList<CentralDataManager.LabelData> getAllLabelData() {
        ArrayList<CentralDataManager.LabelData> labelData = new ArrayList<>();
        for (CentralDataManager.DeviceData d:mCentralPresenter.getDeviceData()) {
            for(CentralDataManager.LabelData l: d.labelData) {
                labelData.add(l);
            }
        }
        return labelData;
    }

    ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
    public void updateChart() {
        iLineDataSets.clear();
        for (CentralDataManager.DeviceData deviceData:mCentralPresenter.getDeviceData()) {
            for (final CentralDataManager.LabelData labelData:deviceData.labelData) {
//                Log.d(labelData.getSpecialEntries().toString());
                iLineDataSets.add(new LineDataSet(labelData.getSpecialEntries(), labelData.labelName));
            }
        }
//        Log.d(String.valueOf(iLineDataSets.size()));
        mLineChart.setMyData(new LineData(iLineDataSets));

        mLineChart.setAllShowArray(getAllShowArray());
        mLineChart.refreshChart();

        // ==================================================================================
        // Set selector
        if(mDataMonitorSelector != null) {
            mDataMonitorSelector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getDataMonitorManager().show();
                }
            });
        }
    }

    public void showHighlightedData() {
        // mData_ListView
        if(mLineChart.getHighlighted() == null) {
            if(mDataMonitorAdapter != null) {
                // mLineChart.zoomOut();    // auto zoom out
            }
        } else {
            if(mDataMonitorAdapter != null) {
                mDataMonitorAdapter.setX(mLineChart.getHighlighted()[0].getX());
                mDataMonitorAdapter.clearDatas();
                for (CentralDataManager.LabelData l:getAllLabelData()) {
                    if(l.show) {
                        mDataMonitorAdapter.addData(l);
                    }
                }
                mDataMonitorAdapter.notifyDataSetChanged();
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
        mCentralPresenter.updateLabelData(getResources(), bleData);
        showHighlightedData();
    }

    private AlertDialog getDataMonitorManager() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        String[] typeArray = getResources().getStringArray(R.array.AboutDataMonitorSelector);
        boolean[] typeBooleans = new boolean[typeArray.length];
        for (int i=0; i<typeBooleans.length; i++) {
            typeBooleans[i] = false;
        }

        final AlertDialog dialog = builder
                .setTitle(getResources().getString(R.string.AboutDataMonitorManager))
                .setCancelable(true)
                .setPositiveButton(getResources().getString(android.R.string.cancel), null)
                .setMultiChoiceItems(typeArray, typeBooleans, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                // =====================================================================================
                // MultiChoiceItems
                dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        try {
                            getMyMultipleSelector(i).show();
                        } catch (Exception e) {}
                        dialog.dismiss();
                    }
                });

                // =====================================================================================
                // Cancel
                final Button buttonCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
        return dialog;
    }

    boolean[] selectionArrayBuffer;
    private AlertDialog getMyMultipleSelector(final int type) {
        String Title;
        boolean Cancelable;

        // Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        String[] typeArray = getResources().getStringArray(R.array.AboutDataMonitorSelector);
        Title = typeArray[type];
        Cancelable = true;

        // Build
        String[] labelNameArrayBuffer;
        switch(type) {
            case 0:
            case 2:
                selectionArrayBuffer = new boolean[getAllShowArray().size()];
                labelNameArrayBuffer = getAllLabelNameArray().toArray(new String[0]);
                break;
            case 1:
            case 3:
                selectionArrayBuffer = new boolean[mCentralPresenter.getDataTypes(getResources()).length];
                labelNameArrayBuffer = mCentralPresenter.getDataTypes(getResources());
                break;
            default:
                labelNameArrayBuffer = new String[0];
        }
        if(selectionArrayBuffer.equals(null)) {
            return null;
        }
        for (int i=0; i<selectionArrayBuffer.length; i++) {
            switch(type) {
                case 0:
                    selectionArrayBuffer[i] = getAllShowArray().get(i).booleanValue();
                    break;
                case 1:
                    selectionArrayBuffer[i] = true;
                    break;
                case 2:
                case 3:
                    selectionArrayBuffer[i] = false;
                    break;
                default:
            }
        }
        final AlertDialog dialog = builder
                .setTitle(Title)
                .setCancelable(Cancelable)
                .setPositiveButton(getResources().getString(android.R.string.ok), null)
                .setNegativeButton(getResources().getString(R.string.Fragment_SelectAll), null)
                .setNeutralButton(R.string.Fragment_Reset, null)
                .setMultiChoiceItems(labelNameArrayBuffer, selectionArrayBuffer, null)
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
                        switch(type) {
                            case 0:
                            case 2:
                                for (int i=0; i<getAllShowArray().size(); i++) {
                                    selectionArrayBuffer[i] = getAllShowArray().get(i).booleanValue();
                                    dialog.getListView().setItemChecked(i, selectionArrayBuffer[i]);
                                }
                                break;
                            case 1:
                            case 3:
                                for (int i=0; i<selectionArrayBuffer.length; i++) {
                                    selectionArrayBuffer[i] = true;
                                    dialog.getListView().setItemChecked(i, selectionArrayBuffer[i]);
                                }
                                break;
                            default:
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
                        for (int i=0; i<selectionArrayBuffer.length; i++) {
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
                            case 0:
                                setAllShowArray(bs);
                                break;
                            case 1:
                                setTypeShowArray(bs);
                                break;
                            case 2:
                                deleteSelectedData(bs);
                                break;
                            case 3:
                                deleteSelectedType(bs);
                                break;
                            default:
                        }
                        updateChart();
                        //Dismiss once everything is OK.
                        dialog.dismiss();
                    }
                });
            }
        });

        return dialog;

    }
}
