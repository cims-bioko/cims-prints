package broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.biometrac.core.Controller;

import logic.Scanner;

/**
 * Created by sarwar on 1/9/15.
 */
public class USBReceiver extends BroadcastReceiver{

    private final String USB_ON = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private final String USB_OFF = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private final String TAG = "USBReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()==USB_ON){
            Log.d(TAG, "Caught Scanner plug signal!!!");
        }
        else if (intent.getAction() == USB_OFF){
            Log.e(TAG, "Caught Scanner unplug signal!!!");
            if (!Scanner.isInInit){
                Log.e(TAG, "Killing Scanner");
                Controller.mScanner = null;
            }else{
                Log.e(TAG, "Scanner is initializing, false positive");
            }

        }

    }
}
