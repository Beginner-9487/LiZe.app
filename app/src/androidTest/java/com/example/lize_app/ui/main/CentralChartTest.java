package com.example.lize_app.ui.main;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
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
    final int CentralChartFragmentPosition = 2;
    class PrepareFakeData {
        CentralChartFragment centralChartFragment;
        BluetoothGattService bluetoothGattService;
        BluetoothGattCharacteristic bluetoothGattCharacteristic;
        BLEDataServer.BLEData bleData;
        ArrayList<BLEDataServer.BLEData.Dataset> fakeData;
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

        CentralChartFragment centralChartFragment = ((CentralChartFragment) ((CentralViewpagerAdapter) centralActivity.getViewPager().getAdapter()).getItem(CentralChartFragmentPosition));
//        centralChartFragment.stopReadValues();

        // Device1
        BLEDataServer.BLEData bleData = centralChartFragment.getCentralPresenter().getBLEDataServer().findBLEDataByDevice(null);
        bleData.Values = new HashMap<>();
        BluetoothGattService bluetoothGattService = new BluetoothGattService(UUID.randomUUID(), 0);
        bleData.Values.put(bluetoothGattService, new HashMap<BluetoothGattCharacteristic, ArrayList<BLEDataServer.BLEData.Dataset>>());
        BluetoothGattCharacteristic bluetoothGattCharacteristic = new BluetoothGattCharacteristic(UUID.fromString(SampleGattAttributes.subscribed_UUIDs.get(0)), 0, 0);
        ArrayList<BLEDataServer.BLEData.Dataset> fakeData = new ArrayList<>();

        PrepareFakeData prepareToReturn = new PrepareFakeData();
        prepareToReturn.centralChartFragment = centralChartFragment;
        prepareToReturn.bluetoothGattService = bluetoothGattService;
        prepareToReturn.bluetoothGattCharacteristic = bluetoothGattCharacteristic;
        prepareToReturn.bleData = bleData;
        prepareToReturn.fakeData = fakeData;

        return prepareToReturn;

    }

    @Test
    public void testChartWithFakeData() {
        PrepareFakeData myFakeData = prepareFakeData();

        // FakeData
        int fakeIndex = 0;
        myFakeData.bleData.labelNameBuffer = "FakeData" + String.valueOf(fakeIndex);
        myFakeData.fakeData.add(myFakeData.bleData.createNewDataset());
        for(int i=0; i<10; i++) {
            myFakeData.fakeData.get(fakeIndex).data.add(new byte[]{
                    // 0x03, 0x02, 0x58, 0x00, (byte) i, 0x00, 0x00, (byte) (i*100/256), (byte) (i*100%256), 0x00, 0x00,
                    0x03, 0x00, 0x14, 0x00, (byte) (i+1), 0x00, 0x00, (byte) (i*100/256), (byte) (i*100%256), 0x00, 0x00,
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
            });
        }
        for(int i=0; i<10; i++) {
            myFakeData.fakeData.get(fakeIndex).data.add(new byte[]{
                    // 0x03, 0x02, 0x58, 0x00, (byte) (i+10), 0x00, 0x00, (byte) ((9-i)*100/256), (byte) ((9-i)*100%256+100), 0x00, 0x00,
                    0x03, 0x00, 0x14, 0x00, (byte) (i+11), 0x00, 0x00, (byte) ((9-i)*100/256), (byte) ((9-i)*100%256+100), 0x00, 0x00,
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
            });
        }
        // FakeData
        fakeIndex++;
        myFakeData.bleData.labelNameBuffer = "FakeData" + String.valueOf(fakeIndex);
        myFakeData.fakeData.add(myFakeData.bleData.createNewDataset());
        for(int i=0; i<10; i++) {
            myFakeData.fakeData.get(fakeIndex).data.add(new byte[]{
                    0x03, 0x02, 0x58, 0x00, (byte) i, 0x00, 0x00, (byte) (i*100/256), (byte) (i*100%256), 0x00, 0x00,
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
            });
        }
        for(int i=0; i<10; i++) {
            myFakeData.fakeData.get(fakeIndex).data.add(new byte[]{
                    0x03, 0x02, 0x58, 0x00, (byte) (i+10), 0x00, 0x00, (byte) ((9-i)*100/256), (byte) ((9-i)*100%256), 0x00, 0x00,
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
            });
        }
        // FakeData3
        fakeIndex++;
        myFakeData.bleData.labelNameBuffer = "FakeData" + String.valueOf(fakeIndex);
        myFakeData.fakeData.add(myFakeData.bleData.createNewDataset());
        for(int i=0; i<20; i++) {
            myFakeData.fakeData.get(fakeIndex).data.add(new byte[]{
                    0x03, 0x02, 0x58, 0x00, (byte) i, 0x00, 0x00, (byte) (i*100/256), (byte) (i*100%256), 0x00, 0x00,
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
            });
        }
        // FakeData
        fakeIndex++;
        myFakeData.bleData.labelNameBuffer = "FakeData" + String.valueOf(fakeIndex);
        myFakeData.fakeData.add(myFakeData.bleData.createNewDataset());
        for(int i=0; i<10; i++) {
            myFakeData.fakeData.get(fakeIndex).data.add(new byte[]{
                    0x03, 0x02, 0x58, 0x00, (byte) i, 0x00, 0x00, (byte) (i*100/256), (byte) (i*100%256), 0x00, 0x00,
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
            });
        }
        for(int i=0; i<10; i++) {
            myFakeData.fakeData.get(fakeIndex).data.add(new byte[]{
                    0x03, 0x02, 0x58, 0x00, (byte) (i+10), 0x00, 0x00, (byte) ((9-i)*100/256), (byte) ((9-i)*100%256), 0x00, 0x00,
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
                    (byte) ((Math.random() - 0.5f) * 254f),
            });
        }

        myFakeData.bleData.Values.get(myFakeData.bluetoothGattService).put(myFakeData.bluetoothGattCharacteristic, myFakeData.fakeData);
        myFakeData.centralChartFragment.showBLEData(myFakeData.bleData);

        while (true) {}

    }

    public void testRealDataChart(ArrayList<String> ByteStrings) {

        PrepareFakeData myFakeData = prepareFakeData();

        // FakeData
        for (int i=0; i<ByteStrings.size(); i++) {
            String[] byteString = ByteStrings.get(i).split("\n");
            myFakeData.bleData.labelNameBuffer = "FakeData" + String.valueOf(i);
            myFakeData.fakeData.add(myFakeData.bleData.createNewDataset());
            for(int j=0; j<byteString.length; j++) {
                myFakeData.fakeData.get(i).data.add(OtherUsefulFunction.hexStringToByteArray(byteString[j]));
            }
        }

        myFakeData.bleData.Values.get(myFakeData.bluetoothGattService).put(myFakeData.bluetoothGattCharacteristic, myFakeData.fakeData);
        myFakeData.centralChartFragment.showBLEData(myFakeData.bleData);

        while (true) {}
    }

    final String Test2001Data =
        "0100780001025800000000000000000003085350\n" +
        "0100780002024e00000000000000000003085350\n" +
        "0100780003024400000000000000000003085350\n" +
        "0100780004023a00000000000000000003085350\n" +
        "0100780005023000000000000000000003085350\n" +
        "0100780006022600000000000000000003085350\n" +
        "0100780007021c00000000000000000003085350\n" +
        "0100780008021200000000000000000003085350\n" +
        "0100780009020800000000000000000003085350\n" +
        "010078000a01fe00000000000000000003085350\n" +
        "010078000b01f400000000000000000003085350\n" +
        "010078000c01ea00000000000000000003085350\n" +
        "010078000d01e000000000000000000003085350\n" +
        "010078000e01d600000000000000000003085350\n" +
        "010078000f01cc00000000000000000003085350\n" +
        "010078001001c200000000000000000003085350\n" +
        "010078001101b800000000000000000003085350\n" +
        "010078001201ae00000000000000000003085350\n" +
        "010078001301a400000000000000000003085350\n" +
        "0100780014019a00000000000000000003085350\n" +
        "0100780015019000000000000000000003085350\n" +
        "0100780016018600000000000000000003085350\n" +
        "0100780017017c00000000000000000003085350\n" +
        "010078001801720000fff7fff973710003085350\n" +
        "010078001901680000fff6fffde6210003085350\n" +
        "010078001a015e0000fff7fff61fd00003085350\n" +
        "010078001b01540000fff6fff814030003085350\n" +
        "010078001c014a00000000fff107a90003085350\n" +
        "010078001d01400000ffcefff727060003085350\n" +
        "010078001e01360000ffcbfffb58dd0003085350\n" +
        "010078001f012c0000ffcffffb056b0003085350\n" +
        "010078002001220000ffccfff611070003085350\n" +
        "010078002101180000ffc1ffff10270003085350\n" +
        "0100780022010e0000ffc8fffd0b430003085350\n" +
        "010078002301040000ffd4fff217750003085350\n" +
        "010078002400fa0000ffb1fff4b7530003085350\n" +
        "010078002500f00000ffc8ffff89bf0003085350\n" +
        "010078002600e60000ffbdfff7120b0003085350\n" +
        "010078002700dc0000ffb6fff19b010003085350\n" +
        "010078002800d20000ff62fff7915c0003085350\n" +
        "010078002900c80000ff16fffc0c270003085350\n" +
        "010078002a00be0000feadfff91a460003085350\n" +
        "010078002b00b40000fe8afff866080003085350\n" +
        "010078002c00aa0000fe8bfff9c5ef0003085350\n" +
        "010078002d00a00000fe86ffff5fc90003085350\n" +
        "010078002e00960000fe8cfff97c440003085350\n" +
        "010078002f008c0000fe8dfff1b5b50003085350\n" +
        "010078003000820000fe85fffe001e0003085350\n" +
        "010078003100780000fe8dfff85d350003085350\n" +
        "0100780032006e0000fe89fff55c120003085350\n" +
        "010078003300640000fe8dffffd9420003085350\n" +
        "0100780034005a0000fe8ffff61f560003085350\n" +
        "010078003500500000fe8afff6bbfa0003085350\n" +
        "010078003600460000fe91fff9b40e0003085350\n" +
        "0100780037003c0000fe8dfffa06c90003085350\n" +
        "010078003800320000fe8ffffd9b810003085350\n" +
        "010078003900280000fe91fffa88f60003085350\n" +
        "010078003a001e0000fe8bfffdee3c0003085350\n" +
        "010078003b00140000fe91fff80a3d0003085350\n" +
        "010078003c000a0000fe93fff0cf280003085350\n" +
        "010078003d00000000fe91fff9b3ef0003085350\n" +
        "010078003efff60000fe93fff4f7b20003085350\n" +
        "010078003fffec0000fe8afff43d7e0003085350\n" +
        "0100780040ffe20000fe8ffff7c9260003085350\n" +
        "0100780041ffd80000fe8ffff121e30003085350\n" +
        "0100780042ffce0000fe8affffe1b90003085350\n" +
        "0100780043ffc40000fe8efff6693e0003085350\n" +
        "0100780044ffba0000fe8bfff1749e0003085350\n" +
        "0100780045ffb00000fe8dfffadbee0003085350\n" +
        "0100780046ffa60000fee0fffa2cef0003085350\n" +
        "0100780047ff9c0000ff31fff5ea680003085350\n" +
        "0100780048ff920000ff87fff532d30003085350\n" +
        "0100780049ff880000ffdafff7d8690003085350\n" +
        "010078004aff7e0000ffddfffa4e500003085350\n" +
        "010078004bff740000ffdefff5067a0003085350\n" +
        "010078004cff6a0000ffdcfff66f710003085350\n" +
        "010078004dff600000ffdbffffdf940003085350\n" +
        "010078004eff560000ffddffff4b480003085350\n" +
        "010078004fff4c0000ffdefff506f40003085350\n" +
        "0100780050ff420000ffddffff4b480003085350\n" +
        "0100780051ff380000ffdcfff3f0f50003085350\n" +
        "0100780052ff2e0000ffddfffccc520003085350\n" +
        "0100780053ff240000ffdcfffec0ff0003085350\n" +
        "0100780054ff1a0000ffdcfff9c3130003085350\n" +
        "0100780055ff100000ffdefff0de2d0003085350\n" +
        "0100780056ff060000ffddfff6faae0003085350\n" +
        "0100780057fefc0000ffddfff2d1e70003085350\n" +
        "0100780058fef20000ffddfffa4dd50003085350\n" +
        "0100780059fee80000ffddfffbf8200003085350\n" +
        "010078005afede0000ffdefff785ea0003085350\n" +
        "010078005bfed40000ffddfffda1f10003085350\n" +
        "010078005cfeca0000ffdefff506f40003085350\n" +
        "010078005dfec00000ffdefffbadbd0003085350\n" +
        "010078005efeb60000ffddfff8a47f0003085350\n" +
        "010078005ffeac0000ffdefff6b0c50003085350\n" +
        "0100780060fea20000ffdefff92f410003085350\n" +
        "0100780061fe980000ffdcfffd163b0003085350\n" +
        "0100780062fe8e0000ffdefff0dea70003085350\n" +
        "0100780063fe840000ffdcffff95ab0003085350\n" +
        "0100780064fe7a0000ffdefff1b3530003085350\n" +
        "0100780065fe700000ffdefff1b3530003085350\n" +
        "0100780066fe660000ffdcfff9c3130003085350\n" +
        "0100780067fe5c0000ffdefff431cf0003085350\n" +
        "0100780068fe520000ffdffff4bd0b0003085350\n" +
        "0100780069fe480000ffddfffa4e500003085350\n" +
        "010078006afe3e0000ffdefffa04670003085350\n" +
        "010078006bfe340000ffddfffb23750003085350\n" +
        "010078006cfe2a0000ffddfff9792a0003085350\n" +
        "010078006dfe200000ffddfffccd460003085350\n" +
        "010078006efe160000ffddfffe769c0003085350\n" +
        "010078006ffe0c0000ffddfffa4e500003085350\n" +
        "0100780070fe020000ffdcffff96250003085350\n" +
        "0100780071fdf80000ffdefffc82e30003085350\n" +
        "0100780072fdee0000ffddfffa4e500003085350\n" +
        "0100780073fde40000ffddfff550dd0003085350\n" +
        "0100780074fdda0000ffdefffe2cb40003085350\n" +
        "0100780075fdd00000ffdefff5dba00003085350\n" +
        "0100780076fdc60000ffddfff3a70c0003085350\n" +
        "0100780077fdbc0000ffdefff92f410003085350\n" +
        "0100780078fdb20000ffddfffb22fb0003085350";
        // "";

    @Test
    public void test2001() {
        testRealDataChart(new ArrayList<String>(Arrays.asList(new String[]{Test2001Data})));
    }

    final String Test2002Data =
        "0200f00002fdb20000ff60fffde03b0003085350\n" +
        "0200f00003fdbc0000ff61fffc16e20003085350\n" +
        "0200f00004fdc60000ff62fff993cf0003085350\n" +
        "0200f00005fdd00000ff63fff7254c0003085350\n" +
        "0200f00006fdda0000ff64fff4b6c90003085350\n" +
        "0200f00007fde40000ff65fff29af20003085350\n" +
        "0200f00008fdee0000ff66fff124460003085350\n" +
        "0200f00009fdf80000ff66fffda5660003085350\n" +
        "0200f0000afe020000ff67fffbb2ce0003085350\n" +
        "0200f0000bfe0c0000ff68fff8f1af0003085350\n" +
        "0200f0000cfe160000ff69fff6d5c90003085350\n" +
        "0200f0000dfe200000ff6afff4ce820003085350\n" +
        "0200f0000efe2a0000ff6bfff236c00003085350\n" +
        "0200f0000ffe340000ff6bffffd9050003085350\n" +
        "0200f00010fe3e0000ff6cfffd55e30003085350\n" +
        "0200f00011fe480000ff6dfffad2c10003085350\n" +
        "0200f00012fe520000ff6efff8cb7a0003085350\n" +
        "0200f00013fe5c0000ff6ffff6c4330003085350\n" +
        "0200f00014fe660000ff70fff4a85d0003085350\n" +
        "0200f00015fe700000ff71fff31d010003085350\n" +
        "0200f00016fe7a0000ff71ffff60340003085350\n" +
        "0200f00017fe840000ff72fffd1aff0003085350\n" +
        "0200f00018fe8e0000ff73fffa6e8f0003085350\n" +
        "0200f00019fe980000ff74fff867480003085350\n" +
        "0200f0001afea20000ff75fff674b00003085350\n" +
        "0200f0001bfeac0000ff76fff3dcdf0003085350\n" +
        "0200f0001cfeb60000ff77fff197ba0003085350\n" +
        "0200f0001dfec00000ff77fffee7530003085350\n" +
        "0200f0001efeca0000ff78fffc3ae20003085350\n" +
        "0200f0001ffed40000ff79fffa9af60003085350\n" +
        "0200f00020fede0000ff7afff803250003085350\n" +
        "0200f00021fee80000ff7bfff6108d0003085350\n" +
        "0200f00022fef20000ff7cfff3cb4a0003085350\n" +
        "0200f00023fefc0000ff7dfff0e0eb0003085350\n" +
        "0200f00024ff060000ff7dfffe59e20003085350\n" +
        "0200f00025ff100000ff7efffb98d20003085350\n" +
        "0200f00026ff1a0000ff7ffff9a62a0003085350\n" +
        "0200f00027ff240000ff80fff8063f0003085350\n" +
        "0200f00028ff2e0000ff81fff5451f0003085350\n" +
        "0200f00029ff380000ff82fff298a70003085350\n" +
        "0200f0002aff420000ff82fffffcf60003085350\n" +
        "0200f0002bff4c0000ff83fffdf5c70003085350\n" +
        "0200f0002cff560000ff84fffc03270003085350\n" +
        "0200f0002dff600000ff85fff918b90003085350\n" +
        "0200f0002eff6a0000ff86fff6e82c0003085350\n" +
        "0200f0002fff740000ff87fff548390003085350\n" +
        "0200f00030ff7e0000ff88fff2ee650003085350\n" +
        "0200f00031ff880000ff8fffffeb8f0003085350\n" +
        "0200f00032ff920000ff99fffc7f8c0003085350\n" +
        "0200f00033ff9c0000ffa4fff0d60a0003085350\n" +
        "0200f00034ffa60000ffaefff4c16d0003085350\n" +
        "0200f00035ffb00000ffb8fff117750003085350\n" +
        "0200f00036ffba0000ffc2fff60f510003085350\n" +
        "0200f00037ffc40000ffccfff1c01c0003085350\n" +
        "0200f00038ffce0000ffd6fff5e9700003085350\n" +
        "0200f00039ffd80000ffe0fffab7f60003085350\n" +
        "0200f0003affe20000ffeafff5c38f0003085350\n" +
        "0200f0003bffec0000fff4fff9d8390003085350\n" +
        "0200f0003cfff60000fffefffee4b80003085350\n" +
        "0200f0003d000000000007000abaf30003085350\n" +
        "0200f0003e000a00000011000e53b40003085350\n" +
        "0200f0003f00140000001b000988980003085350\n" +
        "0200f00040001e00000025000e2dd50003085350\n" +
        "0200f000410028000000300002eb970003085350\n" +
        "0200f00042003200000039000ec1d10003085350\n" +
        "0200f00043003c000000440004f3580003085350\n" +
        "0200f0004400460000004e0000f6c30003085350\n" +
        "0200f0004500500000005800050b6c0003085350\n" +
        "0200f00046005a000000620009d9fd0003085350\n" +
        "0200f0004700640000006c00054cd70003085350\n" +
        "0200f00048006e000000760008d0ee0003085350\n" +
        "0200f000490078000000800005503c0003085350\n" +
        "0200f0004a00820000008a0009798d0003085350\n" +
        "0200f0004b008c00000094000515b40003085350\n" +
        "0200f0004c009600000098000f0aef0003085350\n" +
        "0200f0004d00a00000009a00007fac0003085350\n" +
        "0200f0004e00aa0000009a000e36900003085350\n" +
        "0200f0004f00b40000009f000dd5590003085350\n" +
        "0200f0005000be000000a0000b66c70003085350\n" +
        "0200f0005100c8000000a1000784930003085350\n" +
        "0200f0005200d2000000a2000933560003085350\n" +
        "0200f0005300dc0000009e000b05620003085350\n" +
        "0200f0005400e6000000a6000c61b70003085350\n" +
        "0200f0005500f0000000a1000ba1d90003085350\n" +
        "0200f0005600fa000000a1000cd79d0003085350\n" +
        "0200f000570104000000a2000ae5050003085350\n" +
        "0200f00058010e00000099000082880003085350\n" +
        "0200f0005901180000009e0001d2f10003085350\n" +
        "0200f0005a0122000000ac00035bbc0003085350\n" +
        "0200f0005b012c000000ac00047ce10003085350\n" +
        "0200f0005c0136000000ad00036d700003085350\n" +
        "0200f0005d0140000000ad00036d700003085350\n" +
        "0200f0005e014a000000ad00036d700003085350\n" +
        "0200f0005f0154000000ad00036d700003085350\n" +
        "0200f00060015e000000ad00036d700003085350\n" +
        "0200f000610168000000ad00036d700003085350\n" +
        "0200f000620172000000ad00036d700003085350\n" +
        "0200f00063017c000000ad00036d700003085350\n" +
        "0200f000640186000000ad00036d700003085350\n" +
        "0200f000650190000000ad00036d700003085350\n" +
        "0200f00066019a000000ad00036d700003085350\n" +
        "0200f0006701a4000000ad00036d700003085350\n" +
        "0200f0006801ae000000ad00036d700003085350\n" +
        "0200f0006901b8000000ad00036d700003085350\n" +
        "0200f0006a01c2000000ad00036d700003085350\n" +
        "0200f0006b01cc000000ad00036d700003085350\n" +
        "0200f0006c01d6000000ad00036d700003085350\n" +
        "0200f0006d01e0000000ad00036d700003085350\n" +
        "0200f0006e01ea000000ad00036d700003085350\n" +
        "0200f0006f01f4000000ad00036d700003085350\n" +
        "0200f0007001fe000000ad00036d700003085350\n" +
        "0200f000710208000000ad00036d700003085350\n" +
        "0200f000720212000000ad00036d700003085350\n" +
        "0200f00073021c000000ad00036d700003085350\n" +
        "0200f000740226000000ad00036d700003085350\n" +
        "0200f000750230000000ad00036d700003085350\n" +
        "0200f00076023a000000ad00036d700003085350\n" +
        "0200f000770244000000ad00036d700003085350\n" +
        "0200f00078024e000000ad00036d700003085350\n" +
        "0200f000790258000000ad00036d700003085350\n" +
        "0200f0007a024e000000ad00036d700003085350\n" +
        "0200f0007b0244000000ad00036d700003085350\n" +
        "0200f0007c023a000000ad00036d700003085350\n" +
        "0200f0007d0230000000ad00036d700003085350\n" +
        "0200f0007e0226000000ad00036d700003085350\n" +
        "0200f0007f021c000000ad00036d700003085350\n" +
        "0200f000800212000000ad00036d700003085350\n" +
        "0200f000810208000000ad00036d700003085350\n" +
        "0200f0008201fe000000ad00036d700003085350\n" +
        "0200f0008301f4000000ad00036d700003085350\n" +
        "0200f0008401ea000000ad00036d700003085350\n" +
        "0200f0008501e0000000ad00036d700003085350\n" +
        "0200f0008601d6000000ad00036d700003085350\n" +
        "0200f0008701cc000000ad00036d700003085350\n" +
        "0200f0008801c2000000ad00036d700003085350\n" +
        "0200f0008901b8000000ad00036d700003085350\n" +
        "0200f0008a01ae000000ad00036d700003085350\n" +
        "0200f0008b01a4000000ad00036d700003085350\n" +
        "0200f0008c019a000000ad00036d700003085350\n" +
        "0200f0008d0190000000ad00036d700003085350\n" +
        "0200f0008e0186000000ad00036d700003085350\n" +
        "0200f0008f017c000000ad00036d700003085350\n" +
        "0200f000900172000000ad00036d700003085350\n" +
        "0200f000910168000000ad00036d700003085350\n" +
        "0200f00092015e000000ad00036d700003085350\n" +
        "0200f000930154000000ad00036d700003085350\n" +
        "0200f00094014a000000ad00036d700003085350\n" +
        "0200f000950140000000a5000ed02a0003085350\n" +
        "0200f000960136000000ac0003850b0003085350\n" +
        "0200f00097012c000000ac000225e90003085350\n" +
        "0200f000980122000000a70009de940003085350\n" +
        "0200f000990118000000a6000009f40003085350\n" +
        "0200f0009a010e000000a5000605140003085350\n" +
        "0200f0009b0104000000a2000295d90003085350\n" +
        "0200f0009c00fa000000a10003674c0003085350\n" +
        "0200f0009d00f0000000a50008c3470003085350\n" +
        "0200f0009e00e6000000a2000470d60003085350\n" +
        "0200f0009f00dc000000a10006df4a0003085350\n" +
        "0200f000a000d2000000a0000b90250003085350\n" +
        "0200f000a100c80000009f000ba7b00003085350\n" +
        "0200f000a200be0000009f000033050003085350\n" +
        "0200f000a300b40000009c0002bbff0003085350\n" +
        "0200f000a400aa00000099000aacb10003085350\n" +
        "0200f000a500a00000009400069e150003085350\n" +
        "0200f000a600960000008a000a0a270003085350\n" +
        "0200f000a7008c000000800005b7880003085350\n" +
        "0200f000a8008200000076000a829d0003085350\n" +
        "0200f000a900780000006c000606b70003085350\n" +
        "0200f000aa006e00000062000a6a880003085350\n" +
        "0200f000ab0064000000580006bd230003085350\n" +
        "0200f000ac005a0000004e000255dd0003085350\n" +
        "0200f000ad005000000044000598910003085350\n" +
        "0200f000ae00460000003a0000ca030003085350\n" +
        "0200f000af003c0000003000045f540003085350\n" +
        "0200f000b0003200000025000efc590003085350\n" +
        "0200f000b100280000001b000b3a4c0003085350\n" +
        "0200f000b2001e000000120000d7ce0003085350\n" +
        "0200f000b3001400000007000bc76f0003085350\n" +
        "0200f000b4000a0000fffffff17d7a0003085350\n" +
        "0200f000b500000000fff4fffc1a7e0003085350\n" +
        "0200f000b6fff60000ffeafff7c7de0003085350\n" +
        "0200f000b7ffec0000ffe0fffc02680003085350\n" +
        "0200f000b8ffe20000ffd6fff8170e0003085350\n" +
        "0200f000b9ffd80000ffccfff30a8e0003085350\n" +
        "0200f000baffce0000ffc2fff76e630003085350\n" +
        "0200f000bbffc40000ffb8fff2ddcb0003085350\n" +
        "0200f000bcffba0000ffaefff620870003085350\n" +
        "0200f000bdffb00000ffa4fff1e28f0003085350\n" +
        "0200f000beffa60000ff99fffd4e1c0003085350\n" +
        "0200f000bfff9c0000ff90fff298fb0003085350\n" +
        "0200f000c0ff920000ff88fff3939e0003085350\n" +
        "0200f000c1ff880000ff87fff5ed720003085350\n" +
        "0200f000c2ff7e0000ff86fff8853c0003085350\n" +
        "0200f000c3ff740000ff85fff9bdf30003085350\n" +
        "0200f000c4ff6a0000ff84fffc41150003085350\n" +
        "0200f000c5ff600000ff83fffe9af80003085350\n" +
        "0200f000c6ff560000ff83fff1893e0003085350\n" +
        "0200f000c7ff4c0000ff82fff435af0003085350\n" +
        "0200f000c8ff420000ff81fff63ce60003085350\n" +
        "0200f000c9ff380000ff80fff8822a0003085350\n" +
        "0200f000caff2e0000ff7ffffac75e0003085350\n" +
        "0200f000cbff240000ff7efffcce860003085350\n" +
        "0200f000ccff1a0000ff7dfffeff1b0003085350\n" +
        "0200f000cdff100000ff7dfff1af820003085350\n" +
        "0200f000ceff060000ff7cfff499e10003085350\n" +
        "0200f000cffefc0000ff7bfff677ca0003085350\n" +
        "0200f000d0fef20000ff7afff87f010003085350\n" +
        "0200f000d1fee80000ff79fffb16e10003085350\n" +
        "0200f000d2fede0000ff78fffdd7f20003085350\n" +
        "0200f000d3fed40000ff77ffff633e0003085350\n" +
        "0200f000d4feca0000ff77fff2b8cf0003085350\n" +
        "0200f000d5fec00000ff76fff458ca0003085350\n" +
        "0200f000d6feb60000ff75fff743470003085350\n" +
        "0200f000d7feac0000ff74fff8ce840003085350\n" +
        "0200f000d8fea20000ff73fffbcd920003085350\n" +
        "0200f000d9fe980000ff72fffdd4d80003085350\n" +
        "0200f000dafe8e0000ff71ffffb2d00003085350\n" +
        "0200f000dbfe840000ff71fff331b00003085350\n" +
        "0200f000dcfe7a0000ff70fff524480003085350\n" +
        "0200f000ddfe700000ff6ffff7697c0003085350\n" +
        "0200f000defe660000ff6efffa013e0003085350\n" +
        "0200f000dffe5c0000ff6dfffbf3d60003085350\n" +
        "0200f000e0fe520000ff6cfffe245b0003085350\n" +
        "0200f000e1fe480000ff6cfff0e9620003085350\n" +
        "0200f000e2fe3e0000ff6bfff2dbea0003085350\n" +
        "0200f000e3fe340000ff6afff5211f0003085350\n" +
        "0200f000e4fe2a0000ff69fff766630003085350\n" +
        "0200f000e5fe200000ff68fff96d9a0003085350\n" +
        "0200f000e6fe160000ff67fffbb2ce0003085350\n" +
        "0200f000e7fe0c0000ff66fffe73ee0003085350\n" +
        "0200f000e8fe020000ff66fff1c9700003085350\n" +
        "0200f000e9fdf80000ff65fff3d0b60003085350\n" +
        "0200f000eafdee0000ff64fff59a000003085350\n" +
        "0200f000ebfde40000ff63fff7633a0003085350\n" +
        "0200f000ecfdda0000ff62fff9fb1a0003085350\n" +
        "0200f000edfdd00000ff61fffca78b0003085350\n" +
        "0200f000eefdc60000ff60fffe70c50003085350\n" +
        "0200f000effdbc0000ff60fff14a6b0003085350\n" +
        "0200f000f0fda80000ff5ffff3cd9d0003085350";
        // "";

    @Test
    public void test2002() {
        testRealDataChart(new ArrayList<String>(Arrays.asList(new String[]{Test2002Data})));
    }

    @Test
    public void Test2001and2() {
        testRealDataChart(new ArrayList<String>(Arrays.asList(new String[]{Test2001Data, Test2002Data})));
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

}
