package logic;

import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;

public class Scanner {

    private static final String TAG = "Scanner";

    UsbDeviceConnection devConn;
    UsbInterface usbInterface;
    UsbManager usbManager;
    UsbDevice usbDevice;
    protected UsbEndpoint in, out;

    static boolean inInit = false, fingerSensed, scanCancelled = false;

    private boolean ready;

    private HashMap<String, Bitmap> images;
    private HashMap<String, String> biometrics, isoTemplates;

    Scanner(UsbDevice device, UsbManager uManager) {
        usbDevice = device;
        usbManager = uManager;
        devConn = usbManager.openDevice(device);
        initMaps();
    }

    public void makeReady() {
        ready = true;
    }

    public void initMaps() {
        images = new HashMap<>();
        biometrics = new HashMap<>();
        isoTemplates = new HashMap<>();
    }

    public boolean scan(String finger) {
        return false;
    }

    public boolean scan(String finger, boolean trigger) {
        return false;
    }

    public Bitmap getImage(String name) {
        if (images.containsKey(name)) {
            return images.get(name);
        } else {
            return null;
        }
    }

    void setImage(String name, Bitmap img) {
        images.put(name, img);
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }

    public static boolean isInInit() {
        return inInit;
    }

    public boolean isFingerSensed() {
        return fingerSensed;
    }

    public static boolean isScanCancelled() {
        return scanCancelled;
    }

    public void setBiometrics(String key, String value) {
        biometrics.put(key, value);
    }

    public HashMap<String, String> getIsoTemplates() {
        try {
            return isoTemplates;
        } catch (NullPointerException e) {
            Log.e(TAG, "Biometrics Map was null! Returning nothing");
            return new HashMap<>();
        }
    }

    public void setIsoTemplate(String key, String value) {
        if (isoTemplates == null) {
            isoTemplates = new HashMap<>();
        }
        isoTemplates.put(key, value);
    }

    public HashMap<String, String> getBiometrics() {
        try {
            return biometrics;
        } catch (NullPointerException e) {
            Log.e(TAG, "Biometrics were null! Returning nothing");
            return new HashMap<>();
        }
    }

    public void cancelScan() {
        scanCancelled = true;
    }
}
