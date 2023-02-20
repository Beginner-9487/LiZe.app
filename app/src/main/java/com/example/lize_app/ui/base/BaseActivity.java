package com.example.lize_app.ui.base;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lize_app.BLEApplication;
import com.example.lize_app.injector.component.ActivityComponent;
import com.example.lize_app.injector.component.DaggerActivityComponent;
import com.example.lize_app.injector.module.ActivityModule;

/**
 *
 */
public class BaseActivity extends AppCompatActivity {

    private ActivityComponent mComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ActivityComponent getActivityComponent() {
        if (mComponent == null) {
            mComponent = DaggerActivityComponent.builder()
                    .applicationComponent(((BLEApplication)getApplication()).getApplicationComponent())
                    .activityModule(new ActivityModule(this))
                    .build();
        }

        return mComponent;
    }
}
