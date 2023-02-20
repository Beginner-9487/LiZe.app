package com.example.lize_app.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lize_app.R;
import com.example.lize_app.ui.main.MainActivity;

public class PermissionAgreeFragment extends DialogFragment {

    String message = "";
    String[] permission_list;
    int requestCode = 0;

    /**
     * Use {@link DialogFragment}} to create request permission dialogs.
     * @param Message - AlertDialog Message
     * @param PermissionList - Requested Permissions
     * @param RequestCode - RequestCode
     */
    public PermissionAgreeFragment(@NonNull String Message, @NonNull String[] PermissionList, int RequestCode) {
        message = Message;
        permission_list = PermissionList;
        requestCode = RequestCode;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
            .setPositiveButton(
                getResources().getString(R.string.Fragment_OK),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                            getActivity(),
                                permission_list,
                            requestCode
                        );
                        dismiss();
                    }
                }
            )
            .setNegativeButton(
                getResources().getString(R.string.Fragment_Cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                }
            );

        return builder.create();
    }

}
