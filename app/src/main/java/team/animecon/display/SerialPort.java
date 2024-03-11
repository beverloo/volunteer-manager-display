package team.animecon.display;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Class responsible for communicating with a particular serial port. Uses Cedric Priscal's C++
 * code for actually opening the port, and combines functionality from elsewhere to make it work.
 */
public class SerialPort {
    private static final String TAG = "SerialPort";

    /**
     * The device that should be connected to. (E.g. "/dev/ttyS3")
     */
    private final String mDevice;

    /**
     * The baud rate indicating the rate of communication. (E.g. 9600)
     */
    private final int mBaudRate;

    /**
     * The observer that should be informed about events and lifetime changes of the port.
     */
    private final SerialPortObserver mObserver;

    /**
     * The file descriptor (created by native code) and the input- and output streams through which
     * we'll communicate with the device.
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(String device, int baudRate, SerialPortObserver observer) {
        this.mDevice = device;
        this.mBaudRate = baudRate;
        this.mObserver = observer;
    }

    /**
     * Opens a file descriptor to the device.
     */
    public void open() {
        File deviceFile = new File(this.mDevice);
        if (!deviceFile.canRead() || !deviceFile.canWrite()) {
            Log.e(TAG, "The device is not readable or writable: " + this.mDevice);
            return;
        }

        this.mFd = nativeOpen(deviceFile.getAbsolutePath(), this.mBaudRate, 8, 0, 1, 0);
        if (this.mFd == null) {
            Log.e(TAG, "The device could not be opened: " + this.mDevice);
            return;
        }

        this.mFileInputStream = new FileInputStream(this.mFd);
        this.mFileOutputStream = new FileOutputStream(this.mFd);
    }

    /**
     * Writes the given `command` over the serial connection.
     */
    public void write(String command) {
        byte[] commandBytes = command.getBytes();
        try {
            this.mFileOutputStream.write(commandBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the file descriptor with the device.
     */
    public void close() {
        try {
            if (this.mFileOutputStream != null) {
                this.mFileOutputStream.close();
                this.mFileOutputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (this.mFileInputStream != null) {
                this.mFileInputStream.close();
                this.mFileInputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        nativeClose();
    }

    public native void nativeClose();
    public native FileDescriptor nativeOpen(
            String device, int baudRate, int dataBits, int parity, int stopBits, int flags);
}
