package com.example.lize_app.utils;

import static java.lang.Integer.parseInt;

import androidx.annotation.Nullable;

import com.example.lize_app.data.BLEDataServer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyNamingStrategy {

    public String name = "";
    public int mode = 0;

    private int[] intArray;

    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH-mm-ss");
    String currentTime;

    public String getName(BLEDataServer.BLEData bleData) {
        String finalName = "";
        switch(mode) {
            case 0:
                finalName = this.name;
                break;
            case 1:
                finalName = this.name + "_" + currentTime + "_(" + String.valueOf(intArray[0]) + ")";
                break;
        }
        return finalName;
    }

    public void next() {
        switch(mode) {
            case 0:
                break;
            case 1:
                setCurrentTime();
                intArray[0] = (intArray[0] >= intArray[1]) ? 1 : (intArray[0] + 1);
                break;
        }
    }

    public void setCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        currentTime = sdf.format(calendar.getTime());
    }

    public MyNamingStrategy setNormal(String Name) {
        this.mode = 0;
        this.name = Name;
        return this;
    }

    public MyNamingStrategy setXieZhiLong(String Name, @Nullable Integer CurrentNumber, @Nullable Integer LimitNumber) {
        this.mode = 1;
        this.name = Name;
        CurrentNumber = (CurrentNumber != null) ? CurrentNumber.intValue() : 1;
        LimitNumber = (LimitNumber != null) ? LimitNumber.intValue() : 1;
        intArray = new int[]{CurrentNumber, LimitNumber};
        setCurrentTime();
        return this;
    }

}

