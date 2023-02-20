package com.example.lize_app.ui.peripheral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.lize_app.R;
import com.example.lize_app.ui.base.BaseActivity;
import com.example.lize_app.utils.Log;

import javax.inject.Inject;

//import butterknife.BindView;
//import butterknife.ButterKnife;

public class PeripheralActivity extends BaseActivity implements PeripheralMvpView {

    @Inject
    PeripheralPresenter mPeripheralPresenter;

    @Inject
    BluetoothAdapter bluetoothAdapter;

//    @BindView(R.id.toolbar)
    Toolbar mToolbar;

//    @BindView(R.id.support_advertiser)
    TextView mUnSupportAdvertiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);
        setContentView(R.layout.activity_peripheral);

//        ButterKnife.bind(this);
        mToolbar = findViewById(R.id.toolbar);
        mUnSupportAdvertiser = findViewById(R.id.support_advertiser);

        if (mToolbar != null) {
            mToolbar.setTitle(R.string.Peripheral);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mPeripheralPresenter.attachView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPeripheralPresenter.startLeAdvertise(null);
    }

    @Override
    protected void onPause() {
        mPeripheralPresenter.stopLeAdvertise();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mPeripheralPresenter.detachView();
        super.onStop();
    }

    @Override
    public void showSupportLEAdvertiser(boolean support) {
        if (!support) {
            Log.e("BluetoothLeAdvertiser is null");
            mUnSupportAdvertiser.setVisibility(View.VISIBLE);
        } else {
            mUnSupportAdvertiser.setVisibility(View.GONE);
        }
    }
}
