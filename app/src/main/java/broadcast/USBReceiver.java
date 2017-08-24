package broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.openandid.core.Controller;
import com.openandid.core.ScannerCatcher;
import com.openandid.core.ScanningActivity;

import logic.Scanner;


public class USBReceiver extends BroadcastReceiver {

    private static final String USB_ON = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String USB_OFF = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private static final String LOCAL_USB = ScannerCatcher.USB_ON_BROADCAST;
    private static final String TAG = "USBReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case LOCAL_USB:
                UsbDevice incomingDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                ScanningActivity.setFreeDevice(incomingDevice);
                break;
            case USB_OFF:
                try {
                    if (!Controller.mHostUsbManager.isPermittedDeviceConnected()) {
                        if (!Scanner.isInInit()) {
                            Controller.mScanner = null;
                            ScanningActivity.setFreeDevice(null);
                        }
                    }
                } catch (Exception e) {
                    Log.i(TAG, e.toString());
                }
                break;
        }
    }
}
