package com.openandid.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import data.CommCareContentHandler;

import static com.openandid.core.Constants.ENROLL_ACTION;
import static com.openandid.core.Constants.IDENTIFY_ACTION;
import static com.openandid.core.Constants.PIPE_ACTION;
import static com.openandid.core.Constants.SCAN_ACTION;

public class PipeActivity extends Activity {

    private static final String TAG = "PIPE";


    private static int requestCode = 1;

    private static int startSequence = 0;
    private static int createSequence = 0;
    private static boolean caughtCancelled = false;
    private static boolean gotResult = false;

    private boolean finished = false;
    private Intent outputIntent;
    private List<Intent> stack;
    private int stackPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, String.format("onCreate | %s", createSequence));
        createSequence += 1;
        outputIntent = new Intent();
        stack = new ArrayList<>();
        stackPosition = 0;
        if (savedInstanceState != null) {
            Log.i(TAG, "Found saved instance, reporting and restoring");
            restoreAssets(savedInstanceState);
            if (caughtCancelled) {
                Log.e(TAG, "This was already canceled!");
                caughtCancelled = false;
                gotResult = false;
                setResult(RESULT_CANCELED);
                finish();
                return;
            } else {
                if (stack != null) {
                    Log.i(TAG, "Dispatching existing stack");
                } else {
                    Log.e(TAG, "Using last known bundle as output for looped instance");
                    Intent i = new Intent();
                    i.putExtras(Controller.getLastStackOutput());
                    setResult(Activity.RESULT_OK, i);
                    Controller.resetStack();
                    gotResult = false;
                    finish();
                }

            }
        } else {
            Log.i(TAG, "savedInstanceState is null");
            Log.i(TAG, "Resetting Stack");
            Controller.resetStack();
        }
        boolean stackOK = checkStack(stack, stackPosition);
        if (!stackOK) {
            stackOK = fixStack();
            if (!stackOK) {
                Log.e(TAG, "Could not fix stack. Killing.");
                Controller.nullPipeStack();
                gotResult = false;
                finish();
                return;
            } else {
                Log.i(TAG, "StackFixed! Carry on...");
            }

        }
        if (Controller.isStackFinished()) {
            Log.e(TAG, "Finished PIPE is attempting to restart. Killing");
            Log.e(TAG, "Using last known bundle as output for looped instance");
            Intent i = new Intent();
            i.putExtras(Controller.getLastStackOutput());
            setResult(Activity.RESULT_OK, i);
            Controller.resetStack();
            gotResult = false;
            finish();

        } else {
            Intent incoming = getIntent();
            Log.i(TAG, "Pipe NOT finished.");
            outputIntent.putExtras(mergeBundles(outputIntent, incoming));
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, String.format("onResume| S:%s | C:%s", startSequence, createSequence));
        super.onResume();
        if (!gotResult && stackPosition == 0) {
            Log.i(TAG, "New Stack");
            processRequest(getIntent());
            return;
        }
        gotResult = false;
        Log.i(TAG, "Has Result");
        if (finished || caughtCancelled) {
            caughtCancelled = false;
            finished = false;
            Log.d(TAG, "Finishing from resume");
            finish();
        } else {
            Log.d(TAG, "Dispatch from resume");
            dispatchIntent(stack);
        }
    }

    public void setStackPosition(int position) {
        stackPosition = position;
        Controller.setPipePosition(position);
    }

    public boolean checkStack(List<Intent> stack, int stackPosition) {
        int controlStackPosition = Controller.getPipeStackPosition();
        if (stackPosition != controlStackPosition) {
            Log.e(TAG, "Stack is out of sync!");
            return false;
        }
        Log.i(TAG, "Stack is correct");
        return true;
    }

    public boolean fixStack() {
        try {
            stack = Controller.getPipeStack();
            stackPosition = Controller.getPipeStackPosition();
            return checkStack(stack, stackPosition);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, String.format("onStart| %s", startSequence));
        startSequence += 1;
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

    public Bundle saveAssets(Bundle bundle) {
        Log.i(TAG, "Saving State");
        bundle.putBoolean("caughtCancelled", caughtCancelled);
        caughtCancelled = false;
        bundle.putSerializable("stack", (ArrayList<Intent>) stack);
        if (stack != null) {
            Log.i(TAG, String.format("Stack Size: %s", stack.size()));
        }
        bundle.putInt("stackPosition", stackPosition);
        Log.i(TAG, String.format("Current position %s", stackPosition));
        bundle.putBoolean("finished", finished);
        Log.i(TAG, String.format("Is finished: %s", finished));
        return bundle;
    }

    @SuppressWarnings("unchecked")
    public void restoreAssets(Bundle bundle) {
        Log.i(TAG, "Restoring State");
        caughtCancelled = bundle.getBoolean("caughtCancelled");
        Log.i(TAG, String.format("Caught Cancelled: %s", caughtCancelled));
        stack = (ArrayList<Intent>) bundle.getSerializable("stack");
        if (stack != null) {
            Log.i(TAG, String.format("Stack Size: %s", stack.size()));
        } else {
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

    private void processRequest(Intent incoming) {
        try {
            requestCode = incoming.getExtras().getInt("requestCode");
        } catch (Exception e) {
            Log.i(TAG, "Couldn't get requestcode from intent.");
        }
        String action = incoming.getAction();
        if (action.equals(PIPE_ACTION)) {
            processPipe(incoming);
        } else if (action.equals(SCAN_ACTION)) {
            appendScans(incoming);
        }
        dispatchIntent(stack);
    }

    private void processPipe(Intent incoming) {
        Bundle b = incoming.getExtras();
        for (int x = 0; x < 10; x++) {
            String a = b.getString("action_" + Integer.toString(x));
            if (a != null) {
                if (a.equals(SCAN_ACTION)) {
                    appendScans(incoming);
                } else {
                    Log.i(TAG, "Stacking | " + a);
                    Intent i = new Intent();
                    i.setAction(a);
                    stack.add(i);
                }
            }
        }
        Log.d(TAG, "Stack size: " + Integer.toString(stack.size()));
    }

    private void appendScans(Intent incoming) {
        Log.i(TAG, "Append Scans");
        int iter = ScanningActivity.getScanCount(incoming.getExtras());
        Log.i(TAG, "Total Scans: " + Integer.toString(iter));
        if (stack == null) {
            stack = new ArrayList<>();
        }
        for (int x = 0; x < iter; x++) {
            Intent b = ScanningActivity.getNextScan(incoming, x);
            if (b != null) {
                stack.add(b);
            } else {
                Log.i(TAG, "Scan Bundle Null");
            }
        }
        Log.d(TAG, "Stack size: " + Integer.toString(stack.size()));
    }

    private void dispatchIntent(List<Intent> stack) {
        try {
            Intent currentIntent = stack.get(stackPosition);
            setStackPosition(stackPosition + 1);
            Log.i(TAG, String.format("Current Stack Position %s of %s", stackPosition, stack.size()));
            dispatchIntent(currentIntent);
        } catch (IndexOutOfBoundsException e) {
            try {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                Log.e(TAG, "Error getting current stack item");
                Log.e(TAG, String.format("Stack Size in controller: %s Stack Position: %s", Controller.getPipeStack().size(), stackPosition));
                throw new NullPointerException();
            } catch (Exception np) {
                Log.e(TAG, "Killing pipe");
                gotResult = false;
                finish();
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointer getting current stack item");
            Log.e(TAG, "Killing pipe");
            gotResult = false;
            finish();
        }
    }

    private void dispatchIntent(Intent incoming) {

        String action = incoming.getAction();

        switch (action) {
            case IDENTIFY_ACTION:
                outputIntent.setClass(this, IdentifyActivity.class);
                Log.i(TAG, "Starting | IDENTIFY");
                if (Controller.commCareHandler != null) {
                    if (CommCareContentHandler.isInSync()) {
                        SyncTask sync = new SyncTask(this, incoming);
                        sync.execute();
                        return;
                    }
                }
                startActivityForResult(outputIntent, requestCode);
                break;
            case SCAN_ACTION:
                incoming.setClass(this, ScanningActivity.class);
                Log.i(TAG, "Starting | SCAN");
                startActivityForResult(incoming, requestCode);
                break;
            case ENROLL_ACTION:
                outputIntent.setClass(this, EnrollActivity.class);
                Log.i(TAG, "Starting | ENROLL");
                startActivityForResult(outputIntent, requestCode);
                break;
            default:
                if (stackPosition >= stack.size()) {
                    Bundle b = outputIntent.getExtras();
                    b.putString("pipe_finished", "true");
                    outputIntent.putExtras(b);
                    setResult(Activity.RESULT_OK, outputIntent);
                } else {
                    Log.i(TAG, "More intents to dispatch!");
                    dispatchIntent(stack);
                }
                break;
        }
    }

    private void cancelPipe() {
        Log.i(TAG, "Previous Activity was Canceled... Returning null");
        caughtCancelled = true;
        setResult(RESULT_CANCELED, new Intent());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Pipe Caught Output from child.");
        gotResult = true;
        if (data == null) {
            Log.i(TAG, "Previous Activity DIED... Returning null");
            cancelPipe();
            super.onActivityResult(requestCode, resultCode, null);
            return;
        }
        if (resultCode == RESULT_CANCELED) {
            Log.i(TAG, "Previous Activity Canceled...");
            cancelPipe();
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        outputIntent.putExtras(mergeBundles(outputIntent, data));

        if (stackPosition >= stack.size()) {
            Log.i(TAG, "No more intents, finished with PIPE");

            Controller.nullPipeStack();
            Controller.setStackFinished();
            setResult(resultCode, outputIntent);
            finished = true;
            super.onActivityResult(requestCode, resultCode, data);

        } else {
            Log.i(TAG, String.format("Dispatching #%s of %s", stackPosition, stack.size()));
            dispatchIntent(stack);
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private Bundle mergeBundles(Intent a, Intent b) {
        Bundle c = new Bundle();
        Bundle ba = a.getExtras();
        if (ba != null) {
            c.putAll(ba);
        }
        Bundle bb = b.getExtras();
        if (bb != null) {
            c.putAll(bb);
        }
        Controller.setLastStackOutput(c);
        return c;
    }

    private class SyncTask extends AsyncTask<Void, Void, Void> {
        private Context oldContext;
        private Intent incoming;

        SyncTask(Context context, Intent incoming) {
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
            while (CommCareContentHandler.isInSync()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "interrupted during sleep");
                }
            }
            Log.i(TAG, "Sync Done Found");
            return null;
        }

        protected void onPostExecute(Void res) {
            Toast.makeText(oldContext, "Sync Finished...", Toast.LENGTH_LONG).show();
            dispatchIntent(incoming);
        }
    }
}
