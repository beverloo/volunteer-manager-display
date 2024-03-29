// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

/**
 * Interface defining the callbacks related to the serial port's communication.
 */
public interface SerialPortObserver {
    /**
     * To be called when an error has occurred on the serial port.
     */
    void onError(String operation, String message);
}
