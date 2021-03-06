package broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.openandid.core.Controller;
import com.openandid.core.ScanningActivity;


public class ScannerReceiver extends BroadcastReceiver {

    private static final String TAG = "ScannerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            synchronized (this) {
                Controller.mDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    ScanningActivity.waitingForPermission = false;
                    Log.i(TAG, "scanner permission granted");
                } else {
                    Log.i(TAG, "permission denied for device " + Controller.mDevice);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("Error in BroadcastReceiver | %s", e.getMessage()));
        }
    }
}
