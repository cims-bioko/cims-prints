package com.biometrac.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


import logic.FingerType;
import logic.HostUsbManager;
import logic.NativeClass;
import logic.NativeSetup;
import logic.Scanner;
import logic.Scanner_Lumidigm_Mercury;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

public class ScanningActivity extends Activity{

	//Scanner restart variables
	//GETTING USB Permission
	private BroadcastReceiver mUsbReceiver;
	private PendingIntent mPermissionIntent;
	public static boolean waiting_for_permission = true;
	private static final String ACTION_USB_PERMISSION =
	    "com.biometrac.screentest.ScanningActivity.USB_PERMISSION";
	
	FingerType left_finger;
	FingerType right_finger;
    Map<String, Bitmap> scanImages;
	ImageButton left_thumb_btn;
	ImageButton right_thumb_btn;
	Map<String,String> nfiqScores;
	Map<String,String> template_cache;
	Map<String,String> iso_template_cache;
    ImageButton proceed;
	ImageButton skip;
	boolean easy_skip = false;
	ImageView restart_scanner;
	Button pop_cancel;
	View pview;
	PopupWindow popUp;
	TextView pop_prompt;
	Context mContext = this;
	Controller c;
	Layout mainView;
	Map<String,String> opts = null;
	Map<String,Object> binary_opts = null;
    operation_type type;
    private boolean setupDone = false;
	
	
	private static String TAG = "ScanningActivity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setCorrectContentView(getResources().getConfiguration());

        scanImages = new HashMap<String, Bitmap>();
        nfiqScores = new HashMap<String,String>();
        template_cache = new HashMap<String,String>();
        iso_template_cache = new HashMap<String,String>();
        load_options(getIntent().getExtras());
        //If we have options from the intent Bundle, parse and enact them
        if (opts != null || binary_opts != null){
        	parse_options();	
        }else{
        	default_options();
        }
        
