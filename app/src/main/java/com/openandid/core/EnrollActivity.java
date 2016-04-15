package com.openandid.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.openandid.core.Controller;

public class EnrollActivity extends Activity{

	final String TAG = "EnrollActivity";
	boolean enrolled = false;
	String previous_id = null;
	String new_id = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
		setContentView(R.layout.spinner_view);
		Bundle extras = getIntent().getExtras();
		EnrollBG bg = new EnrollBG(extras);
		bg.execute();
		//show spinner
		//run asynch with extras
		//finish with code
		
		
	}
	
	private void finish_ok(){
		//send all clear
		Intent i = new Intent();
		i.putExtra("new_id", new_id);
		setResult(RESULT_OK, i);
		this.finish();
	}	
	private void finish_error(){
		//send error code and previous id
		Intent i = new Intent();
		i.putExtra("previous_id", previous_id);
		setResult(RESULT_CANCELED, i);
		this.finish();
	}
	
	private class EnrollBG extends AsyncTask<Void, Void, Void> {
		
		final List<String> fingers = new ArrayList<String>(){{
			add("left_thumb");
			add("right_thumb");
			add("left_index");
			add("right_index");
		}};

		Bundle extras;
	private EnrollBG(Bundle mExtras) {
			super();
			extras = mExtras;
			
		}
		@Override
		protected Void doInBackground(Void... params) {
		    
			String uuid = extras.getString("uuid");
			Map<String,String> templates = new HashMap<String,String>();
			for (String f : fingers){
				String temp = extras.getString(f);
				if (temp != null){
					templates.put(f, temp);
				}
			}
			try{
				Controller.mEngine.add_candidate_to_cache(uuid, templates);
				enrolled = true;
			}catch (Exception e){
				enrolled = false;
			}
			
			return null;
		}

		protected void onPostExecute(Void res) {
			if (enrolled == true){
				finish_ok();
			}else{
				finish_error();
			}
	    }
	}

}
