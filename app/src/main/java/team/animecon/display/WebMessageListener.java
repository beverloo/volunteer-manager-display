// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.webkit.JavaScriptReplyProxy;
import androidx.webkit.WebMessageCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

/**
 * The `WebMessageListener` listens for command coming from JavaScript, telling the host app to
 * execute commands. This class is responsible for accepting, parsing, and then routing messages.
 */
public class WebMessageListener implements WebViewCompat.WebMessageListener {
    private static final String TAG = "WebMessageListener";

    /**
     * Instances of the controller objects that can be controlled through JavaScript.
     */
    private final BrightnessController mBrightnessController;
    private final KioskController mKioskController;
    private final LightController mLightController;

    public WebMessageListener(
            BrightnessController brightnessController, KioskController kioskController,
            LightController lightController) {
        this.mBrightnessController = brightnessController;
        this.mKioskController = kioskController;
        this.mLightController = lightController;
    }

    /**
     * Responds to a received message on the `replyProxy` with the given `response`.
     */
    private void respond(@NonNull JavaScriptReplyProxy replyProxy, String response) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            replyProxy.postMessage(response);
        }
    }

    /**
     * Receives messages from JavaScript, and translates them to commands on the various controller
     * objects that we have. Each command will be acknowledged with a response.
     */
    @Override
    public void onPostMessage(
            @NonNull WebView view, @NonNull WebMessageCompat message, @NonNull Uri sourceOrigin,
            boolean isMainFrame, @NonNull JavaScriptReplyProxy replyProxy) {
        if (message.getType() != WebMessageCompat.TYPE_STRING) {
            Log.e(TAG, "A non-string message was received from JavaScript; ignoring");
            return;
        }

        String messageData = message.getData();
        if (messageData == null || messageData.isEmpty()) {
            Log.e(TAG, "A null or empty message was received from JavaScript; ignoring");
            return;
        }

        if (messageData.startsWith("brightness:")) {
            this.onBrightnessCommand(messageData.substring(/* len(brightness:)= */ 11), replyProxy);
        } else if (messageData.startsWith("kiosk:")) {
            this.onKioskCommand(messageData.substring(/* len(kiosk:)= */ 6), replyProxy);
        } else if (messageData.startsWith("light:")) {
            this.onLightCommand(messageData.substring(/* len(light:)= */ 6), replyProxy);
        } else {
            this.respond(replyProxy, "error:Invalid command");
        }
    }

    /**
     * Deals with brightness commands. The following commands are supported:
     * - {0-255}    Updates the device's brightness to the given value.
     */
    private void onBrightnessCommand(
            @NonNull String command, @NonNull JavaScriptReplyProxy replyProxy) {
        try {
            int brightness = Integer.parseInt(command);
            if (brightness >= 0 && brightness <= 255) {
                this.mBrightnessController.update(brightness);
                this.respond(replyProxy, "success");
            } else {
                this.respond(replyProxy, "error:Invalid brightness command (out of bounds");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Received an invalid brightness value: " + command);
            this.respond(replyProxy, "error:Invalid brightness command");
        }
    }

    /**
     * Deals with kiosk commands. The following commands are supported:
     * - disable          Disables kiosk mode's task lockdown on the current device.
     * - enable           Enables kiosk mode's task lockdown on the current device.
     */
    private void onKioskCommand(@NonNull String command, @NonNull JavaScriptReplyProxy replyProxy) {
        if (command.startsWith("disable")) {
            this.mKioskController.disable();
            this.respond(replyProxy, "success");
        } else if (command.startsWith("enable")) {
            this.mKioskController.enable();
            this.respond(replyProxy, "success");
        } else {
            this.respond(replyProxy, "error:Invalid kiosk command");
        }
    }

    /**
     * Deals with light commands. The following commands are supported:
     * - LIVE:{RED,GREEN,BLUE}:{SECONDS}          Enable the "live" mode for the given colour.
     * - KEEP:{RED,GREEN,BLUE}:{SECONDS}:{0-255}  Enable the "keep" mode for the given colour.
     * - CRAZY:{SECONDS}                          Enable the "crazy" mode.
     * - FLASH:{SECONDS}                          Enable the "flash" mode.
     * - CLOSE:{RED,GREEN,BLUE}                   Shuts off the given colour(s) entirely.
     */
    private void onLightCommand(@NonNull String command, @NonNull JavaScriptReplyProxy replyProxy) {
        this.mLightController.sendCommand(command);
        this.respond(replyProxy, "success");
    }
}
