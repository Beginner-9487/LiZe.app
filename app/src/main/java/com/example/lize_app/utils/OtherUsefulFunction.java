package com.example.lize_app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.lize_app.R;
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

    public static final int REQUEST_BLUETOOTH_CODE = 1;
    /**
     * Check Bluetooth Permissions have been granted.
     * <p></p>
     * {@link OtherUsefulFunction#checkPermissionList(Activity, boolean, String, String[], int, FragmentManager, String)}
     */
    public static boolean checkBluetoothPermission(FragmentActivity activity) {
        return OtherUsefulFunction.checkPermissionList(
                activity,
                true,
                activity.getResources().getString(R.string.BluetoothPermissionAgreeFragment),
                new String[] {
                        Manifest.permission.BLUETOOTH_CONNECT
                },
                REQUEST_BLUETOOTH_CODE,
                activity.getSupportFragmentManager(),
                activity.getResources().getString(R.string.BluetoothPermissionTag)
        );
    }

    public static final int REQUEST_LOCATION_CODE = 10;
    /**
     * Check Location Permissions have been granted.
     * <p></p>
     * {@link OtherUsefulFunction#checkPermissionList(Activity, boolean, String, String[], int, FragmentManager, String)}
     */
    public static boolean checkLocationPermission(FragmentActivity activity) {
        return OtherUsefulFunction.checkPermissionList(
                activity,
                true,
                activity.getResources().getString(R.string.LocationPermissionAgreeFragment),
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_LOCATION_CODE,
                activity.getSupportFragmentManager(),
                activity.getResources().getString(R.string.LocationPermissionTag)
        );
    }
    public static final int REQUEST_EXTERNAL_STORAGE_CODE = 0;
    /**
     * Check External Storage Permissions have been granted.
     * <p></p>
     * {@link OtherUsefulFunction#checkPermissionList(Activity, boolean, String, String[], int, FragmentManager, String)}
     */
    public static boolean checkExternalStoragePermission(FragmentActivity activity) {
        return OtherUsefulFunction.checkPermissionList(
                activity,
                true,
                activity.getResources().getString(R.string.ExternalStoragePermissionAgreeFragment),
                new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },
                REQUEST_EXTERNAL_STORAGE_CODE,
                activity.getSupportFragmentManager(),
                activity.getResources().getString(R.string.ExternalStoragePermissionTag)
        );
    }

    /**
     * Convert Alphanumeric String to Byte Array
     * <p></p>
     * {@link Character#digit}
     */
    public static byte[] hexStringToByteArray(String s) {

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;

    }

    /**
     * Convert Byte Array to Alphanumeric String
     * <p></p>
     * {@link Integer#toHexString}
     */
    public static String byteArrayToHexString(byte[] bytes, String split_regex, @NonNull boolean isSigned, String positive_sign, String negative_sign) {

        if(bytes == null) bytes = new byte[]{};
        if(split_regex == null) split_regex = "";
        if(positive_sign == null) positive_sign = "";
        if(negative_sign == null) negative_sign = "";

        String s = "";
        for (byte b:bytes) {
            if(isSigned) {
                s += ( (b<0) ? negative_sign : positive_sign ) + (( b<0x10 && b>=0 ) ? "0" : "" ) + Integer.toHexString( (b<0) ? b&0xff : b ) + split_regex;
            } else {
                s += (((b&0xff)<0x10)?"0":"") + Integer.toHexString(b&0xff) + split_regex;
            }
        }
        return s;

    }
    /**
     * {@link #byteArrayToHexString(byte[], String, boolean, String, String)}
     * <p>
     * isSigned == false;
     */
    public static String byteArrayToHexString(byte[] bytes, String split_regex) {
        return byteArrayToHexString(bytes, split_regex, false, null, null);
    }
    /**
     * {@link #byteArrayToHexString(byte[], String, boolean, String, String)}
     * <p>
     * isSigned == true;
     */
    public static String byteArrayToHexString(byte[] bytes, String split_regex, String positive_sign, String negative_sign) {
        return byteArrayToHexString(bytes, split_regex, true, positive_sign, negative_sign);
    }

    /**
     * Convert Byte Array to Integer
     * <p></p>
     * {@link Character#digit}
     */
    public static int byteArrayToSignedInt(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value |= (long) (bytes[bytes.length - i - 1] & 0xff) << (8 * i);
        }
        // If the most significant bit of the final byte is set, the value is negative.
        if ((bytes[0] & 0x80) != 0) {
            // Extend the sign bit to fill the entire long value.
            value |= (-1L << (8 * bytes.length));
        }
        return (int) value;
    }

}
