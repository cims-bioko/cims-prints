package logic;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.openandid.core.Controller;
import com.openandid.core.ScanningActivity;

public class Scanner_Lumidigm_Mercury extends Scanner{
	/*
	This class is the result of reverse engineering scanner protocols from wireshark-sniffed packets.
	It's terrible. Try not to do things like this in the future. Get an official driver to work from.
	 */

	String TAG = "LumidigmDriver";

    private static TriggerType currentTrigger = null;

    private HashMap<String, int[]> words;
	
	public Scanner_Lumidigm_Mercury(UsbDevice device, UsbManager manager) {
		super(device, manager);
		ready = false;
		words = new HashMap<String, int[]>();
		load_words();
		init_scanner();
	}
	
	private String bytes_to_string(byte[] in){
		StringBuilder sb = new StringBuilder();
        for (byte b : in) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
	}
	private String bytes_to_minimized_string(byte[] in){
		StringBuilder sb = new StringBuilder();
        for (byte b : in) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
	}
	
	
	
	
	@Override
	public boolean get_ready(){
		return ready;
	}
	
	//TODO DEP!!
	public static String getHexString(byte[] b){
		  String result = "";
		  for (int i=0; i < b.length; i++) {
		    result +=
		          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
		}
	
	@Override
	//Ugly fucking method to init scanner
	public void init_scanner(){
		new Thread(new Runnable() {
	        public void run() {
        		try{
                    isInInit = true;
		        	usbIf = dev.getInterface(0);
	        		Log.i("Endpoint Count",Integer.toString(usbIf.getEndpointCount()));
		        	
		        	epIN = null;
		        	epOUT = null;
		    		
		    		for (int i = 0; i < usbIf.getEndpointCount(); i++) {
		    			if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
		    				if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN){
		    					Log.i("USB Connection", "EP_IN Found @ "+Integer.toString(i));
		    					epIN = usbIf.getEndpoint(i);
		    					Log.i("epIN_INFO",Integer.toHexString(epIN.getAddress()));
		    				}
		    				else{
		    					Log.i("USB Connection", "EP_OUT Found @ "+Integer.toString(i));
		    					epOUT = usbIf.getEndpoint(i);
		    					Log.i("epOUT_INFO",Integer.toHexString(epOUT.getAddress()));
		    				}
		    					
		    			}
		    		}
		    		Log.i("Scanner Ops", "Starting Init Process");
		    	    	
		        	int k = 0;
		    		
		    		byte[] comm = write_buff("get_command");
		    		int c = 0;
                    while(true){
                        try{
                            k = conn.bulkTransfer(epOUT, comm, comm.length , 1000);
                            Log.i(TAG, String.format("get_command length %s bytes", k));
                            break;
                        }catch(Exception e){
                            c+=1;
                            Log.i(TAG,"Init Failure " + Integer.toString(c));
                            Thread.sleep(250);
                            if(conn != null){Log.e(TAG, "Connection isn't null... STRANGE!");}
                            else{Log.e(TAG, "Connection is null.");}
                            e.printStackTrace();
                            if (c>3){
                                throw new NullPointerException();
                            }else{
                                Log.i(TAG,"Flushing Buffer");
                                Controller.mDevice = ScanningActivity.getFreeDevice();
                                dev = Controller.mDevice;
                                conn = manager.openDevice(dev);
                                Log.i(TAG, "Trying init again");
                            }

                        }
                    }
                    set_scanner_settings(false);
                    isInInit = false;
                    ready = true;
        		}catch(Exception e){
        			scan_cancelled = true;
        			Log.i(TAG,"INIT FAILED");
                    isInInit = false;
        			e.printStackTrace();
        		}

	    	}
	    }).start();
		
	}

    protected void set_scanner_settings(){
        set_scanner_settings(false);
    }
    protected void set_scanner_settings(boolean instant){
        byte[] read_buffer;
        int k = 0;
        try{
            read_buffer = new byte[k];
        }catch(Exception e){
            read_buffer = new byte[1024];
            e.printStackTrace();
        }


        conn.bulkTransfer(epIN, read_buffer, k, 1000);
        read_buffer = new byte[1024];
        conn.bulkTransfer(epIN, read_buffer, 512, 1000);
        read_buffer = new byte[1024];

        byte[] comm = write_buff("mp1");
        k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
        if (instant){
            comm = write_buff("set_config_instant");
            currentTrigger = TriggerType.INSTANT;
        }else{
            comm = write_buff("set_config");
            currentTrigger = TriggerType.TRIGGERED;
        }

        k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
        conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
        read_buffer = new byte[1024];
        conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
        read_buffer = new byte[1024];

        comm = write_buff("mp3");
        k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);

        comm = write_buff("get_config");
        k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
        conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
        read_buffer = new byte[1024];
        conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
        //read_buffer = new byte[1024];
        Log.i("Scanner Ops", "Finished Settings");
    }
    @Override
    public boolean run_scan(final String finger){
        return run_scan(finger, true);
    }

	//Ugly Fucking method to send our magic words in the right order for scanner IO to work
	@Override
    public boolean run_scan(final String finger, boolean triggered){
        if (ready != true){
            return false;
        }
        ready = false;
        scan_cancelled = false;
        Scanner.finger_sensed = false;

        if (triggered && currentTrigger.equals(TriggerType.INSTANT)){
            set_scanner_settings(false);
        }else if (!triggered && currentTrigger.equals(TriggerType.TRIGGERED)){
            set_scanner_settings(true);
        }
		//MUST be run in the background with get_image returning the bitmap to the GUI in the onfinish method if the scan is successful.

		int k = 0;
		byte[] read_buffer = new byte[1024];
		
		byte[] comm = write_buff("mp2");
		k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
		//Log.i("len", Integer.toString(k));
		
		comm = write_buff("arm_trigger");
		k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
		//Log.i("len", Integer.toString(k));
		conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
		read_buffer = new byte[1024];
		conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
		read_buffer = new byte[1024];
		
		comm = write_buff("mp2");
		k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
		//Log.i("len", Integer.toString(k));
		
		boolean taken = false;
		while(true){
			if (scan_cancelled==true){
				return false;
			}
			if (((int) read_buffer[8] & 0xFF) == 4 && ((int) read_buffer[12] & 0xFF) == 1 && taken == false){
				taken = true;
				Log.i("scanner status","finger sensed");
				Scanner.finger_sensed = true;
			}
			if (taken == true){

				if (((int) read_buffer[8] & 0xFF) == 4 && ((int) read_buffer[12] & 0xFF) == 0){
					Log.i("scanner status","image ready!");
                    Log.i("found status:", read_buffer.toString());
					comm = write_buff("get_image");
					k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
					//Log.i("length", Integer.toString(k));
					read_buffer = new byte[1024];
					
					
					//Phantom Data
					k = conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
					
					byte[] image_holder = new byte[0];
					int last = 0;
					int count = 0;
					int read_size = 512;
					while(true){
						k = conn.bulkTransfer(epIN, read_buffer, read_size, 1000);
						//Log.i("length", Integer.toString(k));
						if (count == 192){
							count +=1;
							int cur = last + 278;
							byte[] tmp = new byte[last];
							System.arraycopy(image_holder, 0, tmp,0, image_holder.length);
							image_holder = new byte[cur];
							System.arraycopy(tmp, 0, image_holder, 0, last);
							System.arraycopy(read_buffer, 0, image_holder, tmp.length, 278);
							break;
								
						}
						count +=1;
						int cur = (count * read_size);
						byte[] tmp = new byte[last];
						System.arraycopy(image_holder, 0, tmp,0, image_holder.length);
						image_holder = new byte[cur];
						System.arraycopy(tmp, 0, image_holder, 0, last);
						System.arraycopy(read_buffer, 0, image_holder, tmp.length, read_size);	
						
						last = cur;
						
					}
					new Thread(new Runnable() {
				        public void run() {
				        	//Clean up on new thread so we don't lose time.
				        	try{
					        	byte[] read_buffer = new byte[1024];
					        	byte[] comm = write_buff("mp2");
								int k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
								comm = write_buff("put_finished");
								k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
								k = conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
								k = conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
	                            //Old finish with //ready = true;
					        	comm = null;
								comm = write_buff("mp2");
	                            k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
					        	comm = null;
	                            comm = write_buff("get_template");
	                            k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
	                            //Junk read
	                            k = conn.bulkTransfer(epIN, read_buffer, 512, 1000);
	                            //Template Read
								k = conn.bulkTransfer(epIN, read_buffer, 512, 1000);
	                            //LOG output
								Log.i(TAG,"Flushing the scanner's memory");
								byte[] fake_buff = new byte[512];
								for(int i = 0; i < 4; i++){
									k = conn.bulkTransfer(epIN, fake_buff, 512, 1000);
								}
								Log.i(TAG,"Done Flushing");
								byte b_46 = Byte.parseByte("70", 10);
								byte b_4d = Byte.parseByte("77", 10);
								byte[] t_start = new byte[2];
								int template_start_position = 0;
								t_start[0] = b_46;t_start[1] = b_4d;
								//Try the shortcut first
								if (Arrays.equals(Arrays.copyOfRange(read_buffer, 16, (18)),t_start)==true){
									template_start_position=16;
									Log.i(TAG,"WooHoo! Template start shortcut!");
								}
								//Ok now we have to hunt for the start of the template
								else{
									for(int i = 0; i < 100; i++){
										try{
											byte[] sub = Arrays.copyOfRange(read_buffer, i, (i+2));
											if(Arrays.equals(sub, t_start)){
												template_start_position = i;
												Log.i(TAG,"Start position found at: " + Integer.toString(i));
												break;
											}
											else{
												//Log.i("TAG",bytes_to_string(sub)+"!="+bytes_to_string(t_start));
											}
										}catch(Exception e){
											Log.i(TAG,"error comparing bytes");
										}
									}
								}
								byte[] temp_header = Arrays.copyOfRange(read_buffer, template_start_position, (template_start_position+31));
								int minutia_count = temp_header[29];
								int body_copy_length =(6 * minutia_count);
								byte[] _temp_body = Arrays.copyOfRange(read_buffer, (template_start_position+30), (template_start_position+30+body_copy_length));
								byte[] temp_body = Arrays.copyOf(_temp_body, (_temp_body.length + 2));
								//indicating no extended data
								temp_body[temp_body.length-1] = (byte)0;
								temp_body[temp_body.length-2] = (byte)0;
								byte[] header_start = Arrays.copyOfRange(temp_header, 0 , 8);
								byte[] header_finish = Arrays.copyOfRange(temp_header, 14 , 30);
								int new_total_length = 28+ temp_body.length;
								ByteBuffer b = ByteBuffer.allocate(4);
								b.order(ByteOrder.BIG_ENDIAN);
								b.putInt(new_total_length);
								byte[] length_seq = b.array();
								byte[] iso_template = new byte[new_total_length];
								int point = 0;
								System.arraycopy(header_start, 0, iso_template, 0, header_start.length);
								point += header_start.length;
								System.arraycopy(length_seq, 0, iso_template, point, length_seq.length);
								point += length_seq.length;
								System.arraycopy(header_finish, 0, iso_template, point, header_finish.length);
								point += header_finish.length;
								System.arraycopy(temp_body, 0, iso_template, point, temp_body.length);
								String iso_min_template = bytes_to_minimized_string(iso_template);
								set_iso_template(finger, iso_min_template);
	                            Log.i("Template:",iso_min_template);
								ready = true;
				        	}catch(Exception e){
				        		Log.i(TAG,"iso templating failed");
                                set_iso_template(finger, null);
				        		ready = true;
				        		e.printStackTrace();
				        	}
						}
			        }).start();
					

					byte[] final_image = new byte[98560];
					System.arraycopy(image_holder, 20, final_image, 0, 98560);
					
					image_holder = null;
					
					//Convert byte array from scanner to appropriately colored int array for stupid bitmap setPixel method
					int[] image_ints = new int[98560];
					
					for (int i = 0; i < final_image.length; i++){
						int n = ( final_image[i] & 0xff );
						int c = Color.rgb(n, n, n);
						image_ints[i] = c;
					}
					
					final_image = null;
					
					Bitmap complete_image;
					complete_image = Bitmap.createBitmap(280,352, Bitmap.Config.RGB_565);
					
					int pix = 0;
					
					for (int y = 0; y < 352; y++){
						for (int x = 279; x >= 0; x--){
							complete_image.setPixel(x, y, (0xFFFFFF - image_ints[pix]));
							
							pix +=1;
						}
					}
					
					set_image(finger, complete_image);
					Scanner.finger_sensed = false;
					break;
				}
                Log.i(TAG, "Waiting for sensor");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else{
                Log.i(TAG, "Waiting for finger");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
			
			read_buffer = new byte[1024];
			comm = write_buff("get_busy");
			k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
			conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
			read_buffer = new byte[1024];
			conn.bulkTransfer(epIN, read_buffer, 1024, 1000);
			comm = write_buff("mp2");
			k = conn.bulkTransfer(epOUT, comm , comm.length, 1000);
			if(((int) read_buffer[12] == 11)){
                Log.e(TAG, "Scan Error!");
                return false;
            }
		}
		Log.i("Scanner Status","Scan Finished");
		return true;
	}
	
	@Override
	public boolean shut_down(){
		return false;
	}
	
	//Load byte_phrase dictionary for scanner commands
	private void load_words(){
		words.put("mp1", new int[] {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x52, 0x00 ,0x00, 0x00});
		words.put("mp2", new int[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0e, 0x00 ,0x00, 0x00});
		words.put("mp3", new int[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x12, 0x00 ,0x00, 0x00});
		words.put("arm_trigger", new int[]{0x0d,0x56, 0x47,0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
        /*
        #older instruction set with templating turned off, timeout at default 15
		int[] set_str = 
               {0x0d, 0x56, 0x4d, 0x00, 0x00, 0x00, 0x00, 0x00,   0x44, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00,
				0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x11, 0x00,   0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x03, 0x00,
				0x00, 0x00, 0x2c, 0x01, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x01, 0x00};
        */
        //Templating turned ON, timeout to 60.

        int[] set_str =
               {0x0d, 0x56, 0x4d, 0x00, 0x00, 0x00, 0x00, 0x00,   0x44, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x3c, 0x00, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00,
				0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x11, 0x00,   0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x03, 0x00,
				0x00, 0x00, 0x2c, 0x01, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x03, 0x00};

        int[] set_str_inst =
               {0x0d, 0x56, 0x4d, 0x00, 0x00, 0x00, 0x00, 0x00,   0x44, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x0f, 0x00, 0x01, 0x00, 0x00, 0x00,   0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x11, 0x00,   0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x03, 0x00,
                0x00, 0x00, 0x2c, 0x01, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00};
		words.put("set_config", set_str);
        words.put("set_config_instant", set_str_inst);
		words.put("get_config", new int[]{0x0d,0x56,0x49,0x00,0x00,0x00,0x00,0x00,0x04,0x00,0x00,0x00, 0x80,0x00,0x00,0x00,0xc8,0x00});
		words.put("get_command", new int[]{0x0d,0x56, 0x4c, 0x00, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
		words.put("get_busy", new int[]{0x0d,0x56, 0x48, 0x00, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
		words.put("get_status", new int[]{0x0d,0x56, 0x4a, 0x00, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
		words.put("get_image", new int[]{0x0d,0x56, 0x43, 0x00, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00});
		words.put("put_finished", new int[] {0x0d, 0x56, 0x85, 0x00, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x00, 0x00, 0x0e, 0x00});
        words.put("get_template", new int[] {0x0d, 0x56, 0x45, 0x00, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x00, 0x00, 0x05, 0x05});		

		return;
	}
	
	
	private byte[] write_buff(String key){
		int[] int_phrase = words.get(key);
		ByteBuffer messageBuff = ByteBuffer.allocate(int_phrase.length * 4);
		IntBuffer intBuff = messageBuff.asIntBuffer();
		intBuff.put(int_phrase);
		byte[] array = messageBuff.array();
		byte[] out = new byte[array.length/4];
		int c = 0;
		for (int i=3; i < array.length; i+=4){
			out[c] = array[i];
			c+=1;
		}
		return out;
	}
	
	//TODO Depricate
	//For Debugging
	private void print_buff(byte[] phrase){
		StringBuffer hexString = new StringBuffer();
		for (byte b : phrase){
			String hex = Integer.toHexString(0xFF & b);
			if (hex.length() == 1) {
			    // could use a for loop, but we're only dealing with a single byte
			    hexString.append('0');
			}
			hexString.append(hex);
		}

	}

    private enum TriggerType{
        INSTANT,
        TRIGGERED;
    }
}
