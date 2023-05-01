package com.example.lize_app.ui.main;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.example.lize_app.R;
import com.example.lize_app.SampleGattAttributes;
import com.example.lize_app.ui.central.CentralActivity;
import com.example.lize_app.ui.central.CentralPresenter;
import com.example.lize_app.ui.central.CentralTempUI;
import com.example.lize_app.ui.central.CentralViewpagerAdapter;
import com.example.lize_app.utils.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.UUID;

@RunWith(AndroidJUnit4ClassRunner.class)
public class SeniorTest extends CentralChartTest {

    @Override
    public PrepareFakeData prepareFakeData() {
        PrepareFakeData myFakeData = super.prepareFakeData();

        onView(withId(R.id.ViewPager))
            .check(matches(isDisplayed()))
            .perform(swipeRight());

        return myFakeData;
    }

    public void testChartWithFakeData(PrepareFakeData myFakeData) {

        int dataMAX = 50;

        for(int i=0; i<dataMAX; i++) {
            myFakeData.bleData.lastReceivedData.get(
                myFakeData.bluetoothGattService).get(
                    myFakeData.bluetoothGattCharacteristic).add(
                    new byte[]{
                            0x03, 0x00, (byte) (dataMAX), 0x00, (byte) (i+1), 0x00, 0x00, (byte) (i*100/256), (byte) (i*100%256), 0x00, 0x00,
                            (byte) ((Math.random() - 0.5f) * 254f),
                            (byte) ((Math.random() - 0.5f) * 254f),
                            (byte) ((Math.random() - 0.5f) * 254f),
                            (byte) ((Math.random() - 0.5f) * 254f),
                    }
            );
            myFakeData.centralChartFragment.showBLEData(myFakeData.bleData);
        }
    }

    @Test
    public void testLiZe() {
        PrepareFakeData myFakeData = prepareFakeData();

        for(int i=0; i<3; i++) {
            onView(withId(R.id.DataName_Text))
                    .check(matches(isDisplayed()))
                    .perform(clearText())
                    .perform(typeText("LiZe" + String.valueOf(i)));

            onView(withId(R.id.ViewPager))
                    .check(matches(isDisplayed()))
                    .perform(swipeLeft());

            testChartWithFakeData(myFakeData);

            onView(withId(R.id.ViewPager))
                    .check(matches(isDisplayed()))
                    .perform(swipeRight());
        }

        onView(withId(R.id.ViewPager))
                .check(matches(isDisplayed()))
                .perform(swipeLeft());

        while (true) {}
    }

    @Test
    public void testXieZhiLong() {
        PrepareFakeData myFakeData = prepareFakeData();

        onView(withId(R.id.RadioXieZhiLong))
                .perform(click());
        onView(withId(R.id.FileIndexLimit_Edit))
                .check(matches(isDisplayed()))
                .perform(clearText())
                .perform(typeText("3"));

        onView(withId(R.id.DataName_Text))
                .check(matches(isDisplayed()))
                .perform(clearText())
                .perform(typeText("XZL"));

        onView(withId(R.id.ViewPager))
                .check(matches(isDisplayed()))
                .perform(swipeLeft());

        for(int i=0; i<5; i++) {
            testChartWithFakeData(myFakeData);
        }

        while (true) {}
    }

}
