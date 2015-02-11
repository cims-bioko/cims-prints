package com.biometrac.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class PipeActivity extends Activity {

	private final String TAG = "PIPE";
	boolean finished;
	Intent output_intent;
	Iterator<Intent> intents;
	List<Intent> stack;
	
	int REQUEST_CODE = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		output_intent = new Intent();
		stack = new ArrayList<Intent>();
		Intent incoming = getIntent();
		log_intent(incoming);
		output_intent.putExtras(merge_bundles(output_intent, incoming));
		process_request(incoming);
		super.onCreate(savedInstanceState);
	}
	
	private void log_intent(Intent incoming) {
		Log.i(TAG, "INCOMING INTENT | CHECKIT");
		Log.i(TAG, "Action : "  + incoming.getAction());
		
	}

	private void process_request(Intent incoming){
		try{
			REQUEST_CODE = incoming.getExtras().getInt("requestCode");	
		}catch (Exception e){
			Log.i(TAG, "Couldn't get requestcode from intent.");
		}
		String action = incoming.getAction();
		if(action.equals("com.biometrac.core.PIPE")){
			process_pipe(incoming);
		}else if(action.equals("com.biometrac.core.SCAN")){
			append_scans(incoming);
		}
		intents = stack.iterator();
		dispatch_intent(intents.next());
	}
	
	private void process_pipe(Intent incoming){
		Bundle b = incoming.getExtras();
		List<String> actions = new ArrayList<String>();
		for (int x = 0; x < 10; x++){
			String a = b.getString("action_"+Integer.toString(x));
			if (a!= null){
				if (a.equals("com.biometrac.core.SCAN")){
					append_scans(incoming);
				}else{
					Log.i(TAG, "Stacking | " + a);
					Intent i = new Intent();
					i.setAction(a);
					stack.add(i);
				}
			}
		}
		
	}
	
	private void append_scans(Intent incoming) {
		Log.i(TAG, "Append Scans");
		int iter = ScanningActivity.get_total_scans_from_bundle(incoming.getExtras());
		Log.i(TAG, "Total Scans: " + Integer.toString(iter));
		for (int x = 0; x< iter; x++){
			Intent b = ScanningActivity.get_next_scanning_bundle(incoming, x);
			if (b!=null){
				Log.i(TAG, "Stacking | SCAN -- details follow");
				print_bundle(b);
				stack.add(b);
			}else{
				Log.i(TAG, "Scan Bundle Null");
			}
		}
	}
	
	private void dispatch_intent(Intent incoming){
		
		String action = incoming.getAction();
		Log.i(TAG, "Dispatching Activity | " + action);
		if(action.equals("com.biometrac.core.IDENTIFY")){
			output_intent.setClass(this, com.biometrac.core.IdentifyActivity.class);
			Log.i(TAG, "Starting | IDENTIFY");
			if (Controller.commcare_handler != null){
				if (Controller.commcare_handler.isWorking()){
					//Wait for sync to finish w/ prompt
					
					//TODO fix  output_intent -> incoming
					Syncronizing sync = new Syncronizing(this, incoming);
					sync.execute();
					return;
				}
			}
			startActivityForResult(output_intent, REQUEST_CODE);
		}else if (action.equals("com.biometrac.core.SCAN")){
			incoming.setClass(this, com.biometrac.core.ScanningActivity.class);
			Log.i(TAG, "Starting | SCAN");
			startActivityForResult(incoming, REQUEST_CODE);
		}
		else if (action.equals("com.biometrac.core.ENROLL")){
			output_intent.setClass(this, com.biometrac.core.EnrollActivity.class);
			Log.i(TAG, "Starting | ENROLL");
			startActivityForResult(output_intent, REQUEST_CODE);
		}
		else if(action.equals("com.biometrac.core.VERIFY")){
			
		}else{
			if(!intents.hasNext()){
                Bundle b = output_intent.getExtras();
                b.putString("pipe_finished", "true");
				setResult(Activity.RESULT_OK, output_intent);
			}
			dispatch_intent(intents.next());
		}
		
	}
	
	private void print_bundle(Intent data){
		
		Log.i(TAG, "Print Bundle");
		Bundle b = data.getExtras();
		String txt = "";
	
		try{
			Set<String> keys = b.keySet();
			for (String k:keys){
				txt += k+ " : " + b.get(k) + "\n";
			}
			Log.i(TAG, txt);
			// TODO Auto-generated method stub
			}catch(Exception e){
				Log.i(TAG,"No result returned");
			}	
	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "Pipe Caught Output from child.");
		//print_bundle(data);
		if (data == null){
			Log.i(TAG,"Previous Activity DIED... Returning null");
			setResult(RESULT_CANCELED, new Intent());
			super.onActivityResult(requestCode, resultCode, data);
			this.finish();
			return;
		}else{
			Log.i(TAG, "Child data != null");
			print_bundle(data);
		}
		if(resultCode == Activity.RESULT_CANCELED){
			Log.i(TAG,"Previous Activity was Canceled... Returning null");
			setResult(RESULT_CANCELED, new Intent());
			super.onActivityResult(requestCode, resultCode, data);
			this.finish();
			return;
		}
		output_intent.putExtras(merge_bundles(output_intent, data));
		
		if (!intents.hasNext()){
			Log.i(TAG, "No more intents, finished with PIPE");
            MainActivity.pipeFinished = true;
			setResult(resultCode, output_intent);
			super.onActivityResult(requestCode, resultCode, data);
			this.finish();
		}else{
			dispatch_intent(intents.next());
			super.onActivityResult(requestCode, resultCode, data);
		}
		
	}
	
	private Bundle merge_bundles(Intent a, Intent b){
		Bundle c = new Bundle();
		Bundle ba = a.getExtras();
		if (ba!= null){
			Log.i(TAG, "Merge A:");
			print_bundle(a);
			c.putAll(ba);
		}
		Bundle bb = b.getExtras();
		if (bb!= null){
			Log.i(TAG, "Merge B:");
			print_bundle(b);
			c.putAll(bb);
		}
		return c;
	}

	class Syncronizing extends AsyncTask<Void, Void, Void> {
		private Context oldContext;
		private Intent incoming;
		
		public Syncronizing(Context context, Intent incoming){
			super();
			oldContext = context;
			this.incoming = incoming;
		}
		protected void onPreExecute() {
			Toast.makeText(oldContext, "CommCare Sync in Progress...\nPlease Wait", Toast.LENGTH_LONG).show();
			setContentView(R.layout.activity_main_wait);
			TextView t = (TextView) findViewById(R.id.main_blank_txt);
			t.setText("CommCare Sync in Progress...");
		}
		@Override
		protected Void doInBackground(Void... params) {
	    	while(Controller.commcare_handler.isWorking()){
	    		try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    	Log.i(TAG, "Sync Done Found");
	    	return null;
		}
		protected void onPostExecute(Void res) {
			Toast.makeText(oldContext, "Sync Finished...", Toast.LENGTH_LONG).show();
			dispatch_intent(incoming);
		}

	}
	
}
