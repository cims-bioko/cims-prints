package com.openandid.core;

import java.util.ArrayList;
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

    private static int startSequence =0;
    private static int createSequence =0;
    private static boolean caughtCancelled = false;
    private final String TAG = "PIPE";
	boolean finished = false;
	Intent output_intent;
	List<Intent> stack;
    int stackPosition;
	private static boolean gotResult = false;

	int REQUEST_CODE = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, String.format("onCreate | %s", createSequence));
        createSequence +=1;
		output_intent = new Intent();
		stack = new ArrayList<Intent>();
        stackPosition = 0;
		if (savedInstanceState != null){
            Log.i(TAG, "Found saved instance, reporting and restoring");
            restoreAssets(savedInstanceState);
            print_bundle(savedInstanceState);
            if(caughtCancelled){
                Log.e(TAG, "This was already canceled!");
				caughtCancelled = false;
                gotResult = false;
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            else{
                if(stack != null) {
                    Log.i(TAG, "Dispatching existing stack");
                    //dispatch_intent(stack);
                }else{
                    Log.e(TAG, "Using last known bundle as output for looped instance");
                    Intent i = new Intent();
                    i.putExtras(Controller.getLastStackOutput());
                    setResult(Activity.RESULT_OK, i);
                    Controller.resetStack();
                    gotResult = false;
                    finish();
                }

            }
        }else{
            Log.i(TAG, "savedInstanceState is null");
            Log.i(TAG, "Resetting Stack");
            Controller.resetStack();
        }
        boolean stackOK = checkStack(stack, stackPosition);
        if (!stackOK){
            stackOK = fixStack();
			if (!stackOK){
				Log.e(TAG, "Could not fix stack. Killing.");
				Controller.nullPipeStack();
                gotResult = false;
                finish();
				return;
			}else{
				Log.i(TAG, "StackFixed! Carry on...");
			}

        }
		if (Controller.isStackFinished()){
			Log.e(TAG, "Finished PIPE is attempting to restart. Killing");
            Log.e(TAG, "Using last known bundle as output for looped instance");
            Intent i = new Intent();
            i.putExtras(Controller.getLastStackOutput());
            setResult(Activity.RESULT_OK, i);
            Controller.resetStack();
            gotResult = false;
			finish();

		}else{
            Intent incoming = getIntent();
            Log.i(TAG, "Pipe NOT finished.");
            log_intent(incoming);
            output_intent.putExtras(merge_bundles(output_intent, incoming));
            //process_request(incoming);
        }

	}

	@Override
	protected void onResume() {
		Log.i(TAG, String.format("onResume| S:%s | C:%s", startSequence, createSequence));
		super.onResume();
        if (!gotResult && stackPosition == 0){
            Log.i(TAG, "New Stack");
            process_request(getIntent());
            return;
        }
		gotResult = false;
        Log.i(TAG, "Has Result");
        if(finished || caughtCancelled){
            caughtCancelled = false;
            finished = false;
            Log.d(TAG, "Finishing from resume");
            finish();
        }else{
            Log.d(TAG, "Dispatch from resume");
            dispatch_intent(stack);
        }
    }

    public void setStackPosition(int position){
        stackPosition = position;
        Controller.setPipePosition(position);
    }

    public boolean checkStack(List<Intent> stack, int stackPosition){
        int controlStackPosition = Controller.getPipeStackPosition();
        List<Intent> controlStack = Controller.getPipeStack();
        if (stackPosition != controlStackPosition){
            Log.e(TAG, "Stack is out of sync!");
            return false;
        }
        Log.i(TAG, "Stack is correct");
        return true;
    }

	public boolean fixStack(){
        try {
            stack = Controller.getPipeStack();
            stackPosition = Controller.getPipeStackPosition();
            return checkStack(stack, stackPosition);
        } catch (Exception e){
            Log.e(TAG, e.getMessage());
            return false;
        }

	}

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, String.format("onStart| %s", startSequence));
        startSequence +=1;
    }

    @Override
    protected void onPause() {
        Log.i(TAG, String.format("onPause| S:%s | C:%s", startSequence, createSequence));
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, String.format("onDestroy| S:%s | C:%s", startSequence, createSequence));
		super.onDestroy();
    }

    public Bundle saveAssets(Bundle bundle){
        Log.i(TAG, "Saving State");
        bundle.putBoolean("caughtCancelled", caughtCancelled);
        caughtCancelled = false;
        bundle.putSerializable("stack", (ArrayList<Intent>) stack);
        if(stack != null) {
            Log.i(TAG, String.format("Stack Size: %s", stack.size()));
        }
        bundle.putInt("stackPosition", stackPosition);
        Log.i(TAG, String.format("Current position %s", stackPosition));
        bundle.putBoolean("finished", finished);
        Log.i(TAG, String.format("Is finished: %s", finished));
        return bundle;
    }

    public void restoreAssets(Bundle bundle){
        Log.i(TAG, "Restoring State");
        caughtCancelled = bundle.getBoolean("caughtCancelled");
        Log.i(TAG, String.format("Caught Cancelled: %s", caughtCancelled));
        stack = (ArrayList<Intent>) bundle.getSerializable("stack");
        if(stack != null) {
            Log.i(TAG, String.format("Stack Size: %s", stack.size()));
        }else{
            Log.i(TAG, "stack is null");
        }
        stackPosition = bundle.getInt("stackPosition");
        Log.i(TAG, String.format("Current position %s", stackPosition));
        finished = bundle.getBoolean("finished");
        Log.i(TAG, String.format("Is finished: %s", finished));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        saveAssets(outState);

    }

    private void log_intent(Intent incoming) {
		Log.i(TAG, "INCOMING INTENT | CHECKIT");
		Log.i(TAG, "Action : " + incoming.getAction());
		
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
		dispatch_intent(stack);
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
		Log.d(TAG, "Stack size: " + Integer.toString(stack.size()));
		
	}
	
	private void append_scans(Intent incoming) {
		Log.i(TAG, "Append Scans");
		int iter = ScanningActivity.get_total_scans_from_bundle(incoming.getExtras());
		Log.i(TAG, "Total Scans: " + Integer.toString(iter));
		if (stack == null){stack = new ArrayList<Intent>();}
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
		Log.d(TAG, "Stack size: " + Integer.toString(stack.size()));
	}

    private void dispatch_intent(List<Intent> stack){
        Log.i(TAG, "Dispatching Stack");

        try {
            Intent currentIntent = stack.get(stackPosition);
            setStackPosition(stackPosition +1);
            Log.i(TAG, String.format("Current Stack Position %s of %s", stackPosition, stack.size()));
            dispatch_intent(currentIntent);
        }catch (IndexOutOfBoundsException e){
            try {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                Log.e(TAG, "Error getting current stack item");
                Log.e(TAG, String.format("Stack Size in controller: %s Stack Position: %s", Controller.getPipeStack().size(), stackPosition));
                throw new NullPointerException();
            }catch (Exception np){
                Log.e(TAG, "Killing pipe");
                gotResult = false;
                finish();
            }
        }catch (NullPointerException e){
            Log.e(TAG, "NullPointer getting current stack item");
            Log.e(TAG, "Killing pipe");
            gotResult = false;
            finish();
        }

    }

	private void dispatch_intent(Intent incoming){
		
		String action = incoming.getAction();
		Log.i(TAG, "Dispatching Activity | " + action);
		if(action.equals("com.biometrac.core.IDENTIFY")){
			output_intent.setClass(this, IdentifyActivity.class);
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
			incoming.setClass(this, ScanningActivity.class);
			Log.i(TAG, "Starting | SCAN");
			startActivityForResult(incoming, REQUEST_CODE);
		}
		else if (action.equals("com.biometrac.core.ENROLL")){
			output_intent.setClass(this, EnrollActivity.class);
			Log.i(TAG, "Starting | ENROLL");
			startActivityForResult(output_intent, REQUEST_CODE);
		}
		else if(action.equals("com.biometrac.core.VERIFY")){
			
		}else{
			if(stackPosition >= stack.size()){
                Bundle b = output_intent.getExtras();
                b.putString("pipe_finished", "true");
                output_intent.putExtras(b);
				setResult(Activity.RESULT_OK, output_intent);
			}
			else{
                Log.i(TAG, "More intents to dispatch!");
                dispatch_intent(stack);
            }

		}
		
	}
	
	private void print_bundle(Intent data){
		
		Log.i(TAG, "Print Bundle");
		Bundle b = data.getExtras();
		print_bundle(b);
	}

    private void print_bundle(Bundle b){
        String txt = "";

        try{
            Set<String> keys = b.keySet();
            for (String k:keys){
                txt += k+ " : " + b.get(k) + "\n";
            }
            Log.i(TAG, txt);
        }catch(Exception e){
            Log.i(TAG,"No result returned");
        }
    }

    private void cancelPipe(){
        Log.i(TAG, "Previous Activity was Canceled... Returning null");
        caughtCancelled = true;
        setResult(RESULT_CANCELED, new Intent());
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "Pipe Caught Output from child.");
        gotResult = true;
		if (data == null){
			Log.i(TAG, "Previous Activity DIED... Returning null");
            cancelPipe();
            super.onActivityResult(requestCode, resultCode, data);
            return;
		}else{
			Log.i(TAG, "Child data != null");
			print_bundle(data);
		}
		if(resultCode == RESULT_CANCELED){
            Log.i(TAG, "Previous Activity Canceled...");
			cancelPipe();
            super.onActivityResult(requestCode, resultCode, data);
			return;
		}
		output_intent.putExtras(merge_bundles(output_intent, data));
		
		if (stackPosition >= stack.size()) {
            Log.i(TAG, "No more intents, finished with PIPE");

            Controller.nullPipeStack();
            Controller.setStackFinished();
            setResult(resultCode, output_intent);
			finished = true;
            super.onActivityResult(requestCode, resultCode, data);

		}else{
            Log.i(TAG, String.format("Dispatching #%s of %s", stackPosition, stack.size()));
            dispatch_intent(stack);
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
        Controller.setLastStackOutput(c);
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
