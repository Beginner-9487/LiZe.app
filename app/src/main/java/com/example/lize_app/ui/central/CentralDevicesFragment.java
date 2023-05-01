package com.example.lize_app.ui.central;

import com.example.lize_app.R;
import com.example.lize_app.adapter.LeDeviceAdapter;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.ui.base.BaseFragment;
import com.example.lize_app.utils.Log;
import com.example.lize_app.utils.OtherUsefulFunction;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import javax.inject.Inject;

public class CentralDevicesFragment extends BaseFragment implements CentralMvpView {

    RecyclerView mRecyclerView;

    SwipeRefreshLayout mRefreshLayout;

    private LeDeviceAdapter mLeDeviceAdapter;

    public LeDeviceAdapter getmLeDeviceAdapter() {
        return mLeDeviceAdapter;
    }

    @Inject
    CentralPresenter mCentralPresenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        com.example.lize_app.utils.Log.d("onAttach");
        getFragmentComponent().inject(this);
        mCentralPresenter.setCurrentView(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        com.example.lize_app.utils.Log.d("onCreateView");
        View view = inflater.inflate(R.layout.device_scan_fragment, container, false);
//        ButterKnife.bind(this, view);
        mRecyclerView = view.findViewById(R.id.recycler);

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swip_refresh_layout);

        mLeDeviceAdapter = new LeDeviceAdapter();
        mRecyclerView.setAdapter(mLeDeviceAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLeDeviceAdapter.setListener(new LeDeviceAdapter.DeviceItemClickListener() {

            @Override
            public void onItemClicked(BluetoothDevice device, int position) {
                startCentralDetailsActivity(device);
            }

            @Override
            public void onItemConnectionButtonClicked(BluetoothDevice device, int position, boolean connection_state_setting) {
                connectGatt(device, connection_state_setting);
            }

            @Override
            public void onItemPairButtonClicked(BLEDataServer.BLEData data, int position, boolean bonding_state_setting) {
                createBond(data);
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshSwipLayout();
            }
        });

        // TODO setColorSchemeResources
        mRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        com.example.lize_app.utils.Log.d("onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        com.example.lize_app.utils.Log.d("onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        com.example.lize_app.utils.Log.d("onStop");
    }

    @Override
    public void showBLEDevice(BluetoothDevice bt) {
        // maybe running in UI thread
        mLeDeviceAdapter.addDevice(bt);
        mLeDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void showBLEData(BLEDataServer.BLEData data) {
        // Log.e(data.device.getName());
        mLeDeviceAdapter.showBLEData(data);
        mLeDeviceAdapter.notifyDataSetChanged();
    }

    public void onRefreshSwipLayout() {
    }

    public void startCentralDetailsActivity(BluetoothDevice device) {
        if (OtherUsefulFunction.checkBluetoothPermission(getActivity())) {
            Intent intent = new Intent(getActivity(), CentralDetailsActivity.class);
            intent.putExtra(CentralDetailsActivity.EXTRAS_DEVICE_NAME, device.getName());
            intent.putExtra(CentralDetailsActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
            startActivity(intent);
        }
    }
    public void connectGatt(BluetoothDevice device, boolean connection_state_setting) {
        if(connection_state_setting) {
            mCentralPresenter.connectGatt(device);
        } else {
            mCentralPresenter.disconnectGatt(device);
        }
    }
    public void createBond(BLEDataServer.BLEData data) {
        mCentralPresenter.createBond(data);
    }

}
