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
     * we'll communicate with the device. The `mFd` member is accessed by native code too.
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
    public boolean open() {
        File deviceFile = new File(this.mDevice);
        if (!deviceFile.canRead() || !deviceFile.canWrite()) {
            this.mObserver.onError("open", "The device is not readable or writable.");
            return false;
        }

        this.mFd = nativeOpen(deviceFile.getAbsolutePath(), this.mBaudRate, 8, 0, 1, 0);
        if (this.mFd == null) {
            this.mObserver.onError("open", "A file descriptor to the device could not be opened.");
            return false;
        }

        this.mFileInputStream = new FileInputStream(this.mFd);
        this.mFileOutputStream = new FileOutputStream(this.mFd);
        return true;
    }

    /**
     * Writes the given `command` over the serial connection.
     */
    public void write(String command) {
        byte[] commandBytes = command.getBytes();
        try {
            Log.w("SerialPort", "Write: " + command);
            this.mFileOutputStream.write(commandBytes);
        } catch (IOException e) {
            this.mObserver.onError("write", e.getMessage());
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
            this.mObserver.onError("close", "mFileOutputStream: " + e.getMessage());
        }

        try {
            if (this.mFileInputStream != null) {
                this.mFileInputStream.close();
                this.mFileInputStream = null;
            }
        } catch (IOException e) {
            this.mObserver.onError("close", "mFileInputStream: " + e.getMessage());
        }

        nativeClose();
    }

    public native void nativeClose();
    public native FileDescriptor nativeOpen(
            String device, int baudRate, int dataBits, int parity, int stopBits, int flags);
}
