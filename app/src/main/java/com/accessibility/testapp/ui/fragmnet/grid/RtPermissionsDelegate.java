package com.accessibility.testapp.ui.fragmnet.grid;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;

import com.accessibility.testapp.R;
import com.accessibility.testapp.ui.activity.base.delegate.BaseRtPermissionsDelegate;

/**
 * Delegate for checking Runtime Permissions.
 *
 * @author Aleksandr Brazhkin
 */
public class RtPermissionsDelegate extends BaseRtPermissionsDelegate {

    public RtPermissionsDelegate(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    protected String getPermissionName() {
        return Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }

    @Override
    protected int getRationaleLight() {
        return R.string.write_files_permission_rationale_light;
    }

    @Override
    protected int getRationaleHard() {
        return R.string.write_files_permission_rationale_hard;
    }
}
