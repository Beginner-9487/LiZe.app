package com.example.lize_app.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;

import androidx.core.graphics.ColorUtils;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class MyLineChart extends LineChart {

    public MyLineChart(Context context) {
        super(context);
    }

    public MyLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        getLegend().setStackSpace(0.0f);    // 才不會使多條線段組合的這個方法露餡
    }

    LineData splitedData = new LineData();
    ArrayList<ArrayList<Entry>> entryList = new ArrayList<>();
    ArrayList<Integer> groupList = new ArrayList<>();
    synchronized public void setMyData(LineData data) {

        // Log.e("data.getDataSetCount(): " + String.valueOf(data.getDataSetCount()));

        splitedData = new LineData();
        entryList = new ArrayList<>();
        groupList = new ArrayList<>();

        Boolean isAscending = null;

        // ==================================================================================
        // Get splitedData
        // Log.e("data.getDataSets().size(): " + String.valueOf(data.getDataSets().size()));
        for (int i = 0; i < data.getDataSets().size(); i++) {

            // Log.e("i: " + String.valueOf(i));

            groupList.add(entryList.size());

            ArrayList<Entry> temp = new ArrayList<>();
            temp.add(data.getDataSets().get(i).getEntryForIndex(0));
            isAscending = null;
            for (int j = 1; j < data.getDataSets().get(i).getEntryCount(); j++) {
                // Log.e(j + ": " + entryList.size() + ": " + entryList.toString());
                // Log.e("i: j:j-1: " + String.valueOf(i) + ": " + String.valueOf(isAscending) + ": " + String.valueOf(data.getDataSets().get(i).getEntryForIndex(j).getX()) + ": " + String.valueOf(data.getDataSets().get(i).getEntryForIndex(j-1).getX()));
                if(j == data.getDataSets().get(i).getEntryCount() - 1 && temp.size() > 0) {
                    // Log.e("Final: " + String.valueOf(isAscending) + ": " + temp.toString());
                    entryList.add(new ArrayList<Entry>());
                    for (Entry e:temp) {
                        entryList.get(entryList.size()-1).add(e);
                    }
                    // Log.e("Final: " + String.valueOf(isAscending) + ": " + entryList.toString());
                    break;
                } else if (data.getDataSets().get(i).getEntryForIndex(j).getX() > data.getDataSets().get(i).getEntryForIndex(j-1).getX()) {
                    if (isAscending != null && isAscending.booleanValue() == false) {
                        // Log.e(String.valueOf(isAscending) + ": " + temp.toString());
                        entryList.add(new ArrayList<Entry>());
                        for (Entry e:temp) {
                            entryList.get(entryList.size()-1).add(e);
                        }
                        // Log.e(String.valueOf(isAscending) + ": " + entryList.toString());
                        j--;
                        temp = new ArrayList<>();
                    }
                    isAscending = true;
                    temp.add(data.getDataSets().get(i).getEntryForIndex(j));
                } else if (data.getDataSets().get(i).getEntryForIndex(j).getX() < data.getDataSets().get(i).getEntryForIndex(j-1).getX()) {
                    if (isAscending != null && isAscending.booleanValue() == true) {
                        // Log.e(String.valueOf(isAscending) + ": " + temp.toString());
                        entryList.add(new ArrayList<Entry>());
                        for (Entry e:temp) {
                            entryList.get(entryList.size()-1).add(e);
                        }
                        // Log.e(String.valueOf(isAscending) + ": " + entryList.toString());
                        j--;
                        temp = new ArrayList<>();
                    }
                    isAscending = false;
                    // Log.e("1: " + entryList.size() + ": " + temp.toString());
                    temp.add(0, data.getDataSets().get(i).getEntryForIndex(j)); // reverse order
                    // Log.e("2: " + entryList.size() + ": " + temp.toString());
                } else {
                    // Entry(j) == Entry(j-1)
                    if (isAscending != null && isAscending.booleanValue() == true) {
                        temp.add(data.getDataSets().get(i).getEntryForIndex(j));
                    } else {
                        temp.add(0, data.getDataSets().get(i).getEntryForIndex(j));
                    }
                }
            }
        }

        // Log.e("groupList: entryList.size(): " + String.valueOf(groupList.size()) + ": " + String.valueOf(entryList.size()));
        // Log.e("groupList: " + groupList.toString());
        // Log.e(entryList.toString());
        // for (ArrayList<Entry> entries:entryList) {
            // Log.e(entries.toString());
        // }

        // ==================================================================================
        // Init
        int dataIndex = 0;
        ArrayList<ILineDataSet> iLineDataSet = new ArrayList<>();
        for (int i=0; i<entryList.size(); i++) {

            // Log.e("i: dataIndex: " + String.valueOf(i) + ": " + String.valueOf(dataIndex));

            if(dataIndex < (groupList.size()-1) && i == groupList.get(dataIndex+1)) {
                dataIndex++;
            }

            iLineDataSet.add(new LineDataSet(entryList.get(i), data.getDataSetLabels()[dataIndex]));

            Float line_width = 1.8f;
            ((LineDataSet) iLineDataSet.get(iLineDataSet.size()-1)).setMode(LineDataSet.Mode.CUBIC_BEZIER);
            ((LineDataSet) iLineDataSet.get(iLineDataSet.size()-1)).setCubicIntensity(0.2f);
            ((LineDataSet) iLineDataSet.get(iLineDataSet.size()-1)).setDrawCircles(false);
            ((LineDataSet) iLineDataSet.get(iLineDataSet.size()-1)).setLineWidth(line_width);
            ((LineDataSet) iLineDataSet.get(iLineDataSet.size()-1)).setHighlightLineWidth(line_width);
            ((LineDataSet) iLineDataSet.get(iLineDataSet.size()-1)).setDrawHorizontalHighlightIndicator(false);

            ((LineDataSet) iLineDataSet.get(iLineDataSet.size()-1)).setHighLightColor(ColorUtils.HSLToColor(new float[]{ (255.0f * dataIndex/groupList.size()), 1.0f, (((getResources().getConfiguration().uiMode & 48) == Configuration.UI_MODE_NIGHT_YES)?1.0f:0.0f)}));
            ((LineDataSet) iLineDataSet.get(iLineDataSet.size()-1)).setColor(ColorUtils.HSLToColor(new float[]{ (255.0f * dataIndex/groupList.size()), 1.0f, 0.5f}));
        }
        splitedData = new LineData(new ArrayList<ILineDataSet>(iLineDataSet));

        setAllShowArray();
        refreshChart();
    }

    ArrayList<Boolean> shownArray = new ArrayList<>();
    LineData finalShownData = new LineData();
    synchronized public void setAllShowArray(ArrayList<Boolean> ShownArray) {
        shownArray = ShownArray;
        setAllShowArray();
    }
    synchronized public void setAllShowArray() {
        while (shownArray.size() < groupList.size()) {
            shownArray.add(true);
        }
        finalShownData = new LineData();

        // Log.e("booleans.size(): " + String.valueOf(booleans.size()));
        // Log.e("entryList.size(): " + String.valueOf(entryList.size()));
        for (int i=0, j=0; i<entryList.size();) {
            if(j < groupList.size() - 1) {
                while (i < groupList.get(j + 1)) {
                    if(shownArray.get(j).booleanValue()) {
                        finalShownData.addDataSet(((LineDataSet) splitedData.getDataSets().get(i)));
                    }
                    // Log.e("i: b: " + String.valueOf(i) + ": " + String.valueOf(booleans.get(j).booleanValue()));
                    i++;
                }
            } else {
                while (i < entryList.size()) {
                    if(shownArray.get(j).booleanValue()) {
                        finalShownData.addDataSet(((LineDataSet) splitedData.getDataSets().get(i)));
                    }
                    // Log.e("i: b: " + String.valueOf(i) + ": " + String.valueOf(booleans.get(j).booleanValue()));
                    i++;
                }
            }
            j++;
        }

        refreshChart();
    }

    synchronized public void refreshChart() {
        setData(finalShownData);

        // ==================================================================================
        // Set Label
        int dataIndex = 0;
        for (int i=0, j=0; i<entryList.size(); i++) {
            if(dataIndex < (groupList.size()-1) && i == groupList.get(dataIndex+1)) {
                dataIndex++;
            }
            if(shownArray.get(dataIndex).booleanValue()) {
                if(i != groupList.get(dataIndex)) {
                    getLegend().getEntries()[i-j].label = null;
                    getLegend().getEntries()[i-j].form = Legend.LegendForm.NONE;
                }
            } else {
                j++;
            }
        }

        this.invalidate();  // refresh
    }

    public void highlightValue(float x) {
        highlightValue(x, 0);
    }

    @Override
    public void highlightValue(float x, float y, int dataSetIndex, boolean callListener) {
        Float absDiffX = null;
        int index = 0;
        for(int i=0; i<finalShownData.getDataSets().size();i++) {
            for (int j=0; j<finalShownData.getDataSets().get(i).getEntryCount(); j++) {
                if(absDiffX == null) {
                    absDiffX = Math.abs(x - finalShownData.getDataSets().get(i).getEntryForIndex(j).getX());
                } else if (absDiffX > Math.abs(x - finalShownData.getDataSets().get(i).getEntryForIndex(j).getX())) {
                    absDiffX = Math.abs(x - finalShownData.getDataSets().get(i).getEntryForIndex(j).getX());
                    index = i;
                }
            }
        }
        super.highlightValue(x, Float.NaN, index, callListener);
    }

}
