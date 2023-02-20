package com.example.lize_app.ui.central;

import com.example.lize_app.R;
import com.example.lize_app.SampleGattAttributes;
import com.example.lize_app.adapter.LeDeviceAdapter;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.ui.base.BaseFragment;
import com.example.lize_app.ui.base.BasePresenter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

//import butterknife.BindView;
//import butterknife.ButterKnife;

public class CentralChartFragment extends BaseFragment {

//    @BindView(R.id.LineChart)
    LineChart mLineChart;

//    @BindView(R.id.Data_ListView)
    ListView mData_ListView;

    @Inject
    CentralPresenter mCentralPresenter;

    private final List<BluetoothDevice> mRemoteDevices = new ArrayList<>();

    private static final int READ_VALUES_REPEAT = 1;
    private final long READING_VALUES_TASK_FREQENCY = 2000;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READ_VALUES_REPEAT:
                    synchronized (CentralChartFragment.this) {
                        for (BluetoothDevice device : mRemoteDevices) {
                            mCentralPresenter.readRemoteValues(device);
                        }
                    }
                    updateChart();

                    sendMessageDelayed(
                            obtainMessage(READ_VALUES_REPEAT),
                            READING_VALUES_TASK_FREQENCY
                    );
                    break;
            }
        }
    };

    private void startReadValues() {
        if (mHandler.hasMessages(READ_VALUES_REPEAT)) {
            return;
        }

        mHandler.sendEmptyMessage(READ_VALUES_REPEAT);
    }

    private void stopReadValues() {
        mHandler.removeMessages(READ_VALUES_REPEAT);
    }

    private void updateChart() {

        try {

            // ------------------------------------------------------------------------------
            // get dataset Entry for Chart
            ArrayList<ArrayList<ArrayList<ArrayList<Entry>>>> entry_list = new ArrayList<>();

            // set Chart
            ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

            // set data_list for details
            ArrayList<String> data_list_show = new ArrayList<>();
            String us_string = getResources().getString(R.string.unknown_service);
            String uc_string = getResources().getString(R.string.unknown_characteristic);

            // get dataset
            if(mLineChart.getHighlighted() != null) {
                data_list_show.add("Time: " + String.valueOf(mLineChart.getHighlighted()[0].getX()));
            }
            for(int index_device=0; index_device<mCentralPresenter.getRemoteBLEDatas().size(); index_device++) {

                if(mCentralPresenter.getRemoteBLEDatas().get(index_device).connectedState != BluetoothProfile.STATE_CONNECTED || mCentralPresenter.getRemoteBLEDatas().get(index_device).Values == null) { continue; }

                entry_list.add(new ArrayList<ArrayList<ArrayList<Entry>>>());
                if(mLineChart.getHighlighted() != null) {
                    data_list_show.add(mCentralPresenter.getRemoteBLEDatas().get(index_device).device.getName() + " - " + mCentralPresenter.getRemoteBLEDatas().get(index_device).device.getAddress());
                }
                int index_service = 0;
                for (Map.Entry<BluetoothGattService, HashMap<BluetoothGattCharacteristic, ArrayList<byte[]>>> s:mCentralPresenter.getRemoteBLEDatas().get(index_device).Values.entrySet()) {

                    entry_list.get(index_service).add(new ArrayList<ArrayList<Entry>>());
                    if(mLineChart.getHighlighted() != null) {
                        data_list_show.add("\t" + SampleGattAttributes.lookup(mCentralPresenter.getRemoteBLEDatas().get(index_device).services.get(index_service).getUuid().toString(), us_string));
                    }
                    int index_characteristic = 0;
                    for (Map.Entry<BluetoothGattCharacteristic, ArrayList<byte[]>> c:s.getValue().entrySet()) {

                        entry_list.get(index_service).get(index_characteristic).add(new ArrayList<Entry>());
                        if(mLineChart.getHighlighted() != null) {
                            data_list_show.add("\t\t" + SampleGattAttributes.lookup(mCentralPresenter.getRemoteBLEDatas().get(index_device).services.get(index_service).getCharacteristics().get(index_characteristic).getUuid().toString(), uc_string) + ": " + String.valueOf(mLineChart.getHighlighted()[index_device*index_service+index_characteristic].getY()));
                        }
                        for(int index_time=0; index_characteristic<mCentralPresenter.getRemoteBLEDatas().get(index_device).Values.get(index_service).get(index_characteristic).size(); index_time++) {

                            // TODO byte[] 的處理方式
                            entry_list.get(index_device).get(index_service).get(index_characteristic).add(new Entry(Float.valueOf(index_time), Float.valueOf(String.valueOf(c.getValue().get(index_time)))));
                        }

                        // add data
                        LineDataSet temp = new LineDataSet(entry_list.get(index_device).get(index_service).get(index_characteristic), SampleGattAttributes.lookup(mCentralPresenter.getRemoteBLEDatas().get(index_device).services.get(index_service).getCharacteristics().get(index_characteristic).getUuid().toString(), getResources().getString(R.string.unknown_characteristic)));

                        // add attributes
                        Float line_width = 1.8f;

                        temp.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                        temp.setCubicIntensity(0.2f);   // 似乎是把折線做平滑處理 (數值太高會很像抽象藝術)
                        //temp.setDrawFilled(true);
                        temp.setDrawCircles(false);
                        temp.setLineWidth(line_width);
                        //temp.setCircleRadius(4f);
                        //temp.setCircleColor(Color.BLACK);
                        temp.setHighlightLineWidth(line_width);

                        // TODO Chart Color
                        //Log.e(TAG, "C1: " + String.valueOf(Color.HSVToColor(new float[]{ (Float) (255.0f * i / (i+1)), 255.0f, 255.0f})));
                        temp.setHighLightColor(Color.HSVToColor(new float[]{(Float) (255.0f), 1.0f, 1.0f}));
                        temp.setColor(Color.HSVToColor(new float[]{(Float) (255.0f), 1.0f, 0.5f}));
                        //temp.setFillColor(Color.HSVToColor(new float[]{(Float) (255.0f * i / (i+1)), 0.5f, 1.0f}));
                        //temp.setFillAlpha(100);
                        temp.setDrawHorizontalHighlightIndicator(false);
//                        temp.setFillFormatter(new IFillFormatter() {
//                            @Override
//                            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
//                                return linechart.getAxisLeft().getAxisMinimum();
//                            }
//                        });

                        // set this dataset
                        lineDataSets.add(temp);

                        index_characteristic++;
                    }
                    index_service++;
                }
            }

            // get dataset slices
//            ArrayList<ArrayList<Float>> yy = bluetoothLeService.get_y_axes_list_slices(bluetoothLeService.get_y_axes_list().get(0).size() - 100, bluetoothLeService.get_y_axes_list().get(0).size(), true);
//            ArrayList<Float> xx = bluetoothLeService.get_x_axes_slices(bluetoothLeService.get_y_axes_list().get(0).size() - 100, bluetoothLeService.get_y_axes_list().get(0).size(), true);
//            for(int i=0; i<yy.size(); i++) {
//                entry_list.add(new ArrayList<>());
//                for(int j=0; j<yy.get(i).size(); j++) {
//                    entry_list.get(i).add(new Entry(Float.valueOf(String.valueOf(xx.get(j))), Float.valueOf(String.valueOf(yy.get(i).get(j)))));
//                }
//            }
//            linechart.zoomOut();

            // ------------------------------------------------------------------------------
            // draw chart
            mLineChart.setData(new LineData(lineDataSets));

            // data_ListView
            if(mLineChart.getHighlighted() == null) {
                mLineChart.zoomOut();
//                linechart.zoom(1.0f, 0.0f, linechart.getXChartMax(), ((linechart.getYChartMax() + linechart.getYChartMax()) / 2.0f));
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, (String[]) (data_list_show.toArray(new String[data_list_show.size()])));
                mData_ListView.setAdapter(adapter);
            }

        } catch (Exception e) {
            com.example.lize_app.utils.Log.e("chart ERROR: " + e.getMessage());
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        com.example.lize_app.utils.Log.d("onAttach");
        getFragmentComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        com.example.lize_app.utils.Log.d("onCreateView");
        View view = inflater.inflate(R.layout.central_chart, container, false);
//        ButterKnife.bind(this, view);
        mLineChart = view.findViewById(R.id.LineChart);
        mData_ListView = view.findViewById(R.id.Data_ListView);

        return view;
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//    }

    @Override
    public void onStart() {
        super.onStart();

//        com.example.lize_app.utils.Log.d("onStart");
//        mCentralPresenter.attachView(this);
        startReadValues();
    }

    @Override
    public void onResume() {
        super.onResume();
        com.example.lize_app.utils.Log.d("onResume");
        mCentralPresenter.getRemoteDevices();
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        com.example.lize_app.utils.Log.d("onPause");
//    }

    @Override
    public void onStop() {
        super.onStop();

        com.example.lize_app.utils.Log.d("onStop");
        stopReadValues();
//        mCentralPresenter.detachView();
    }

}
