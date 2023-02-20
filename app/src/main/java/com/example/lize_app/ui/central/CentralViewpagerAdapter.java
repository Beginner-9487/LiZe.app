package com.example.lize_app.ui.central;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lize_app.R;
import com.example.lize_app.adapter.LeDeviceAdapter;
import com.example.lize_app.data.BLEDataServer;
import com.example.lize_app.ui.base.BaseFragment;
import com.example.lize_app.ui.base.BasePresenter;
import com.example.lize_app.ui.base.BaseViewpagerAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class CentralViewpagerAdapter extends BaseViewpagerAdapter {

    public CentralViewpagerAdapter(FragmentManager fm, String[] Titles, Fragment[] Fragments) {
        super(fm, Titles, Fragments);
    }

}