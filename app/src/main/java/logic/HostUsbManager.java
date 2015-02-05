package logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class HostUsbManager {

	private final String TAG = "HostUsbManager";
	private UsbManager mUsbManager;
	private UsbDevice dev;
	private HashMap<String,UsbDevice> deviceList;
	private boolean status_busy;
	private HashMap<String, String> scanner_info;
	
	public static final Set<Integer> vendor_blacklist = new HashSet<Integer>(){{
		add(6531); //NVIDIA Shield
		}};
	
	public HostUsbManager(UsbManager manager){
		mUsbManager = manager;
		find_device();
	}
	
	private void find_device(){
		status_busy = true;
		deviceList = mUsbManager.getDeviceList();
		dev = null;
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while(deviceIterator.hasNext()){
		    UsbDevice device = deviceIterator.next();
		    if(!HostUsbManager.vendor_blacklist.contains(device.getVendorId())){
		    	Log.i(TAG , "Product ID | " + Integer.toHexString(device.getProductId()));
			    Log.i(TAG , "Vendor ID | " + Integer.toHexString(device.getVendorId()));
			    Log.i(TAG, "Vendor = " + Integer.toString(device.getVendorId()));
			    dev = device;	
		    }
		    else{
		    	Log.i(TAG, "Vendor | " + Integer.toString(device.getVendorId()) + " | is blacklisted");
		    }
		    
		}
		if (dev != null){
			scanner_info = new HashMap<String, String>();
			scanner_info.put("ProductID", Integer.toHexString(dev.getProductId()));
			scanner_info.put("VendorID", Integer.toHexString(dev.getVendorId()));
		}
		else{
			scanner_info = null;
		}
		status_busy = false;
		
	}

    public boolean scannerConnected(){
        deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
            if(!HostUsbManager.vendor_blacklist.contains(device.getVendorId())){
                Log.i(TAG , "Product ID | " + Integer.toHexString(device.getProductId()));
                Log.i(TAG , "Vendor ID | " + Integer.toHexString(device.getVendorId()));
                Log.i(TAG, "Vendor = " + Integer.toString(device.getVendorId()));
                return true;
            }
            else{
                Log.i(TAG, "Vendor | " + Integer.toString(device.getVendorId()) + " | is blacklisted");
            }

        }
        return false;
    }

	public boolean get_status(){
		return status_busy;
	}
	
	public UsbDevice get_device(){
		return dev;
	}
	
	@SuppressWarnings("rawtypes")
	public HashMap get_device_info(){
		return scanner_info;
		
	}
}
