package com.biometrac.core;

import logic.NativeSetup;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Override;

public class MainActivity extends Activity {

    private static int startSequence =0;
    private static int createSequence =0;
    boolean needs_return = true;
    public static boolean pipeFinished = false;
    Button fire_btn;
    public final String TAG = "CoreLauncher";
    int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, String.format("onCreate | %s", createSequence));
        createSequence +=1;
        if (is_false_start()){
            return;
        }
        try{
            Bundle b = getIntent().getExtras();
            String pipe_status = b.getString("pipe_finished");
            if(pipe_status.equals("true")){pipeFinished = true;}
            Log.i(TAG, "Pipe finished status in intent");
        }catch(NullPointerException e){
            Log.i(TAG, "No pipe status in intent");
            pipeFinished = false;
        }

        if(pipeFinished){
            Log.i(TAG, "Pipe sent finished signal, aborting onCreate dispatches.");
            pipeFinished = false;
            return;
        }else{Log.i(TAG, "Pipe didn't send finished signal, continuing.");}

        Intent incoming = getIntent();
        try{
            REQUEST_CODE = incoming.getExtras().getInt("requestCode");
        }catch (Exception e){
            Log.i(TAG, "Couldn't get requestcode from intent.");
        }

        try{
            Bundle b = incoming.getExtras();
            Log.i(TAG,"Input from CommCare");
            for (String k: b.keySet()){
                try{
                    Log.i(TAG, k+": " + b.getString(k));
                }catch (Exception e2){
                    Log.i(TAG, "Key: " + k + " is not readable as a string.");
                    Log.i(TAG,e2.toString());
                }
            }
        }catch (Exception e){
            Log.i(TAG,"Error reading incoming bundle.");
            Log.i(TAG, e.toString());
        }
        //TODO KILL
        /*
        Log.i(TAG, "Starting Native Check");

        boolean isNativeSetup = NativeSetup.checkNativeSystem(this);
        if (!isNativeSetup){
            new NativeSetupBackground(this, incoming).execute();
        }else{
            dispatch_intent(incoming);
        }
        */
        dispatch_intent(incoming);
    }

    @Override
    protected void onStart() {
        Log.i(TAG, String.format("onStart| %s", startSequence));
        startSequence +=1;
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Resume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Pause");
        super.onPause();
    }

    private boolean is_false_start() {
        if (Controller.preference_manager.is_false_start()){
            Log.i(TAG, "False Start Caught");
            finish_cancel();
            return true;
        }
        return false;
    }

    private void finish_cancel() {
        Log.i(TAG, "Finishing as Canceled.");
        Intent i = new Intent();
        Bundle b = new Bundle();
        i.putExtra("odk_intent_bundle",b);
        setResult(RESULT_CANCELED, i);
        finish();
    }


    private void dispatch_intent(Intent incoming) {
        String action = incoming.getAction();
        Log.i(TAG,"Started via... " + action);
        if (action.equals("com.biometrac.core.SCAN")){
            if(ScanningActivity.get_total_scans_from_bundle(incoming.getExtras())>1){
                Log.i(TAG, "Launching PIPE");
                incoming.setClass(this, PipeActivity.class);
                startActivityForResult(incoming, REQUEST_CODE);
                return;
            }else{
                Log.i(TAG, "Launching SCAN");
                incoming.setClass(this, ScanningActivity.class);
                startActivityForResult(incoming, REQUEST_CODE);
                return;
            }

        }
        else if (action.equals("com.biometrac.core.ENROLL")){
            incoming.setClass(this, EnrollActivity.class);
            startActivityForResult(incoming, REQUEST_CODE);
            return;
        }
        else if(action.equals("com.biometrac.core.VERIFY")){
            return;

        }
        else if(action.equals("com.biometrac.core.IDENTIFY")){
            if (Controller.commcare_handler != null){
                if (Controller.commcare_handler.isWorking()){
                    //Wait for sync to finish w/ prompt
                    Syncronizing sync = new Syncronizing(this, incoming);
                    sync.execute();
                    return;
                }
            }
            incoming.setClass(this, IdentifyActivity.class);
            startActivityForResult(incoming, REQUEST_CODE);
        }
        else if(action.equals("com.biometrac.core.LOAD")){
            return;

        }
        else if(action.equals("com.biometrac.core.PIPE")){
            incoming.setClass(this, PipeActivity.class);
            startActivityForResult(incoming, REQUEST_CODE);
            return;
        }
        else{
            needs_return = false;
            setContentView(R.layout.activity_main);
            //TODO This needs to be its own little screen
            fire_btn = (Button) findViewById(R.id.main_fire_btn);
            enable_fire();
            Button sync_button = (Button) findViewById(R.id.main_sync_btn);
            if(!Controller.preference_manager.has_preferences()){
                sync_button.setVisibility(View.GONE);
            }else{
                sync_button.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if(!Controller.commcare_handler.isWorking()){
                            Controller.sync_commcare_default();
                            Toast.makeText(getBaseContext(), "Sync Started.", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getBaseContext(), "Sync is Already Running.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            Button sync_set = (Button) findViewById(R.id.main_sync_settings_btn);
            sync_set.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(!Controller.commcare_handler.isWorking()){
                        Intent i = new Intent(getBaseContext(), CCSyncActivity.class);
                        startActivity(i);
                        finish_self();
                    }else{
                        Toast.makeText(getBaseContext(), "Please wait for Sync to Complete...", Toast.LENGTH_LONG).show();
                    }

                }
            });
            Button advanced = (Button) findViewById(R.id.advanced_settings_btn);
            advanced.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getBaseContext(), AdvancedPreferences.class);
                    startActivity(i);

                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Dispatcher Has Activity Result");
        try{
            Bundle b = data.getExtras();
            //IF CCODK
            data.putExtra("odk_intent_bundle",b);

            Log.i(TAG,"Output from BiometracCore");
            for (String k: b.keySet()){
                Log.i(TAG, k+": " + b.getString(k));
            }
        }catch(Exception e){
            Log.i(TAG, "No output from activity");
            Log.i(TAG,e.toString());
        }
        //super.onActivityResult(requestCode, resultCode, data);
        if (needs_return == true){
            setResult(resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
        this.finish();
    }

    private void finish_self(){
        finish();
    }

    private void fire_intent(){
        Intent i = new Intent();
        //Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.biometraclogo);
        //i.putExtra("image", bmp);
        i.setAction("com.biometrac.core.SCAN");
        i.putExtra("prompt_0", "This is a\nTest CC Prompt!");
        i.putExtra("easy_skip_0", "true");
        i.putExtra("prompt_1", "This is a\nTest Prompt\n2!");
        i.putExtra("easy_skip_1", "true");
        i.putExtra("left_finger_assignment_0", "left_index");
        i.putExtra("right_finger_assignment_0", "right_middle");
        i.putExtra("left_finger_assignment_1", "right_thumb");
        i.putExtra("right_finger_assignment_1", "left_middle");
        startActivityForResult(i, 101);
    }

    public void enable_fire(){
        fire_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fire_intent();
            }
        });
    }

    //TODO KILL
    /*
    //Sets up the NDK file system and utilities on the apps first run
    class NativeSetupBackground extends AsyncTask<Void, Void, Void> {
        private Context oldContext;
        private Intent incoming;

        public NativeSetupBackground(Context context, Intent incoming){
            super();
            oldContext = context;
            this.incoming = incoming;
        }
        protected void onPreExecute() {
            Toast.makeText(oldContext, "Updating System... Please Wait", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_main_wait);
            TextView t = (TextView) findViewById(R.id.main_blank_txt);
            t.setText("System Updating...");
        }
        @Override
        protected Void doInBackground(Void... params) {
            NativeSetup.setupNativeSystem(oldContext);
            return null;
        }
        protected void onPostExecute(Void res) {
            Toast.makeText(oldContext, "Update Finished...", Toast.LENGTH_LONG).show();
            dispatch_intent(incoming);
        }

    }
    */
    //Sets up the NDK file system and utilities on the apps first run
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


