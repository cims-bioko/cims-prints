package logic;

import java.lang.reflect.Field;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class FingerScanInterface extends AsyncTask<Void, Void, Void> {
    
	/*
	 * THIS IS NON-OP IT EXISTS AS AN INNER PRIVATE CLASS OF ScanningActivity
	 * 
	 * 
	 * 
	 */
	ImageView view;
	Scanner mScanner;
	String finger;
	boolean success;

	private final String TAG = "FingerInterface";
	
	public FingerScanInterface(String name, Scanner scanner, ImageView spot) {
		super();
		view = spot;
		finger = name;
        mScanner = scanner;
        view.setOnClickListener(null);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	
		success = mScanner.run_scan(finger);
		return null;
		
        
	}
	protected void onPostExecute(Void res) {
		if (success==true){
			Log.i("PostExec", "Started");
			Bitmap result = mScanner.get_image(finger);
	    	Log.i(TAG, "Result H: " + Integer.toString(result.getHeight()));
	    	Log.i(TAG, "Result W: " + Integer.toString(result.getWidth()));
	    	
	    	
	    	int maxWidth = -1;
	    	int maxHeight = -1;

	    	try {
	    	     Field maxWidthField = ImageView.class.getDeclaredField("mMaxWidth");
	    	     Field maxHeightField = ImageView.class.getDeclaredField("mMaxHeight");
	    	     maxWidthField.setAccessible(true);
	    	     maxHeightField.setAccessible(true);

	    	     maxWidth = (Integer) maxWidthField.get(view);
	    	     maxHeight = (Integer) maxHeightField.get(view);
	    	} catch (SecurityException e) {
	    	    e.printStackTrace();
	    	} catch (NoSuchFieldException e) {
	    	    e.printStackTrace();
	    	} catch (IllegalArgumentException e) {
	    	    e.printStackTrace();
	    	} catch (IllegalAccessException e) {
	    	    e.printStackTrace();
	    	}
	    	
	    	
	    	int new_height = (int) Math.round(maxHeight*.92);
	    	int new_width = (int) Math.round(maxWidth*.92);
	    	Log.i(TAG, "new height: " + Integer.toString(new_height));
	    	Log.i(TAG, "new width: " +Integer.toString(new_width));
	    	
	    	Bitmap scaled = Bitmap.createScaledBitmap(result, new_width, new_height, false);
	    	Log.i(TAG, "Scaled BMP width: " + Integer.toString(scaled.getWidth()));
	        view.setScaleType(ScaleType.CENTER);
	    	view.setImageBitmap(scaled);
		}else{
	        view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new FingerScanInterface(finger, mScanner, view).execute();
				}
			});			

		}

        
    }
	
}