        //TODO Enable or edit exit button
        //super.set_up_bar(this);
        setupUI();

        
	}

    private void setupUI() {
        final LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pview = inflater.inflate(R.layout.pop_up_wait,(ViewGroup)findViewById(R.layout.scanner_view_layout));
        popUp = new PopupWindow(pview);
        popUp.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //popUp.setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_trans_red_box));
        pop_prompt = (TextView) pview.findViewById(R.id.pop_up_wait_title);

        pop_cancel = (Button) pview.findViewById(R.id.pop_up_wait_cancel_btn);

        left_thumb_btn = (ImageButton) findViewById(R.id.scanner_btn_finger_1);
        left_thumb_btn.setImageDrawable(getResources().getDrawable(left_finger.finger_image_location));
        TextView left_thumb_txt = (TextView) findViewById(R.id.scanner_txt_finger_1_title);
        left_thumb_txt.setText(left_finger.finger_name);
        left_thumb_btn.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(Controller.mScanner==null){
                    restart_scanner(arg0);
                    Toast.makeText(mContext, getResources().getString(R.string.scanner_not_connected), Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    HashMap<String, UsbDevice> deviceList = Controller.mUsbManager.getDeviceList();
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    if (deviceIterator.hasNext()==false){
                        Log.i(TAG,"Device Unplugged");
                        restart_scanner(arg0);
                        Toast.makeText(mContext, getResources().getString(R.string.scanner_not_connected), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (Controller.mScanner.get_ready()==true){
                    //TODO
                    pop_prompt.setText(getResources().getString(R.string.scan_prompt)+" "+ left_finger.finger_name);
                    popUp.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    popUp.showAtLocation(arg0, Gravity.CENTER_VERTICAL, 0, 0);
                    //TODO
                    final FingerScanInterface f = new FingerScanInterface(left_finger.finger_key, Controller.mScanner, left_thumb_btn, arg0);
                    pop_cancel.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cancel_scan(f, v);
                        }
                    });
                    pop_cancel.setVisibility(View.VISIBLE);
                    popUp.update();
                    f.execute();
                }
            }
        });

        right_thumb_btn = (ImageButton) findViewById(R.id.scanner_btn_finger_2);
        right_thumb_btn.setImageDrawable(getResources().getDrawable(right_finger.finger_image_location));
        TextView right_thumb_txt = (TextView) findViewById(R.id.scanner_txt_finger_2_title);
        right_thumb_txt.setText(right_finger.finger_name);
        right_thumb_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(Controller.mScanner==null){
                    restart_scanner(arg0);
                    Toast.makeText(mContext, getResources().getString(R.string.scanner_not_connected), Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    HashMap<String, UsbDevice> deviceList = Controller.mUsbManager.getDeviceList();
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    if (deviceIterator.hasNext()==false){
                        Log.i(TAG,"Device Unplugged");
                        restart_scanner(arg0);
                        Toast.makeText(mContext, getResources().getString(R.string.scanner_not_connected), Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                if (Controller.mScanner.get_ready()==true){
                    pop_prompt.setText("Please Scan the "+ right_finger.finger_name);
                    popUp.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    popUp.showAtLocation(arg0, Gravity.CENTER_VERTICAL, 0, 0);
                    final FingerScanInterface f = new FingerScanInterface(right_finger.finger_key, Controller.mScanner, right_thumb_btn, arg0);
                    pop_cancel.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cancel_scan(f, v);
                        }
                    });
                    pop_cancel.setVisibility(View.VISIBLE);
                    popUp.update();
                    f.execute();
                }
            }
        });

        proceed = (ImageButton) findViewById(R.id.scan_btn_proceed);
        proceed.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(Controller.mScanner==null){
                    restart_scanner(arg0);
                    Toast.makeText(mContext, getResources().getString(R.string.scanner_not_connected), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Controller.mScanner.get_ready()==true){
                    check_nfiq_scores();
                }
                else{
                    Toast.makeText(mContext, getResources().getString(R.string.please_wait), Toast.LENGTH_SHORT).show();
                }
            }
        });

        skip = (ImageButton) findViewById(R.id.scan_btn_skip);
        if (easy_skip == false){
            skip.setVisibility(View.GONE);
        }else{
            skip.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //TODO add confirmation?
                    finish_cancel();
                }
            });

        }


        restart_scanner = (ImageView) findViewById(R.id.headbar_scanner_btn);
        restart_scanner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                unplug_scanner(v);
            }
        });
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    private void setCorrectContentView(Configuration configuration) {
        switch(configuration.orientation){
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.i(TAG, "landscape");
                setContentView(R.layout.scanner_flex_layout_landscape);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                Log.i(TAG, "portrait");
                setContentView(R.layout.scanner_flex_layout);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "Redrawing for Rotation");
        setCorrectContentView(newConfig);
        if (opts != null || binary_opts != null){
            parse_options();
        }else{
            default_options();
        }
        setupUI();
        publish_nfiq();
        for (String key: scanImages.keySet()){
            Log.d(TAG, "found in scan Images: " + key);
        }
        Bitmap leftScanImage = scanImages.get(left_finger.finger_key);
        if (leftScanImage != null) {
            left_thumb_btn.setImageBitmap(leftScanImage);
            Log.i(TAG, "Drew left Image from previous");
        } else {
            Log.i(TAG, "Left image is null");
        }
        Bitmap rightScanImage = scanImages.get(right_finger.finger_key);
        if (rightScanImage != null) {
            right_thumb_btn.setImageBitmap(rightScanImage);
            Log.i(TAG, "Drew right Image from previous");
        } else {
            Log.i(TAG, "Right image is null");
        }
    }

    @Override
	public void onBackPressed() {
	    finish_cancel();
	}
	
	private void load_options(Bundle extras) {
		 try{
			 if (opts == null || binary_opts == null){
				 opts = new HashMap<String,String>();
				 binary_opts = new HashMap<String, Object>();
			 }
			 Iterator<String> b_keys = extras.keySet().iterator();
			 while (b_keys.hasNext()){
				 String key = b_keys.next();
				 try{
					 String val = extras.getString(key);
					 if (val == null){
						 throw new java.lang.Exception();
					 }
					 Log.i(TAG,"Key " +key+" val " + val);
					 opts.put(key, val);	
				 }catch(Exception e){
					 Log.i(TAG,"Couldn't get string value for key "+key);
					 try{
						 binary_opts.put(key, extras.getParcelable(key));
					 }catch(Exception e2){
						 Log.i(TAG,"Couldn't get binary value for key "+key);
					 }
				 }
			 }
		 }catch(Exception e){
			 Log.i(TAG,"Error parsing options from bundle");
			 e.printStackTrace();
			 if (opts.isEmpty()==true){
				 opts = null;	 
			 }
			 if (binary_opts.isEmpty() == true){
				 binary_opts = null;
			 }
	     }
	}
	
	private void parse_options() {
		//TODO Write API for .SCAN broadcast
		try{
	    	Bitmap bmp = (Bitmap) binary_opts.get("image");
	    	if (bmp == null){
	    		throw new NullPointerException();
	    	}
	    	/*
            Log.i(TAG,"new image found");
	    	ImageView i_view = (ImageView) findViewById(R.id.scanner_view_prompt_image);
	    	i_view.setImageBitmap(bmp);
	    	*/
        }catch (Exception e){
        	ImageView i_view = (ImageView) findViewById(R.id.scanner_view_prompt_image);
	    	i_view.setVisibility(View.GONE);
        	Log.i(TAG,"Couldn't load image");
        	Log.i(TAG,opts.toString());
        }
        try{
        	String prompt = opts.get("prompt");
        	TextView prompt_text = (TextView) findViewById(R.id.scanner_view_prompt);
        	if (prompt_text != null){
        		prompt_text.setText(prompt);
        	}
        }catch (Exception e){
        	Log.i(TAG,"No prompt to load");
        }
        try{
        	String easy_skip_str = opts.get("easy_skip");
        	if (easy_skip_str != null){
        		easy_skip = Boolean.parseBoolean(easy_skip_str);
			}else{
				Log.i(TAG,"easy_skip_str was null...");
			}
        }catch (Exception e) {
        	Log.i(TAG,"Couldn't parse for Easy Skip");
		}
        try{
        	String left_finger_assignment = opts.get("left_finger_assignment");
        	if (left_finger_assignment == null){
        		throw new NullPointerException();
        	}
        	left_finger = new FingerType(left_finger_assignment);
        }catch (Exception e) {
        	Log.i(TAG,"No assignment for left_finger, defaulting to thumb");
			left_finger = new FingerType("left_thumb");
		}
        try{
        	String right_finger_assignment = opts.get("right_finger_assignment");
        	if (right_finger_assignment == null){
        		throw new NullPointerException();
        	}
        	right_finger = new FingerType(right_finger_assignment);
        }catch (Exception e) {
        	Log.i(TAG,"No assignment for right_finger, defaulting to thumb");
        	right_finger = new FingerType("right_thumb");
		}
		
	}
	
	private void default_options() {
		//TODO Write API for .SCAN broadcast
		easy_skip = false;
		left_finger = new FingerType("left_thumb");
    	right_finger = new FingerType("right_thumb");
	}

	

	@Override
	protected void onPause() {
	 	if (waiting_for_permission == false){
            /*
	 		try{
	 			unregisterReceiver(mUsbReceiver);
	 			Log.i(TAG,"Unregistered receiver!");
	 		}catch(Exception e){
	 			Log.i(TAG,"Couldn't unregister receiver");
	 		}
             */
	 	}
		super.onPause();
	}
	
	/*
	@Override
	protected void onRestart() {
		registerReceiver(KillReceiver, new IntentFilter("com.biometrac.core.KILL"));
		super.onRestart();
	}
	
	@Override
	protected void onDestroy() {
		try{
			unregisterReceiver(KillReceiver);
 		}catch(Exception e){
 		}	 
		super.onDestroy();
	}
	*/
	public void publish_nfiq(){
		String l_score = nfiqScores.get(left_finger.finger_key);
		String r_score = nfiqScores.get(right_finger.finger_key);
		try{
			if (Integer.parseInt(r_score)>2){
				//right_thumb_btn.setBackgroundColor(Color.RED);
				right_thumb_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_shape_red_round));
			}else{
				//right_thumb_btn.setBackgroundColor(Color.GREEN);
				right_thumb_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_shape_green_round));
			}
		}catch(Exception e){
			//parse error for no score...
			Log.i(TAG,"No Right Score...");
		}
		try{
			if (Integer.parseInt(l_score)>2){
				//left_thumb_btn.setBackgroundColor(Color.RED);	
				left_thumb_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_shape_red_round));
			}else{
				//left_thumb_btn.setBackgroundColor(Color.GREEN);
				left_thumb_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_shape_green_round));
			}
		}catch(Exception e){
			//parse error for no score...
			Log.i(TAG,"No Left Score...");
		}
		
	}
	
	//On press of confirm in skip dialog
	public void skip_scanning(){
		finish_cancel();
	}
	
	public void check_nfiq_scores(){
		Iterator<String> keys = nfiqScores.keySet().iterator();
		boolean pass = true;
		if (nfiqScores.keySet().size() < 2){
			Toast t = Toast.makeText(mContext, getResources().getString(R.string.scan_all_fingers), Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			t.show();
			Log.i("scanning","quality not enough keys");
			pass = false;
		}
		else{
			while(keys.hasNext()){
				String key = keys.next();
				int score = Integer.parseInt(nfiqScores.get(key));
				if (score > 2){
					Log.i("scanning","score > 2");
					Log.i("scanning","score = "+score);
					pass = false;
					Toast t = Toast.makeText(mContext, getResources().getString(R.string.scan_quality), Toast.LENGTH_LONG);
					t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					t.show();
				}
			}
		}
		if (!pass){
			//
			if (easy_skip == false){
				skip.setVisibility(View.VISIBLE);
				skip.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						finish_cancel();
						//TODO Reimplement exit with cancelled return code...
						/*
						e_pop.showAtLocation(callback, Gravity.CENTER_VERTICAL, 0, 0);
						e_exit.setOnClickListener(new Button.OnClickListener() {
							@Override
							public void onClick(View v) {
								skip_scanning();
							}
						});
						e_prompt.setText("Are you sure that you want\nto Skip Fingerprint Scanning?");
						e_pop.update();
						*/
						
					}
				});
			}
			
			
		}
		else{
			if (easy_skip == false){
				skip.setVisibility(View.GONE);
				skip.setOnClickListener(null);
			}
			//skip.setVisibility(View.GONE);
			//skip.setOnClickListener(null);
			Map<String,String>biometrics = Controller.mScanner.getBiometrics();
			Map<String,String> iso_set = Controller.mScanner.get_iso_biometrics();
			for(String key: biometrics.keySet()){
				//TODO return keyset to caller intent
				/*
				Controller.mParticipant.put_info(key, biometrics.get(key));
				*/
			}
			try{
				
				if (iso_set != null){
					for (String k:iso_set.keySet()){
						String field_name = k +"_iso";
						//TODO return keyset to caller intent
						/*
						Controller.mParticipant.put_info(field_name, iso_set.get(k));
						*/
					}
				}
			}catch (Exception e){
				Log.i(TAG,"Couldn't set iso_templates in part_info");
			}
			
					/*
					Log.i("Debug","Tag Entered");
					Map<String,String>bmt = Controller.mParticipant.get_biometrics();
					Log.i(TAG,"to parse for boolean "+ opts.get("name_fingers"));
					String rename = opts.get("name_fingers");
					Map<String,String> out = new HashMap<String,String>();
					if (rename != null){
						if (rename.equals("true") == true){
							Log.i(TAG,"WTF WE'RE HERE!");
							for(String key: biometrics.keySet()){
								Log.i("Debug","Found Key: "+key);
								Log.i("Debug","Val: "+biometrics.get(key));
								if (opts.get(key) != null){
									String new_name = opts.get(key);
									Log.i(TAG, "New name! :" +new_name);
									out.put(new_name, biometrics.get(key));	
								}else{
									Log.i(TAG,"No rename rule for " + key);
								}
								
							}
							*/
			Map<String,String> out = new HashMap<String,String>();
			for(String key: iso_set.keySet()){
				String iso_key = key+"_iso";
				Log.i("Debug","Found Key: "+iso_key);
				Log.i("Debug","Val: "+iso_set.get(key));
				if (opts != null){
					if (opts.get(key) != null){
						String new_name = opts.get(iso_key);
						Log.i(TAG, "New name! :" +new_name);
						out.put(new_name, iso_set.get(key));
					}else{
						Log.i(TAG,"No rename rule for " + key);
						out.put(key, iso_set.get(key));
					}
				}else{
					Log.i(TAG,"No rename rule for " + key);
					out.put(key, iso_set.get(key));
				}
				
			}
			finish_ok(out);
		}
	}
	
	public void cancel_scan(FingerScanInterface inter, View parent){
		inter.cancel(true);
	}
	
	public void restart_scanner(View parent){
		//TEST
		Log.i(TAG,"Starting restart");
		ScannerSetup ss = new ScannerSetup(this, parent);
		ss.execute();
	}
	
	protected void unplug_scanner(View parent){
		Log.i(TAG,"Starting Unplug");
		ScannerUnplug s = new ScannerUnplug(parent);
		s.execute();
	}
	
	protected void test_workflow_skip(){
		Log.i(TAG,"TEST WORKFLOW! skipping biometrics");
		finish_cancel();
	}
	
	protected void finish_ok(Map<String,String> output){
		Log.i(TAG,"Finish OK -- Start");
		Log.i(TAG,"package keys -- " +output.keySet().toString());
		Intent i = new Intent();
		Iterator<String> keys = output.keySet().iterator();
		while(keys.hasNext()==true){
			String k = keys.next();
			i.putExtra(k, output.get(k));
			Log.i(TAG,"Loading output-- k: " + k +" v: "+ output.get(k));
		}
		setResult(RESULT_OK, i);
		this.finish();
	}
	
	protected void finish_cancel(){
		Intent i = new Intent();
		setResult(RESULT_CANCELED, i);
		this.finish();
	}
	
	public void stop_scanner_restart(){
		
	}
	
	private class FingerScanInterface extends AsyncTask<Void, Void, Void> {
		ImageButton view;
		View parent;
		Scanner mScanner;
		String finger;
		boolean success = false;
        boolean reconnect = false;
        Drawable image;
		
	private FingerScanInterface(String name, Scanner scanner, ImageButton spot, View parent) {
			super();
			this.parent = parent;
			view = spot;
			finger = name;
	        mScanner = scanner;
		}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (Controller.mScanner == null){
                Log.d(TAG, "Reconnect pre!");
                reconnect = true;
                cancel(true);
            }
        }

        @Override
		protected Void doInBackground(Void... params) {
            if (isCancelled()){
                Log.d(TAG, "Canceled background!");
                success = false;
                return null;
            }

			new Thread(new Runnable() {
		        public void run() {
		        	try{
			        	while (Controller.mScanner.finger_sensed()==false){
			        		if (isCancelled()==true){
			        			Controller.mScanner.cancel_scan();
			        			return;
			        		}
			        		if (Controller.mScanner.get_ready()==true){
			        			break;
			        		}
			        		try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	
			        	}
		        	}catch(Exception e){
		        		if (Controller.mScanner != null){
		        			Controller.mScanner.cancel_scan();
		        		}
		        		return;
		        	}
		        	runOnUiThread(new Runnable() {
		                @Override
		                public void run() {
		                	//FIX
		                	popUp.dismiss();
		                	pop_prompt.setText(getResources().getString(R.string.scan_complete));
		                	Button b = pop_cancel;
		                	b.setVisibility(View.GONE);
		                	popUp.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		                	//popUp.setBackgroundDrawable(grey_box);
		                	popUp.showAtLocation(parent, Gravity.CENTER_VERTICAL, 0, 0);
		                	popUp.update();
		                }
		            });
		        }
	        }).start();
			success = mScanner.run_scan(finger);
			return null;
		}
		
		@Override
		protected void onCancelled() {
			popUp.dismiss();
			unplug_scanner(parent);
			super.onCancelled();
		}
		
		protected void onPostExecute(Void res) {
			if (success==true){
				//in case of disconnect, cache items locally
				template_cache = Controller.mScanner.getBiometrics();
				iso_template_cache = Controller.mScanner.get_iso_biometrics();
				
				Log.i("PostExec", "Started");
				Bitmap result = mScanner.get_image(finger);
		    	Log.i("bmp_info", Integer.toString(result.getHeight()));
		    	Log.i("bmp_info", Integer.toString(result.getWidth()));
		    	int starting_image_height = view.getHeight();
				int starting_image_width = view.getWidth();
				
				Log.i(TAG, "Max Image width: " + starting_image_width + " Height: " + starting_image_height);
				
				int new_height = (int) Math.round(starting_image_height*.92);
		    	int new_width = (int) Math.round(starting_image_width*.92);
		    	Log.i(TAG, "new height: " + Integer.toString(new_height));
		    	Log.i(TAG, "new width: " +Integer.toString(new_width));
		    	
		    	Bitmap scaled = Bitmap.createScaledBitmap(result, new_width, new_height, true);
                scanImages.put(finger, scaled);
		    	Log.i(TAG, "Scaled BMP width: " + Integer.toString(scaled.getWidth()));
		        view.setScaleType(ScaleType.CENTER);
		    	view.setImageBitmap(scaled);
				WritePhoto w = new WritePhoto(result, finger);
		        w.execute();
			}else{
				Log.i(TAG,"Scan failed!");
				popUp.dismiss();
			}
            if (reconnect){
                Log.d(TAG, "Reconnect post!");
                unplug_scanner(view);
            }
			
	    }
	}
	private class WritePhoto extends AsyncTask<byte[], Void, String> {
		
		Bitmap img;
		String finger;
		String template;
		public WritePhoto(Bitmap img, String finger){
			super();
			this.img = img;
			this.finger = finger;
			template = "";
		}
		protected void onPreExecute() {
		}
		@Override
		protected String doInBackground(byte[]... data) {

			String image1Path = "/data/data/com.biometrac.core/nbis/img/"+finger+"1.jpg";
			String image2Path = "/data/data/com.biometrac.core/nbis/img/"+finger+"2.jpg";
			OutputStream fOut = null;
			File file = new File(image1Path);
			try {
				fOut = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			img.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
			try {
				fOut.flush();
				fOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			NativeClass.jpegtran(image1Path, image2Path);
			NativeSetup.dropPermission(image2Path);
			String nfiqOut = NativeClass.nfiq(image2Path, false);
			template = NativeClass.mindtct(finger);
			while (Controller.mScanner.get_ready() == false){
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i(TAG,"Waiting for scanner to finish...");
			}
			return nfiqOut;
		}
		protected void onPostExecute(final String nfiqOut) {
				StringTokenizer nfiqTokens = new StringTokenizer(nfiqOut);
				String[] nfiqTerm = new String[15];
				int termCount = 0;
				while(nfiqTokens.hasMoreTokens()){
					nfiqTerm[termCount] = nfiqTokens.nextToken();
					termCount +=1;
				}
			System.out.println(nfiqTerm[11]);
			nfiqScores.put(finger, nfiqTerm[11]);
			Controller.mScanner.setBiometrics(finger, template);
			publish_nfiq();
	        popUp.dismiss();

		}
		
	}
	
	private class ScannerUnplug extends AsyncTask<Void, Void, Void> {
		View parent;
		private ScannerUnplug(View parent){
			this.parent = parent;
		}
		@Override
		protected void onPreExecute() {
			//popUp.setBackgroundDrawable(grey_box);
			pop_prompt.setText(getResources().getString(R.string.unplug_scanner));
			pop_cancel.setVisibility(View.GONE);
			popUp.showAtLocation(parent, Gravity.CENTER_VERTICAL, 0, 0);
			popUp.update();
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(Void... params) {
            HashMap<String, UsbDevice> deviceList;
            Iterator<UsbDevice> deviceIterator;
            while(true){
                try {
                    deviceList = Controller.mUsbManager.getDeviceList();
                }catch (NullPointerException e){
                    Log.i(TAG, "No UsbManager Active");
                    return null;
                }
                deviceIterator = deviceList.values().iterator();
                if (deviceIterator.hasNext()==false){
                	Log.i(TAG,"Device Unplugged");
                	break;
                }else{
                    boolean foundValid = false;
                    while (deviceIterator.hasNext()){
                        UsbDevice device = deviceIterator.next();
                        if(!HostUsbManager.vendor_blacklist.contains(device.getVendorId())){
                            foundValid = true;
                        }
                    }
                    if (!foundValid){
                        break;
                    }
                }
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
			
		}
		@Override
		protected void onPostExecute(Void result) {
			popUp.dismiss();
			restart_scanner(parent);
			super.onPostExecute(result);
		}
	}
	
	private class ScannerSetup extends AsyncTask<Void, Void, Void> {
		private Context oldContext;
		private View parent;
		ScannerSetup a_process;
		public ScannerSetup(Context context, View parent){
			super();
			a_process= this;
			this.parent = parent;
			oldContext = context;
		}
		protected void onPreExecute() {
			pop_prompt.setText(getResources().getString(R.string.attach_scanner));
			pop_cancel.setVisibility(View.VISIBLE);
			pop_cancel.setOnClickListener(new Button.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					a_process.cancel(true);
				}
			});
			//pop_cancel.setVisibility(View.GONE);
			//FIX
			popUp.showAtLocation(parent, Gravity.CENTER_VERTICAL, 0, 0);
			popUp.update();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			//TODO kill getApp
			Controller.mUsbManager = null;
			Controller.mHostUsbManager = null;
			Controller.mDevice = null;
			Controller.mUsbManager = (UsbManager) oldContext.getSystemService(Context.USB_SERVICE);
			Controller.mHostUsbManager = new HostUsbManager(Controller.mUsbManager);
			waiting_for_permission = true;
			while(true){
            	try{
            		if (isCancelled() == true){
    					Log.i(TAG, "Fingerprint Scanner Not Needed -- Canceling Receiver");
    					Controller.mHostUsbManager = null;
    					Controller.mDevice = null;
    					throw new Exception("FP Canceled!");
    				}
            		Thread.sleep(500);
            		HashMap<String, UsbDevice> deviceList = Controller.mUsbManager.getDeviceList();
            		Log.i(TAG, Integer.toString(deviceList.size()) + " | UsbDevices found...");
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    while(deviceIterator.hasNext()==true){
                    	UsbDevice tryDevice = deviceIterator.next();
                    	if(!HostUsbManager.vendor_blacklist.contains(tryDevice.getVendorId())){
                    		Controller.mDevice = tryDevice;
                        	Controller.mUsbManager.requestPermission(Controller.mDevice, mPermissionIntent);
                        	pop_prompt.setText("Scanner Initializing...");
                        	Log.i(TAG,"Device Found!");
                        	break;	
                    	}else{
                    		Log.i(TAG, "Vendor |" + Integer.toString(tryDevice.getVendorId()) + " is blacklisted...");
                    	}
                    }

            	}catch(NullPointerException e2){
            		Log.i(TAG,"no permission...");
            	}catch(Exception e2){
            		Log.i(TAG,"Cancelled!");
            		break;
            	}
			}
			while (true){
				Log.i(TAG,"In loop...");
	        	if (isCancelled() == false){
	        		while(waiting_for_permission == true){
	    				try {
	    					Thread.sleep(500);
	    					Log.i(TAG, "waiting still for permission...");
	    				} catch (InterruptedException e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				}
	    				if(isCancelled()==true){
	    					return null;
	    				}
	    			}
	    			if (Controller.mDevice != null){
	    				try {
	    					Thread.sleep(250);
	    				} catch (InterruptedException e) {
	    					e.printStackTrace();
	    				}
	    				if (Controller.mScanner ==null){
	    					Log.i(TAG,"Scanner is null!");
	    					Controller.mScanner = new Scanner_Lumidigm_Mercury(Controller.mDevice, Controller.mUsbManager);
	    				}
	    				else{
	    					Log.i(TAG,"Scanner already exists!");
	    					Log.i(TAG,"Wiping Scanner");
	    					Controller.mScanner = new Scanner_Lumidigm_Mercury(Controller.mDevice, Controller.mUsbManager);
	    					//Controller.mScanner.init_scanner();
	    					//Controller.mScanner.reset_dicts();
	    				}
	    	    		while (Controller.mScanner.get_ready() == false){
	    	    			if(isCancelled()==true){
		    					return null;
		    				}
	    	    			if (Controller.mScanner.scan_cancelled == true){
	    	    				return null;
	    	    			}
	    	    			//if scan init failed
	                		try {
	    						Thread.sleep(250);
                                if(Controller.mScanner == null){
                                    break;
                                }
                            } catch (InterruptedException e) {
	    						e.printStackTrace();
	    					}
                            Log.i(TAG,"Scanner isn't ready...");
	                	}
	    	    		break;
	                }
	    			else{
	    				Log.i(TAG, "Scanner issue! scanner is null");
	    				break;
	    			}
	        	}else{
	        		//Cancelled
	        		Log.i(TAG,"Cancelled");
	        		break;
	        	}
			}
	    	return null;
		}

		@Override
		protected void onCancelled(Void res){
			try{
				//Controller.mHostUsbManager = null;
				//Controller.mDevice = null;
				Controller.mScanner = null;
				popUp.dismiss();
				//unregisterReceiver(mUsbReceiver);	
			}catch(Exception e){
				Log.i(TAG,"Couldn't kill scanner on cancel");
			}
		}
		@Override
		protected void onPostExecute(Void res) {
			if (Controller.mScanner != null){
				for (String k:template_cache.keySet()){
					Controller.mScanner.setBiometrics(k, template_cache.get(k));
				}
				for (String k:iso_template_cache.keySet()){
					Controller.mScanner.set_iso_template(k, iso_template_cache.get(k));
				}	
			}
			popUp.dismiss();	
		}
	}

	private enum operation_type{verify, identify, tag, skip};
	
	private void finish_activity(){
		this.finish();
	}
	
	public static Intent get_next_scanning_bundle(Intent i, int index){
		Bundle data = i.getExtras();
		List<String>fields = new ArrayList<String>(){{
			add("prompt");
			add("left_finger_assignment");
			add("right_finger_assignment");
			add("easy_skip");
		}};
		Bundle b = new Bundle();
		boolean all_null = true;
		if (index == 0){
			for(String field: fields){
				try{
					String a = data.getString(field);
					if (a!= null){
						b.putString(field, a);	
						Log.i(TAG, "Plain Field found | " + a + " | for" + field + " for # " + Integer.toString(index));
						all_null = false;
					}else{
						Log.i(TAG, "Plain Field empty | " + field + " for # " + Integer.toString(index));
					}
				}catch(Exception e){
					Log.i(TAG, "Error in bundle");
					return null;
				}
			}	
		}
		if (all_null){
			for(String field: fields){
				try{
					String a = data.getString(field+"_"+ Integer.toString(index));
					if (a!= null){
						b.putString(field, a);	
					}else{
						Log.i(TAG, "Numbered Field empty | " + field + " for # " + Integer.toString(index));
					}
					b.putString(field, a);
				}catch(Exception e){
					Log.i(TAG, "Error in bundle");
					return null;
				}
			}	
		}
		
		Intent out = new Intent();
		out.setAction("com.biometrac.core.SCAN");
		out.putExtras(b);
		return out;
		
	}
	
	
	public static boolean bundle_has_multiple_scans(Bundle data){
		if (data.containsKey("left_finger_assignment")){
			return false;
		}
		return true;
	}
	
	public static int get_total_scans_from_bundle(Bundle data){
		
		int out = 1;
		for (int x = 0; x < 10; x++){
			String left = data.getString("left_finger_assignment_" + Integer.toString(x));
			if (left == null){
				out = x;
				Log.i(TAG, "broke on " + Integer.toString(out));
				break;
			}
		}
		if (out < 1){
			return 1;
		}
			return out;
		
	}
	
	
	/*
	public static int get_current_scan_number_from_bundle(Bundle data){
		int x = 0;
		for (x = 0; x < 10; x++){
			String left = data.getString("left_finger_assignment_" + Integer.toString(x));
			if (left != null){
				break;
			}
		}
		return x;
	}
	*/
}


