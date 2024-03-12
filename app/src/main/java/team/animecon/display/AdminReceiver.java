// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * Receives messages and intents for this app as a device administrator app. We don't do anything
 * useful here, but this is important in enabling Kiosk-like behaviour.
 *
 * @see https://developer.android.com/work/device-admin#sample
 */
public class AdminReceiver extends DeviceAdminReceiver {
    /**
     * Shows the given `message` as a toast. Should only be used for diagnostics.
     */
    private void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // DeviceAdminReceiver implementation:

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        this.showMessage(context, "VM: Device Admin enabled");
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        this.showMessage(context, "VM: Device Admin disabled");
    }

    @Override
    public void onLockTaskModeEntering(@NonNull Context context, @NonNull Intent intent, @NonNull String pkg) {
        this.showMessage(context, "VM: Kiosk mode enabled");
    }

    @Override
    public void onLockTaskModeExiting(@NonNull Context context, @NonNull Intent intent) {
        this.showMessage(context, "VM: Kiosk mode disabled");
    }

    @Override
    public CharSequence onDisableRequested(@NonNull Context context, @NonNull Intent intent) {
        return "Warning: Device Admin will be disabled.";
    }
}
