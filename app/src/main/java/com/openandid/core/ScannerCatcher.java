package com.openandid.core;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

public class ScannerCatcher extends Activity {

    public static final String USB_ON_BROADCAST = "com.openandid.core.SCANNER_ATTACHED";
    public static UsbDevice device;
    public static final String TAG = "ScannerCatcher";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Create.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Parcelable usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                device = (UsbDevice) usbDevice;
                Intent broadcastIntent = new Intent(USB_ON_BROADCAST);
                broadcastIntent.putExtra(UsbManager.EXTRA_DEVICE, usbDevice);
                sendBroadcast(broadcastIntent);
            }
        }
        finish();
    }
}
