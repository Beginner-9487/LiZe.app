package com.example.lize_app.utils;

import static java.lang.Integer.parseInt;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyNamingStrategy {

    public String name = "";
    public int mode = 0;

    private int currentNumber;
    private int limitNumber;

    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH-mm-ss");

    public String getName() {
        Calendar calendar = Calendar.getInstance();
        String finalName = "";
        switch(mode) {
            case 0:
                finalName = this.name;
                break;
            case 1:
                final String currentTime = sdf.format(calendar.getTime());
                finalName = this.name + "_" + currentTime + "_(" + String.valueOf(currentNumber) + ")";
                currentNumber = (currentNumber >= limitNumber) ? 1 : (currentNumber + 1);
                break;
        }
        return finalName;
    }

    public void setNormal(String Name) {
        this.mode = 0;
        this.name = Name;
    }

    public void setXieZhiLong(String Name, @Nullable Integer CurrentNumber, @Nullable Integer LimitNumber) {
        this.mode = 1;
        this.name = Name;
        this.currentNumber = (CurrentNumber != null) ? CurrentNumber.intValue() : 1;
        this.limitNumber = (LimitNumber != null) ? LimitNumber.intValue() : 1;
    }

}

