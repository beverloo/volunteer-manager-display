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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
    private final VolumeController mVolumeController;

    public WebMessageListener(
            BrightnessController brightnessController, KioskController kioskController,
            LightController lightController, VolumeController volumeController) {
        this.mBrightnessController = brightnessController;
        this.mKioskController = kioskController;
        this.mLightController = lightController;
        this.mVolumeController = volumeController;
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
        } else if (messageData.startsWith("ip")) {
            this.onIpCommand(replyProxy);
        } else if (messageData.startsWith("kiosk:")) {
            this.onKioskCommand(messageData.substring(/* len(kiosk:)= */ 6), replyProxy);
        } else if (messageData.startsWith("light:")) {
            this.onLightCommand(messageData.substring(/* len(light:)= */ 6), replyProxy);
        } else if (messageData.startsWith("lightset:")) {
            this.onLightSetCommand(messageData.substring(/* len(lightset:)= */ 9), replyProxy);
        } else if (messageData.startsWith("volume:")) {
            this.onVolumeCommand(messageData.substring(/* len(volume:)= */ 7), replyProxy);
        } else {
            this.respond(replyProxy, "error:Invalid command");
        }
    }

    /**
     * Deals with brightness commands. The following commands are supported:
     * - get        Returns the device's current brightness level.
     * - {0-255}    Updates the device's brightness to the given value.
     */
    private void onBrightnessCommand(
            @NonNull String command, @NonNull JavaScriptReplyProxy replyProxy) {
        if (command.startsWith("get")) {
            int brightness = this.mBrightnessController.getBrightness();

            this.respond(replyProxy, "success:" + brightness);
            return;
        }

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
     * Deals with the ip command, which outputs the local IP addresses.
     */
    private void onIpCommand(@NonNull JavaScriptReplyProxy replyProxy) {
        try {
            List<String> addresses = new ArrayList<String>();

            Enumeration<NetworkInterface> networkIter = NetworkInterface.getNetworkInterfaces();
            while (networkIter.hasMoreElements()) {
                NetworkInterface networkInterface = networkIter.nextElement();

                Enumeration<InetAddress> addressIter = networkInterface.getInetAddresses();
                while (addressIter.hasMoreElements()) {
                    InetAddress address = addressIter.nextElement();
                    if (!address.isLoopbackAddress())
                        addresses.add(address.getHostAddress());
                }
            }
            this.respond(replyProxy, "success:" + String.join(";", addresses));
        } catch (SocketException e) {
            this.respond(replyProxy, "error:" + e.getMessage());
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
     * - open                                     Opens the serial connection with the light.
     * - close                                    Closes the serial connection with the light.
     * - LIVE:{RED,GREEN,BLUE}:{SECONDS}          Enable the "live" mode for the given colour.
     * - KEEP:{RED,GREEN,BLUE}:{SECONDS}:{0-255}  Enable the "keep" mode for the given colour.
     * - CRAZY:{SECONDS}                          Enable the "crazy" mode.
     * - FLASH:{SECONDS}                          Enable the "flash" mode.
     * - CLOSE:{RED,GREEN,BLUE}                   Shuts off the given colour(s) entirely.
     */
    private void onLightCommand(@NonNull String command, @NonNull JavaScriptReplyProxy replyProxy) {
        boolean result = false;

        if (command.startsWith("open")) {
            result = this.mLightController.open();
        } else if (command.startsWith("close")) {
            result = this.mLightController.close();
        } else {
            result = this.mLightController.sendCommand(command);
        }

        if (result) {
            this.respond(replyProxy, "success");
        } else {
            this.respond(replyProxy, "error:Invalid light command");
        }
    }

    /**
     * Sets the lights to a predefined value. Singular command that results in multiple commands
     * to be issued over the serial port for improved performance.
     *
     * - {0-255},{0-255},{0-255}   Updates the light strip's colour to the given R, G, B
     */
    private void onLightSetCommand(
            @NonNull String command, @NonNull JavaScriptReplyProxy replyProxy) {
        String[] components = command.split(",");
        if (components.length == 3) {
            try {
                int red = Integer.parseInt(components[0]);
                int green = Integer.parseInt(components[1]);
                int blue = Integer.parseInt(components[2]);

                if (red < 0 || green < 0 || blue < 0 || red > 255 || green > 255 || blue > 255) {
                    this.respond(replyProxy, "error:Invalid light command (out of bounds)");
                    return;
                }

                if (this.mLightController.set(red, green, blue)) {
                    this.respond(replyProxy, "success");
                    return;
                }
            } catch (NumberFormatException e) {
                this.respond(replyProxy, "error:Invalid light command (odd number)");
                return;
            }
        }
        this.respond(replyProxy, "error:Invalid light command (needs rgb)");
    }

    /**
     * Deals with volume commands. The following commands are supported:
     * - get        Returns the device's current volume level.
     * - {0-255}    Updates the device's volume to the given value.
     */
    private void onVolumeCommand(
            @NonNull String command, @NonNull JavaScriptReplyProxy replyProxy) {
        if (command.startsWith("get")) {
            int volume = this.mVolumeController.getVolume();

            this.respond(replyProxy, "success:" + volume);
            return;
        }

        try {
            int volume = Integer.parseInt(command);
            if (volume >= 0 && volume <= 255) {
                this.mVolumeController.update(volume);
                this.respond(replyProxy, "success");
            } else {
                this.respond(replyProxy, "error:Invalid volume command (out of bounds");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Received an invalid volume value: " + command);
            this.respond(replyProxy, "error:Invalid volume command");
        }
    }
}
