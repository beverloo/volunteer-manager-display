// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

import android.os.SystemClock;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * The `LightController` class is used to communicate with the LED strip around the Display, which
 * is a serial device exposed on `/dev/ttyS3` at 9600 baud. The actual input/output will be managed
 * by JavaScript to maintain flexibility in updating our behaviour.
 *
 * The hardware in the displays we use supports the following commands:
 *
 *   LIVE:{RED,GREEN,BLUE}:{SECONDS}          - Enable the "live" mode for the given colour.
 *   KEEP:{RED,GREEN,BLUE}:{SECONDS}:{0-255}  - Enable the "keep" mode for the given colour.
 *   CRAZY:{SECONDS}                          - Enable the "crazy" mode.
 *   FLASH:{SECONDS}                          - Enable the "flash" mode.
 *   CLOSE:{RED,GREEN,BLUE}                   - Shuts off the given colour(s) entirely.
 */
public class LightController implements SerialPortObserver {
    private static final String TAG = "LightController";

    /**
     * The serial port that will be used for communicating with the light.
     */
    private final SerialPort mSerialPort;

    public LightController(String device, int baudRate) {
        this.mSerialPort = new SerialPort(device, baudRate, this);
    }

    /**
     * Opens the serial port connection with the device.
     */
    public boolean open() {
        return this.mSerialPort.open();
    }

    /**
     * Updates the light bar colour to the given `red`, `green` and `blue` RGB values.
     */
    public boolean set(int red, int green, int blue) {
        this.sendCommand("KEEP:RED:0:" + red);
        SystemClock.sleep(40);
        this.sendCommand("KEEP:GREEN:0:" + green);
        SystemClock.sleep(50);
        return this.sendCommand("KEEP:BLUE:0:" + blue);
    }

    /**
     * Sends a command to the device's lights. This should be replaced by a far more sensible API
     * that allows input/output from JavaScript.
     */
    public boolean sendCommand(String command) {
        return this.mSerialPort.write(command);
    }

    /**
     * Closes the serial port connection with the device.
     */
    public boolean close() {
        return this.mSerialPort.close();
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public void onError(String operation, String message) {
        Log.e(TAG, "Error (" + operation + "): " + message);
    }
}
