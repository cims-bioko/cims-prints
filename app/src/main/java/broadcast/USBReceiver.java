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
        if (intent.getAction().equals(USB_ON)) {
            UsbDevice incomingDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            Log.e(TAG, String.format("Caught Scanner PLUG signal | %s : %s ", incomingDevice.getProductId(), incomingDevice.getVendorId()));
            Log.e(TAG, "Ignoring, waiting for redispatch");
            /*
            try {
                Map<String, UsbDevice> deviceList = Controller.mUsbManager.getDeviceList();
                UsbDevice dev = null;
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    if (!HostUsbManager.vendor_blacklist.contains(device.getVendorId())) {
                        Log.i(TAG, "Product ID | " + Integer.toHexString(device.getProductId()));
                        Log.i(TAG, "Vendor ID | " + Integer.toHexString(device.getVendorId()));
                        Log.i(TAG, "Vendor = " + Integer.toString(device.getVendorId()));
                        dev = device;
                    } else {
                        Log.i(TAG, "Vendor | " + Integer.toString(device.getVendorId()) + " | is blacklisted");
                    }

                }
                if (dev != null) {
                    Log.e(TAG, "Live USBHostManager | Setting found scanner");
                    ScanningActivity.setFreeDevice(dev);
                }
            }catch (NullPointerException e){
                Log.e(TAG, "No live USBHostManager");
                ScanningActivity.setFreeDevice(incomingDevice);
            }
            */

        } else if (intent.getAction().equals(LOCAL_USB)) {
            UsbDevice incomingDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            Log.e(TAG, String.format("Caught LOCAL PLUG signal | %s : %s ", incomingDevice.getProductId(), incomingDevice.getVendorId()));
            ScanningActivity.setFreeDevice(incomingDevice);
        } else if (intent.getAction() == USB_OFF) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            Log.e(TAG, String.format("Caught Scanner UNPLUG signal | %s : %s", device.getProductId(), device.getVendorId()));


            try {
                if (!Controller.mHostUsbManager.scannerConnected()) {
                    Log.i(TAG, "Scanner really isn't connected!");
                    if (!Scanner.isInInit) {
                        Log.e(TAG, "Killing Scanner");
                        Controller.mScanner = null;
                        ScanningActivity.setFreeDevice(null);
                    } else {
                        Log.e(TAG, "Scanner is initializing, false positive");
                    }
                } else {
                    Log.i(TAG, "psych, scanner's here");
                    return;
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }

        }


    }
}
