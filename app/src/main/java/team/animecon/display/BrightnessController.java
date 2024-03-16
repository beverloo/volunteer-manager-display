// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * Controller that allows the device's brightness to be manipulated. A special system permission has
 * to be granted, which the `Initialise()` function will insist on being the case.
 */
public class BrightnessController {
    /**
     * The context of the application managing this controller.
     */
    private final Context mContext;

    /**
     * Minimum brightness. The setter won't go below this value.
     */
    private final int mMinimumBrightness;

    public BrightnessController(Context context, int minimumBrightness) {
        this.mContext = context;
        this.mMinimumBrightness = minimumBrightness;
    }

    /**
     * Initialises the controller. Requires the `WRITE_SETTINGS` permission to be granted.
     */
    public void initialise() {
        if (!Settings.System.canWrite(this.mContext)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.mContext.getPackageName()));
            this.mContext.startActivity(intent);
        }
    }

    /**
     * Reads the current brightness from the system. This does the right thing since Android P.
     */
    public int getBrightness() {
        float brightness = Settings.System.getInt(
                this.mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);

        return (int) Math.floor((double) brightness);
    }

    /**
     * Sets the system brightness to the given `brightness`, which must be within valid range.
     */
    public boolean update(int brightness) {
        if (brightness < mMinimumBrightness || brightness < 0)
            return false;  // |brightness| is too low
        if (brightness > 255)
            return false;  // |brightness| is too high

        Settings.System.putInt(
                this.mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        return true;
    }
}
