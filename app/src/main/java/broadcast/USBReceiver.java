package broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.openandid.core.Controller;
import com.openandid.core.ScanningActivity;

import logic.Scanner;

import static com.openandid.core.Constants.DEVICE_DETACHED;
import static com.openandid.core.Constants.SCANNER_ATTACHED;


public class USBReceiver extends BroadcastReceiver {

    private static final String TAG = "USBReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case SCANNER_ATTACHED:
                UsbDevice incomingDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                ScanningActivity.setFreeDevice(incomingDevice);
                break;
            case DEVICE_DETACHED:
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
