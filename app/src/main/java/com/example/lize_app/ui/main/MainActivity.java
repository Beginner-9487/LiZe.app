package com.example.lize_app.ui.main;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.lize_app.utils.OtherUsefulFunction;
import com.mikepenz.aboutlibraries.LibsBuilder;

import com.example.lize_app.R;
import com.example.lize_app.ui.base.BaseActivity;
import com.example.lize_app.utils.BLEIntents;

import javax.inject.Inject;

//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import butterknife.Unbinder;

public class MainActivity extends BaseActivity implements MainMvpView {

    private static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_LOCATION_CODE = 10;

//    @BindView(R.id.Central_Button)
    Button Central_btn;

//    @BindView(R.id.Peripheral_Button)
    Button Peripheral_btn;

//    @BindView(R.id.About_Button)
    Button About_btn;

//    private Unbinder unbinder;

    @Inject BluetoothManager mBluetoothManager;
    @Inject MainPresenter mMainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivityComponent().inject(this);

        setContentView(R.layout.activity_home);

//        unbinder = ButterKnife.bind(this);
        Central_btn = (Button) findViewById(R.id.Central_Button);
        Peripheral_btn = (Button) findViewById(R.id.Peripheral_Button);
        About_btn = (Button) findViewById(R.id.About_Button);
        Central_btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCentralMode();
            }
        });
        Peripheral_btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPeripheralMode();
            }
        });
        About_btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAboutLibrary();
            }
        });

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();

        // Checks if Bluetooth is enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();

        //checkLocationPermission();
        mMainPresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mMainPresenter.detachView();
    }

    @Override
    public void onDestroy() {
//        unbinder.unbind();

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0) {
                    //great;
                }
        }
    }

    @Override
//    @OnClick(R.id.Central_Button)
    public void startCentralMode() {
        if (checkLocationPermission()) {
            startActivity(new Intent(BLEIntents.ACTION_CENTRAL_MODE));
        }
    }

    @Override
//    @OnClick(R.id.Peripheral_Button)
    public void startPeripheralMode() {
        startActivity(new Intent(BLEIntents.ACTION_PERIPHERAL_MODE));
    }

    @Override
//    @OnClick(R.id.About_Button)
    public void startAboutLibrary() {
        new LibsBuilder()
//            .withActivityStyle(Libs.ActivityStyle.DARK)
            .withActivityTheme(R.style.Theme_LiZe_App)
            .withAboutIconShown(true)
            .withAboutVersionShown(true)
            .withAboutDescription(getResources().getString(R.string.AboutDescription))
            .start(this);
    }

    private boolean checkLocationPermission() {

        return OtherUsefulFunction.checkPermissionList(
            this,
            true,
            getResources().getString(R.string.LocationPermissionAgreeFragment),
            new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            },
            REQUEST_LOCATION_CODE,
            getSupportFragmentManager(),
            getResources().getString(R.string.LocationPermissionTag)
        );

    }

}
