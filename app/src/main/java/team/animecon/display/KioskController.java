// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.view.View;

/**
 * The `KioskController` allows the device to enter and leave kiosk mode on demand. Behaviour should
 * be controlled from JavaScript, so that the default device state (e.g. network issues) leads to
 * the full device continuing to be accessible.
 */
public class KioskController {
    private static final String TAG = "KioskController";
    /**
     * The Android Activity for which Kiosk mode is being controlled.
     */
    private final Activity mActivity;

    /**
     * The decor view, i.e. the space outside of our application window.
     */
    private final View mDecorView;

    /**
     * The DevicePolicyManager through which the list of locked task packages is managed.
     */
    private final DevicePolicyManager mDevicePolicyManager;

    public KioskController(Activity activity) {
        this.mActivity = activity;
        this.mDecorView = activity.getWindow().getDecorView();
        this.mDevicePolicyManager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    /**
     * Initialises the controller.
     */
    public void initialise() {
        ComponentName deviceAdmin = new ComponentName(this.mActivity, AdminReceiver.class);
        if (!mDevicePolicyManager.isAdminActive(deviceAdmin)) {
            Log.w(TAG, "This app is not a device admin app");
        }

        if (mDevicePolicyManager.isDeviceOwnerApp(this.mActivity.getPackageName())) {
            mDevicePolicyManager.setLockTaskPackages(
                    deviceAdmin, new String[]{ this.mActivity.getPackageName() });
        } else {
            Log.w(TAG, "This app is not a device owner app");
        }
    }

    /**
     * Hides the user interface for the application. We do this regardless of whether kiosk mode is
     * activated, which really provides a secondary certainty step on top of this.
     */
    public void hideUserInterface() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Enables the kiosk mode for the running app.
     */
    public boolean enable() {
        if (!mDevicePolicyManager.isLockTaskPermitted(this.mActivity.getPackageName())) {
            return false;
        }

        mActivity.startLockTask();
        return true;
    }

    /**
     * Disables the kiosk mode for the running app.
     */
    public boolean disable() {
        mActivity.stopLockTask();
        return true;
    }
}
