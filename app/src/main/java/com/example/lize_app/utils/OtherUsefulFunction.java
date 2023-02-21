package com.example.lize_app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.lize_app.fragment.PermissionAgreeFragment;

public class OtherUsefulFunction {

    /**
     * Check that all required permissions have been granted.
     * <p></p>
     * {@link PermissionAgreeFragment}
     */
    public static boolean checkPermissionList(Activity activity, boolean must_agree, @NonNull String Message, @NonNull String[] PermissionList, int RequestCode, @NonNull FragmentManager manager, @Nullable String tag) {

        boolean b = true;
        boolean Rationale_lock = false;

        for (String permission:PermissionList) {
            if(ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                b = false;
                if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Rationale_lock = true;
                }
                break;
            }
        }

        if(!b) {
            if (must_agree || Rationale_lock) {
                PermissionAgreeFragment dialog = new PermissionAgreeFragment(
                        Message,
                        PermissionList,
                        RequestCode
                );
                dialog.show(manager, tag);
            }
        }

        return b;

    }

}
