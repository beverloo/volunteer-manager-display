// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * The Volume controller is in charge of changing the device's volume. This will be exposed to the
 * user interface as a slider, and takes immediate effect.
 */
public class VolumeController {
    /**
     * The AudioManager instance through which we will interact with the system.
     */
    private AudioManager mAudioManager;

    public VolumeController(Activity activity) {
        this.mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Returns the current volume of the device, as an integer between 0 and 255.
     */
    public int getVolume() {
        double maxVolume = this.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        double currentVolume = this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        return (int) Math.round((currentVolume / maxVolume) * 255.0);
    }

    /**
     * Updates the device's current volume to `volume`, given as an integer between 0 and 255.
     */
    public void update(int volume) {
        double maxVolume = this.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        double updatedVolume = (((double) volume) / 255.0) * maxVolume;

        this.mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC, (int) Math.round(updatedVolume), 0);
    }
}
