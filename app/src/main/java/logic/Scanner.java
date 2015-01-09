package logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

public class Scanner {

	public UsbDeviceConnection conn;
	public UsbInterface usbIf;
	public UsbManager manager;
	public UsbDevice dev;
	public UsbEndpoint epIN;
	public UsbEndpoint epOUT;
	public static boolean isInInit = false;
    public boolean ready;
	public static boolean finger_sensed = false;
	public static boolean scan_cancelled = false;
	
	private boolean makes_iso_templates = false;
	
	private HashMap<String, String> device_info;
	private HashMap<String, Bitmap> images;
	private HashMap<String, String> biometrics;
	private HashMap<String, String> iso_templates;
	
	public Scanner(UsbDevice device, UsbManager uManager){
		dev = device;
		manager = uManager;
		conn = manager.openDevice(device);
		device_info = new HashMap<String, String>();
		reset_dicts();
	}
	
	public void init_scanner(){
		ready = true;
	}

	public void reconnect(){
		conn = manager.openDevice(dev);
	}
	
	public void reset_dicts(){
		images = new HashMap<String, Bitmap>();
		biometrics = new HashMap<String, String>();
		iso_templates = new HashMap<String, String>();
	}
	
	public boolean run_scan(String finger){
		return false;
	}
	
	public boolean shut_down(){
		return false;
	}
	
	public void set_info(HashMap<String, String> info){
		device_info = info;
	}
	
	public HashMap<String, String> get_info(){
		return device_info;
	}
	
	public Set<String> get_image_list(){
		return images.keySet();
	}
	
	public Bitmap get_image(String name){
		if(images.containsKey(name)){
			return images.get(name);
		}
		else{
			return null;
		}
	}
	
	public void set_image(String name, Bitmap img){
		images.put(name, img);
		return;
	}

	public boolean get_ready() {
		// TODO Auto-generated method stub
		return ready;
	}
	
	public boolean finger_sensed(){
		return this.finger_sensed;
	}

	public void setBiometrics(String key, String value) {
		biometrics.put(key, value);
	}

	public String getBiometrics(String key) {
		return biometrics.get(key);
	}

	public Map<String,String> get_iso_biometrics(){
		return iso_templates;
	}
	
	public void set_iso_template(String key, String value){
		if (iso_templates == null){
			iso_templates = new HashMap<String, String>();
		}
		iso_templates.put(key, value);	
	}
	
	public String get_iso_template(String key){
		if (makes_iso_templates==true){
			return iso_templates.get(key);
		}else{
			return null;
		}
	}
	
	public Map<String,String> getBiometrics(){
		return biometrics;
	}

	public void cancel_scan() {
		scan_cancelled = true;
	}
}
