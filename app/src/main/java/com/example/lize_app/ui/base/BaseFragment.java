package com.example.lize_app.ui.base;

import androidx.fragment.app.Fragment;

import com.example.lize_app.BLEApplication;
import com.example.lize_app.injector.component.DaggerFragmentComponent;
import com.example.lize_app.injector.component.FragmentComponent;
import com.example.lize_app.injector.module.FragmentModule;

/**
 *
 */
public class BaseFragment extends Fragment {
    private FragmentComponent mFragmentComponent;

    public FragmentComponent getFragmentComponent() {
        if (mFragmentComponent == null) {
            mFragmentComponent = DaggerFragmentComponent.builder()
                    .applicationComponent(((BLEApplication)getActivity().getApplication()).getApplicationComponent())
                    .fragmentModule(new FragmentModule(this))
                    .build();
        }

        return mFragmentComponent;
    }
}
