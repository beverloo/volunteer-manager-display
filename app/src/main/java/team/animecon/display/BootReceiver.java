// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Listens to the `BOOT_COMPLETED` broadcast, which we should receive whenever the device is
 * rebooted. We automagically start our own activity in here.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
            return;

        Intent myIntent = new Intent(context, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }
}
