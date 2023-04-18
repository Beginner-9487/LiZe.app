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

import androidx.annotation.Nullable;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.example.lize_app.R;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.ui.central.CentralActivity;
import com.example.lize_app.ui.central.CentralChartFragment;
import com.example.lize_app.ui.central.CentralTempUI;
import com.example.lize_app.ui.central.CentralViewpagerAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(AndroidJUnit4ClassRunner.class)
public class SeniorTest extends CentralChartTest {

    final int CentralTempUIFragmentPosition = 1;
    class PrepareFakeData2 extends CentralChartTest.PrepareFakeData {
        CentralTempUI centralTempUI;
        public PrepareFakeData2(PrepareFakeData temp) {
            this.centralChartFragment = temp.centralChartFragment;
            this.bluetoothGattService = temp.bluetoothGattService;
            this.bluetoothGattCharacteristic = temp.bluetoothGattCharacteristic;
            this.bleData = temp.bleData;
            this.fakeData = temp.fakeData;
        }
    }
    @Override
    public PrepareFakeData2 prepareFakeData() {
        PrepareFakeData2 myFakeData = new PrepareFakeData2(super.prepareFakeData());

        CentralActivity centralActivity = (CentralActivity) currentActivity.get(0);

        myFakeData.centralTempUI = ((CentralTempUI) ((CentralViewpagerAdapter) centralActivity.getViewPager().getAdapter()).getItem(CentralTempUIFragmentPosition));

        onView(withId(R.id.ViewPager))
            .check(matches(isDisplayed()))
            .perform(swipeRight());

        return myFakeData;
    }

    public void testChartWithFakeData(PrepareFakeData2 myFakeData) {

        int dataMAX = 50;

        // FakeData
        myFakeData.fakeData.add(myFakeData.bleData.createNewDataset());
        for(int i=0; i<dataMAX; i++) {
            myFakeData.fakeData.get(myFakeData.fakeData.size()-1).data.add(new byte[]{
                0x03, 0x00, (byte) (dataMAX), 0x00, (byte) (i+1), 0x00, 0x00, (byte) (i*100/256), (byte) (i*100%256), 0x00, 0x00,
                (byte) ((Math.random() - 0.5f) * 254f),
                (byte) ((Math.random() - 0.5f) * 254f),
                (byte) ((Math.random() - 0.5f) * 254f),
                (byte) ((Math.random() - 0.5f) * 254f),
            });
            myFakeData.bleData.Values.get(myFakeData.bluetoothGattService).put(myFakeData.bluetoothGattCharacteristic, myFakeData.fakeData);
            myFakeData.centralChartFragment.showBLEData(myFakeData.bleData);
        }
    }

    @Test
    public void testLiZe() {
        PrepareFakeData2 myFakeData = prepareFakeData();

        for(int i=0; i<3; i++) {
            onView(withId(R.id.DataName_Text))
                    .check(matches(isDisplayed()))
                    .perform(clearText())
                    .perform(typeText("LiZe" + String.valueOf(i)));

            onView(withId(R.id.ViewPager))
                    .check(matches(isDisplayed()))
                    .perform(swipeLeft());

            myFakeData.bleData.labelNameBuffer = myFakeData.centralTempUI.myNamingStrategy.getName();
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
        PrepareFakeData2 myFakeData = prepareFakeData();

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
            myFakeData.bleData.labelNameBuffer = myFakeData.centralTempUI.myNamingStrategy.getName();
            testChartWithFakeData(myFakeData);
        }

        while (true) {}
    }

}
