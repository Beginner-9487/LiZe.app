package com.example.lize_app.ui.main;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import androidx.test.espresso.intent.Intents;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.example.lize_app.R;
import com.example.lize_app.SampleGattAttributes;
import com.example.lize_app.adapter.LeDeviceAdapter;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.ui.central.CentralActivity;
import com.example.lize_app.ui.central.CentralChartFragment;
import com.example.lize_app.ui.central.CentralScanFragment;
import com.example.lize_app.ui.central.CentralTempUI;
import com.example.lize_app.ui.central.CentralViewpagerAdapter;
import com.example.lize_app.utils.BLEIntents;
import com.example.lize_app.utils.Log;
import com.example.lize_app.utils.OtherUsefulFunction;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4ClassRunner.class)
public class CentralChartTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    @Before
    public void setUp(){
        Intents.init();
    }

    ArrayList<Activity> currentActivity = null;
    private void getActivityInstance(final Stage stage) {
        currentActivity = null;
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                currentActivity = new ArrayList<>(ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage));
            }
        });
    }

    public void centralInit(){
        onView(withId(R.id.Central_Button))
                .check(matches(isDisplayed()))
                .perform(click());

        Intents.intended(hasAction(BLEIntents.ACTION_CENTRAL_MODE));

        // Wait for the activity to start
        getActivityInstance(Stage.RESUMED);
    }

    public void sendCommand(String[] strings) {
        for(int i=0; i<strings.length; i++) {
            onView(withId(R.id.Command_Edit))
                    .check(matches(isDisplayed()))
                    .perform(typeText(strings[i]));
            onView(withId(R.id.C3_Send_Button))
                    .check(matches(isDisplayed()))
                    .perform(click());
        }
    }

    @Test
    public void testInteger() {
        byte[] bytes = new byte[10];
        for (int i = 0; i < 5; i++) {
            bytes[i*2] = ((byte) (i * 100 / 256));
            bytes[i*2+1] = ((byte) (i * 100 % 256));
        }
        assertEquals("00, 00, 00, 64, 00, c8, 01, 2c, 01, 90, ", OtherUsefulFunction.byteArrayToHexString(bytes, ", "));
    }

    final int CentralScanFragmentPosition = 0;
    final int CentralTempUIFragmentPosition = 1;
    final int CentralChartFragmentPosition = 2;
    class PrepareFakeData {
        CentralTempUI centralTempUI;
        CentralChartFragment centralChartFragment;
        BluetoothGattService bluetoothGattService;
        BluetoothGattCharacteristic bluetoothGattCharacteristic;
        BLEDataServer.BLEData bleData;
        public PrepareFakeData() {}
    }
    public PrepareFakeData prepareFakeData() {
        centralInit();

        // Verify that the correct activity is started
        assertEquals(currentActivity.get(0).getComponentName().getClassName(), CentralActivity.class.getName());
        CentralActivity centralActivity = (CentralActivity) currentActivity.get(0);

        onView(withId(R.id.ViewPager))
                .check(matches(isDisplayed()))
                .perform(swipeLeft())
                .perform(swipeLeft());

        onView(withId(R.id.LineChart))
                .check(matches(isDisplayed()));

        CentralTempUI centralTempUI = ((CentralTempUI) ((CentralViewpagerAdapter) centralActivity.getViewPager().getAdapter()).getItem(CentralTempUIFragmentPosition));
        CentralChartFragment centralChartFragment = ((CentralChartFragment) ((CentralViewpagerAdapter) centralActivity.getViewPager().getAdapter()).getItem(CentralChartFragmentPosition));
//        centralChartFragment.stopReadValues();

        // Device1
        BLEDataServer.BLEData bleData = centralChartFragment.getCentralPresenter().getBLEDataServer().findBLEDataByDevice(null);
        BluetoothGattService bluetoothGattService = new BluetoothGattService(UUID.randomUUID(), 0);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(SampleGattAttributes.subscribed_UUIDs.get(0)), 0, 0);

        PrepareFakeData prepareToReturn = new PrepareFakeData();
        prepareToReturn.centralTempUI = centralTempUI;
        prepareToReturn.centralChartFragment = centralChartFragment;
        prepareToReturn.bluetoothGattService = bluetoothGattService;
        prepareToReturn.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
        prepareToReturn.bleData = bleData;
        prepareToReturn.bleData.lastReceivedData = new HashMap<>();
        prepareToReturn.bleData.lastReceivedData.put(bluetoothGattService, new HashMap<BluetoothGattCharacteristic, ArrayList<byte[]>>());
        prepareToReturn.bleData.lastReceivedData.get(bluetoothGattService).put(bluetoothGattCharacteristic, new ArrayList<byte[]>());

        return prepareToReturn;

    }

    final String DeviceName = "CC2642_AD5941_ZEE";
    public void connectToMyDevice(String deviceName){
        centralInit();

        // Verify that the correct activity is started
        assertEquals(currentActivity.get(0).getComponentName().getClassName(), CentralActivity.class.getName());
        CentralActivity centralActivity = (CentralActivity) currentActivity.get(0);

        boolean lock = true;
        while(lock) {
            LeDeviceAdapter leDeviceAdapter;
            leDeviceAdapter = ((CentralScanFragment) ((CentralViewpagerAdapter) centralActivity.getViewPager().getAdapter()).getItem(CentralScanFragmentPosition)).getmLeDeviceAdapter();
            try {
                for (int i=0; i<leDeviceAdapter.getItemCount(); i++) {
                    Log.e("LeDeviceAdapter: " + String.valueOf(i) + ":1: " + String.valueOf(leDeviceAdapter.getBtDeviceAtIndex(i).getName()));
                    Log.e("LeDeviceAdapter: " + String.valueOf(i) + ":2: " + String.valueOf(leDeviceAdapter.getBtDeviceAtIndex(i).getName() != null));
                    if(leDeviceAdapter.getBtDeviceAtIndex(i).getName() != null && leDeviceAdapter.getBtDeviceAtIndex(i).getName().equals(deviceName)) {
                        ((CentralScanFragment) ((CentralViewpagerAdapter) centralActivity.getViewPager().getAdapter()).getItem(CentralScanFragmentPosition)).connectGatt(leDeviceAdapter.getBtDeviceAtIndex(i), true);
                        lock = false;
                        break;
                    }
                }
            } catch (Exception e){
                Log.e("Exception: " + String.valueOf(leDeviceAdapter.getItemCount()));
                Log.e("Exception: " + String.valueOf(leDeviceAdapter.getBtDeviceAtIndex(leDeviceAdapter.getItemCount()-2).getName()));
                Log.e("Exception: " + e.getMessage());
            }
        }
    }

    public void sendThenChart(String DeviceName, String[] strings) {
        connectToMyDevice(DeviceName);

        onView(withId(R.id.ViewPager))
                .check(matches(isDisplayed()))
                .perform(swipeLeft());

        sendCommand(strings);

        onView(withId(R.id.ViewPager))
                .check(matches(isDisplayed()))
                .perform(swipeLeft());
        pressBack();

        while (true) {}
    }

    @Test
    public void sendToRealDevice() {
        sendThenChart(DeviceName, new String[]{"20", "03"});
    }

    @Test
    public void testChartWithRealDevice() {
        sendThenChart(DeviceName, new String[]{"70", "01"});
    }

    @Test
    public void testChartWithRealDevice_2023_04_29() { sendThenChart(DeviceName, new String[]{"60", "01"}); }
}
