package com.biometrac.core;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;

/**
 * Created by sarwar on 2/5/15.
 */
public class ScannerCatcher extends Activity {

    public static final String USB_ON_BROADCAST = "com.biometrac.core.SCANNER_ATTACHED";
    public static UsbDevice device;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if(intent != null){
            if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
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
