package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Parcelable;

import static com.openandid.core.Constants.SCANNER_ATTACHED;

public class ScannerCatcher extends Activity {


    public static UsbDevice device;
    public static final String TAG = "ScannerCatcher";

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Parcelable usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                device = (UsbDevice) usbDevice;
                Intent broadcastIntent = new Intent(SCANNER_ATTACHED);
                broadcastIntent.putExtra(UsbManager.EXTRA_DEVICE, usbDevice);
                sendBroadcast(broadcastIntent);
            }
        }
        finish();
    }
}
