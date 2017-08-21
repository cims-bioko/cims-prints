package broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.openandid.core.Controller;
import com.openandid.core.ScanningActivity;

/**
 * Created by sarwar on 1/7/15.
 */
public class ScannerReceiver extends BroadcastReceiver {
    private final String TAG = "ScannerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Caught Broadcast");
        try {
            synchronized (this) {
                Controller.mDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    ScanningActivity.waiting_for_permission = false;
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
