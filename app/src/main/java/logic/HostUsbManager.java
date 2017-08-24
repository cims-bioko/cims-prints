package logic;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HostUsbManager {

    private static final String TAG = "HostUsbManager";

    /* make this private and consolidate client code */
    public static final Set<Integer> VENDOR_BLACKLIST = new HashSet<Integer>() {{
        add(6531); //NVIDIA Shield
        add(1478); //Moto Nexus 6
    }};

    private UsbManager mUsbManager;
    private HashMap<String, UsbDevice> deviceList;

    public HostUsbManager(UsbManager manager) {
        mUsbManager = manager;
    }

    public boolean isPermittedDeviceConnected() {
        deviceList = mUsbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            if (VENDOR_BLACKLIST.contains(device.getVendorId())) {
                Log.d(TAG, "ignoring blacklisted device " + device);
            } else {
                Log.d(TAG, "connected " + device);
                return true;
            }
        }
        return false;
    }
}
