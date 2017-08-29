package com.openandid.core;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import logic.Finger;
import logic.HostUsbManager;
import logic.LumidigmMercuryScanner;
import logic.Scanner;

import static com.openandid.core.Constants.EASY_SKIP_KEY;
import static com.openandid.core.Constants.INTERNAL_SCAN_ACTION;
import static com.openandid.core.Constants.LEFT_FINGER_ASSIGNMENT_KEY;
import static com.openandid.core.Constants.PROMPT_KEY;
import static com.openandid.core.Constants.RIGHT_FINGER_ASSIGNMENT_KEY;
import static com.openandid.core.Constants.SCAN_FIELDS;

public class ScanningActivity extends Activity {

    private static final String TAG = "ScanningActivity";

    private static final String ACTION_USB_PERMISSION = "com.openandid.screentest.ScanningActivity.USB_PERMISSION";

    /*
        I'm embarrassed by this mess. Please, refactor...
     */

    public static boolean waitingForPermission = true;

    private static UsbDevice freeScanner = null;
    private static HashMap<String, Bitmap> scanImages;
    private static HashMap<String, Boolean> scannedFingers;
    private static HashMap<String, String> templateCache, isoTemplateCache;

    private PendingIntent mPermissionIntent;

    Finger leftFinger, rightFinger;
    ImageButton leftButton, rightButton, proceedButton, skipButton;
    Button cancelPopupButton;

    boolean easySkip = false, killSwitch = false;
    ImageView restartScanner;
    View popupView;
    PopupWindow popupWindow;
    TextView popupPrompt;

    Map<String, String> opts = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        scanImages = new HashMap<>();
        scannedFingers = new HashMap<>();
        templateCache = new HashMap<>();
        isoTemplateCache = new HashMap<>();

        if (savedInstanceState != null) {
            loadAssets(savedInstanceState);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String rotation = prefs.getString("bmt.rotation", "Horizontal");
        if (rotation.equals("Horizontal")) {
            if (getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Log.i(TAG, "Rotating to Landscape");
            }
            Log.i(TAG, "Setting Layout Landscape");
            setCorrectContentView(Configuration.ORIENTATION_LANDSCAPE);

        } else {
            if (getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                Log.i(TAG, "Rotating to Portrait");
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            Log.i(TAG, "Setting Layout Portrait");
            setCorrectContentView(Configuration.ORIENTATION_PORTRAIT);
        }

        loadOptions(getIntent().getExtras());
        //If we have options from the intent Bundle, parse and enact them
        if (opts != null) {
            applyOptions();
        } else {
            loadDefaultOptions();
        }

        //TODO Enable or edit exit button
        setupUI();
        if (savedInstanceState != null) {
            Log.d(TAG, "Redrawing assets");
            redrawAssets();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveAssets(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void dismissProgressDialog() {
        if (popupWindow != null && popupWindow.isShowing()) {
            try {
                popupWindow.dismiss();
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "PopUp not attached to window: " + e.getMessage());

            } catch (Exception e2) {
                Log.e(TAG, "Unspecified Error with dismiss popup: " + e2.getMessage());
                e2.printStackTrace();
            }
        }
    }

    private void setupUI() {

        popupView = getLayoutInflater().inflate(R.layout.pop_up_wait, null);
        popupWindow = new PopupWindow(popupView);
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupPrompt = (TextView) popupView.findViewById(R.id.pop_up_wait_title);

        cancelPopupButton = (Button) popupView.findViewById(R.id.pop_up_wait_cancel_btn);

        TextView leftButtonText = (TextView) findViewById(R.id.scanner_txt_finger_1_title);
        leftButtonText.setText(leftFinger.getLabel());

        leftButton = (ImageButton) findViewById(R.id.scanner_btn_finger_1);
        leftButton.setImageDrawable(getResources().getDrawable(leftFinger.getDrawable()));
        leftButton.setOnClickListener(getScanClickListener(leftFinger, leftButton));
        leftButton.setOnLongClickListener(getScanLongClickListener(leftFinger, leftButton));

        TextView rightButtonText = (TextView) findViewById(R.id.scanner_txt_finger_2_title);
        rightButtonText.setText(rightFinger.getLabel());

        rightButton = (ImageButton) findViewById(R.id.scanner_btn_finger_2);
        rightButton.setImageDrawable(getResources().getDrawable(rightFinger.getDrawable()));
        rightButton.setOnClickListener(getScanClickListener(rightFinger, rightButton));
        rightButton.setOnLongClickListener(getScanLongClickListener(rightFinger, rightButton));

        proceedButton = (ImageButton) findViewById(R.id.scan_btn_proceed);
        proceedButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Controller.mScanner == null) {
                    restartScanner(view);
                    Toast.makeText(ScanningActivity.this, getResources().getString(R.string.scanner_not_connected), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Controller.mScanner.isReady()) {
                    finalizeScans();
                } else {
                    Toast.makeText(ScanningActivity.this, getResources().getString(R.string.please_wait), Toast.LENGTH_SHORT).show();
                }
            }
        });

        skipButton = (ImageButton) findViewById(R.id.scan_btn_skip);
        if (!easySkip) {
            skipButton.setVisibility(View.GONE);
        } else {
            skipButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO add confirmation?
                    finishCancel();
                }
            });
        }

        restartScanner = (ImageView) findViewById(R.id.headbar_scanner_btn);
        restartScanner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                unplugScanner(v);
            }
        });

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    private void setCorrectContentView(int configuration) {
        switch (configuration) {
            case Configuration.ORIENTATION_LANDSCAPE:
                setContentView(R.layout.scanner_flex_layout_landscape);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                setContentView(R.layout.scanner_flex_layout);
                break;
        }
    }

    private OnClickListener getScanClickListener(final Finger finger, final ImageButton btn) {
        return new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClick(finger, btn, false, view);
            }
        };
    }

    private Button.OnLongClickListener getScanLongClickListener(final Finger finger, final ImageButton btn) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                handleClick(finger, btn, true, view);
                return true;
            }
        };
    }

    private void handleClick(final Finger finger, final ImageButton btn, final boolean instant, View view) {
        if (Controller.mScanner == null) {
            restartScanner(view);
            Toast.makeText(this, getResources().getString(R.string.scanner_not_connected), Toast.LENGTH_SHORT).show();
            return;
        } else {
            HashMap<String, UsbDevice> deviceList = Controller.mUsbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            if (!deviceIterator.hasNext()) {
                Log.i(TAG, "Device Unplugged");
                restartScanner(view);
                Toast.makeText(this, getResources().getString(R.string.scanner_not_connected), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (Controller.mScanner.isReady()) {
            popupPrompt.setText(getResources().getString(R.string.scan_prompt) + " " + finger.getLabel());
            popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.showAtLocation(view, Gravity.CENTER_VERTICAL, 0, 0);
            final FingerScanTask f = new FingerScanTask(finger.getKey(), Controller.mScanner, btn, view, instant);
            cancelPopupButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelScan(f);
                }
            });
            cancelPopupButton.setVisibility(View.VISIBLE);
            popupWindow.update();
            f.execute();
        }
    }

    public Bundle saveAssets(Bundle bundle) {
        bundle.putSerializable("scanImages", scanImages);
        bundle.putSerializable("scannedFingers", scannedFingers);
        bundle.putSerializable("template_cache", templateCache);
        bundle.putSerializable("iso_template_cache", isoTemplateCache);
        return bundle;
    }


    @SuppressWarnings("unchecked")
    public void loadAssets(Bundle bundle) {
        if (bundle != null) {
            Log.e(TAG, "Loading Assets");
            scanImages = (HashMap<String, Bitmap>) bundle.getSerializable("scanImages");
            scannedFingers = (HashMap<String, Boolean>) bundle.getSerializable("scannedFingers");
            templateCache = (HashMap<String, String>) bundle.getSerializable("template_cache");
            isoTemplateCache = (HashMap<String, String>) bundle.getSerializable("iso_template_cache");
        }
    }

    public void redrawAssets() {
        if (scanImages == null) {
            return;
        }
        for (String key : scanImages.keySet()) {
            Log.d(TAG, "found in scan Images: " + key);
        }
        Bitmap leftScanImage = scanImages.get(leftFinger.getKey());
        if (leftScanImage != null) {
            leftButton.setImageBitmap(leftScanImage);
            Log.i(TAG, "Drew left Image from previous");
        } else {
            Log.i(TAG, "Left image is null");
        }
        Bitmap rightScanImage = scanImages.get(rightFinger.getKey());
        if (rightScanImage != null) {
            rightButton.setImageBitmap(rightScanImage);
            Log.i(TAG, "Drew right Image from previous");
        } else {
            Log.i(TAG, "Right image is null");
        }
    }

    @Override
    public void onBackPressed() {
        finishCancel();
    }


    public boolean checkKillSwitch() {
        if (!killSwitch) {
            return false;
        }
        killSwitch = false;
        return true;
    }

    public static void setFreeDevice(UsbDevice device) {
        freeScanner = device;
    }

    public static UsbDevice getFreeDevice() {
        return freeScanner;
    }

    private void loadOptions(Bundle extras) {
        try {
            if (opts == null) {
                opts = new HashMap<>();
            }
            for (String key : extras.keySet()) {
                try {
                    String val = extras.getString(key);
                    if (val == null) {
                        throw new Exception();
                    }
                    opts.put(key, val);
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't get string value for key " + key);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing options from bundle", e);
            if (opts.isEmpty()) {
                opts = null;
            }
        }
    }

    private void applyOptions() {
        try {
            String promptVal = opts.get(PROMPT_KEY);
            TextView promptText = (TextView) findViewById(R.id.scanner_view_prompt);
            if (promptText != null) {
                promptText.setText(promptVal);
            }
        } catch (Exception e) {
            Log.i(TAG, "No prompt to load");
        }
        try {
            String skipVal = opts.get(EASY_SKIP_KEY);
            if (skipVal != null) {
                easySkip = Boolean.parseBoolean(skipVal);
            } else {
                Log.i(TAG, "easy_skip_str was null...");
            }
        } catch (Exception e) {
            Log.i(TAG, "Couldn't parse for Easy Skip");
        }
        try {
            String leftVal = opts.get(LEFT_FINGER_ASSIGNMENT_KEY);
            if (leftVal == null) {
                throw new NullPointerException();
            }
            leftFinger = Finger.valueOf(leftVal);
        } catch (Exception e) {
            Log.i(TAG, "No assignment for left_finger, defaulting to thumb");
            leftFinger = Finger.left_thumb;
        }
        try {
            String rightVal = opts.get(RIGHT_FINGER_ASSIGNMENT_KEY);
            if (rightVal == null) {
                throw new NullPointerException();
            }
            rightFinger = Finger.valueOf(rightVal);
        } catch (Exception e) {
            Log.i(TAG, "No assignment for right_finger, defaulting to thumb");
            rightFinger = Finger.right_thumb;
        }
    }

    private void loadDefaultOptions() {
        easySkip = false;
        leftFinger = Finger.left_thumb;
        rightFinger = Finger.right_thumb;
    }

    public void highlightScannedFingers() {
        if (scannedFingers.get(leftFinger.getKey()) != null) {
            leftButton.setBackground(getResources().getDrawable(R.drawable.bg_shape_green_round));
        }
        if (scannedFingers.get(rightFinger.getKey()) != null) {
            rightButton.setBackground(getResources().getDrawable(R.drawable.bg_shape_green_round));
        }
    }

    public void finalizeScans() {

        if (scannedFingers.keySet().size() < 2) {
            Toast t = Toast.makeText(this, getResources().getString(R.string.scan_all_fingers), Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            t.show();
            Log.i("scanning", "quality not enough keys");
            return;
        }

        Map<String, String> scans = new HashMap<>();
        for (Map.Entry<String, String> templateEntry : Controller.mScanner.getIsoTemplates().entrySet()) {
            String key = templateEntry.getKey(), value = templateEntry.getValue();
            if (opts != null && opts.get(key) != null) {
                scans.put(opts.get(key + "_iso"), value);
            } else {
                scans.put(key, value);
            }
        }

        finishOk(scans);
    }

    public void cancelScan(FingerScanTask inter) {
        inter.cancel(true);
    }

    public void restartScanner(View parent) {
        Log.i(TAG, "Starting restart");
        ScannerSetupTask ss = new ScannerSetupTask(this, parent);
        ss.execute();
    }

    protected void unplugScanner(View parent) {
        Log.i(TAG, "Starting Unplug");
        UnplugTask s = new UnplugTask(parent);
        s.execute();
    }

    protected void finishOk(Map<String, String> scans) {
        Intent resultIntent = new Intent();
        for (Map.Entry<String, String> scanEntry : scans.entrySet()) {
            resultIntent.putExtra(scanEntry.getKey(), scanEntry.getValue());
        }
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    protected void finishCancel() {
        Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }

    public void finish() {
        try {
            Controller.mScanner.initMaps();
        } catch (Exception e) {
            Log.d(TAG, "Couldn't clear scanner");
        }
        super.finish();
    }

    /*
     *    Scanning Task Async
     */
    private class FingerScanTask extends AsyncTask<Void, Void, Void> {

        ImageButton view;
        View parent;
        Scanner mScanner;
        String finger;
        boolean success = false;
        boolean reconnect = false;
        boolean triggered = false;
        int starting_image_width, starting_image_height;

        private FingerScanTask(String name, Scanner scanner, ImageButton spot, View parent, boolean instant) {
            super();
            this.parent = parent;
            view = spot;
            finger = name;
            mScanner = scanner;
            this.triggered = !instant;
            starting_image_height = view.getHeight();
            starting_image_width = view.getWidth();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (Controller.mScanner == null) {
                reconnect = true;
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (isCancelled() || checkKillSwitch()) {
                success = false;
                return null;
            }

            new Thread(new Runnable() {
                public void run() {
                    try {
                        while (!Controller.mScanner.isFingerSensed()) {
                            if (isCancelled() || checkKillSwitch()) {
                                Controller.mScanner.cancelScan();
                                return;
                            }
                            if (Controller.mScanner.isReady()) {
                                break;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Log.d(TAG, "interrupted during sleep");
                            }
                        }
                    } catch (Exception e) {
                        if (Controller.mScanner != null) {
                            Controller.mScanner.cancelScan();
                        }
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //FIX
                            dismissProgressDialog();
                            popupPrompt.setText(getResources().getString(R.string.scan_complete));
                            Button b = cancelPopupButton;
                            b.setVisibility(View.GONE);
                            popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            //popUp.setBackgroundDrawable(grey_box);
                            try {
                                popupWindow.showAtLocation(parent, Gravity.CENTER_VERTICAL, 0, 0);
                                popupWindow.update();
                            } catch (WindowManager.BadTokenException e) {
                                Log.e(TAG, "No window to attach popup: " + e.getMessage());
                            }
                        }
                    });
                }
            }).start();

            success = mScanner.scan(finger, triggered);

            if (success) {
                //in case of disconnect, cache items locally
                try {
                    HashMap<String, String> existingBiometrics = Controller.mScanner.getBiometrics();
                    if (!existingBiometrics.isEmpty()) {
                        templateCache = existingBiometrics;
                    }
                    existingBiometrics = Controller.mScanner.getIsoTemplates();
                    if (!existingBiometrics.isEmpty()) {
                        isoTemplateCache = existingBiometrics;
                    }
                    Log.i("PostExec", "Started");
                    Bitmap result = mScanner.getImage(finger);
                    Log.i("bmp_info", Integer.toString(result.getHeight()));
                    Log.i("bmp_info", Integer.toString(result.getWidth()));

                    Log.i(TAG, "Max Image width: " + starting_image_width + " Height: " + starting_image_height);

                    int new_height = (int) Math.round(starting_image_height * .92);
                    int new_width = (int) Math.round(starting_image_width * .92);
                    Log.i(TAG, "new height: " + Integer.toString(new_height));
                    Log.i(TAG, "new width: " + Integer.toString(new_width));

                    final Bitmap scaled = Bitmap.createScaledBitmap(result, new_width, new_height, true);
                    scanImages.put(finger, scaled);
                    Log.i(TAG, "Scaled BMP width: " + Integer.toString(scaled.getWidth()));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                view.setScaleType(ScaleType.CENTER);
                                view.setImageBitmap(scaled);
                            } catch (IllegalArgumentException ille) {
                                Log.e(TAG, "Couldn't apply image to view: " + ille.getMessage());
                            }
                        }
                    });

                    scannedFingers.put(finger, true);

                } catch (RuntimeException np) {
                    Log.e(TAG, "Lost reference to an object!");
                    np.printStackTrace();
                    try {
                        Log.e(TAG, "Attempting to cancel");
                        this.cancel(true);
                    } catch (Exception e3) {
                        Log.e(TAG, "Couldn't cancel!");
                    }
                }
            } else {
                Log.i(TAG, "Caught Scan Failure. Canceling");
                this.cancel(true);
            }
            while (!mScanner.isReady()) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    Log.d(TAG, "Waiting for scanner to complete flush.");
                }
                if (isCancelled()) {
                    Log.i(TAG, "Caught Cancel. Killing");
                    mScanner.setReady(true);
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            try {
                dismissProgressDialog();
                unplugScanner(parent);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, String.format("Cancel: %s", e.getMessage()));
            }
            super.onCancelled();
        }

        protected void onPostExecute(Void res) {
            dismissProgressDialog();
            highlightScannedFingers();
            if (!success) {
                Log.i(TAG, "Scan failed!");
            }
            if (reconnect) {
                Log.d(TAG, "Reconnect post!");
                unplugScanner(view);
            }
        }
    }

    /*
     *    Scanner Unplug Async
     */
    private class UnplugTask extends AsyncTask<Void, Void, Void> {

        View parent;

        private UnplugTask(View parent) {
            this.parent = parent;
        }

        @Override
        protected void onPreExecute() {
            popupPrompt.setText(getResources().getString(R.string.unplug_scanner));
            cancelPopupButton.setVisibility(View.GONE);
            popupWindow.showAtLocation(parent, Gravity.CENTER_VERTICAL, 0, 0);
            popupWindow.update();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, UsbDevice> deviceList;
            Iterator<UsbDevice> deviceIterator;
            while (true) {
                try {
                    deviceList = Controller.mUsbManager.getDeviceList();
                } catch (NullPointerException e) {
                    Log.i(TAG, "No UsbManager Active");
                    return null;
                }
                deviceIterator = deviceList.values().iterator();
                if (!deviceIterator.hasNext()) {
                    Log.i(TAG, "Device Unplugged");
                    break;
                } else {
                    boolean foundValid = false;
                    while (deviceIterator.hasNext()) {
                        UsbDevice device = deviceIterator.next();
                        if (!HostUsbManager.VENDOR_BLACKLIST.contains(device.getVendorId())) {
                            foundValid = true;
                        }
                    }
                    if (!foundValid) {
                        break;
                    }
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Log.d(TAG, "interrupted during sleep");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dismissProgressDialog();
            restartScanner(parent);
            super.onPostExecute(result);
        }
    }

    /*
    *    Scanner Setup Async
    */
    private class ScannerSetupTask extends AsyncTask<Void, Void, Void> {

        private Context oldContext;
        private View parent;
        private ScannerSetupTask a_process;

        ScannerSetupTask(Context context, View parent) {
            super();
            a_process = this;
            this.parent = parent;
            oldContext = context;
        }

        protected void onPreExecute() {
            popupPrompt.setText(getResources().getString(R.string.attach_scanner));
            cancelPopupButton.setVisibility(View.VISIBLE);
            cancelPopupButton.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    a_process.cancel(true);
                }
            });
            //pop_cancel.setVisibility(View.GONE);
            //FIX
            popupWindow.showAtLocation(parent, Gravity.CENTER_VERTICAL, 0, 0);
            popupWindow.update();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //TODO kill getApp
            Controller.mUsbManager = null;
            Controller.mHostUsbManager = null;
            Controller.mDevice = null;
            Controller.mUsbManager = (UsbManager) oldContext.getSystemService(Context.USB_SERVICE);
            Controller.mHostUsbManager = new HostUsbManager(Controller.mUsbManager);
            waitingForPermission = true;
            while (true) {
                try {
                    if (isCancelled() || killSwitch) {
                        Log.i(TAG, "Fingerprint Scanner Not Needed -- Canceling Receiver");
                        Controller.mHostUsbManager = null;
                        Controller.mDevice = null;
                        throw new Exception("FP Canceled!");
                    }
                    Thread.sleep(250);

                    if (freeScanner != null) {
                        Controller.mDevice = freeScanner;
                        if (Controller.mUsbManager.hasPermission(Controller.mDevice)) {
                            Log.d(TAG, "We have permission, loading attached usb");
                            waitingForPermission = false;

                        } else {
                            Log.e(TAG, "No Scanner permission!!");
                            Controller.mUsbManager.requestPermission(Controller.mDevice, mPermissionIntent);
                        }
                        break;
                    }
                } catch (NullPointerException e2) {
                    Log.i(TAG, "no permission...");
                } catch (Exception e2) {
                    Log.i(TAG, "Cancelled!");
                    e2.printStackTrace();
                    break;
                }
            }
            while (true) {
                Log.i(TAG, "In loop...");
                if (!isCancelled()) {
                    int c = 0;
                    while (waitingForPermission && !killSwitch) {
                        try {
                            Thread.sleep(100);
                            Log.i(TAG, String.format("waiting still for permission... killed: %s", killSwitch));
                        } catch (InterruptedException e) {
                            Log.d(TAG, "interrupted during sleep");
                        }
                        if (isCancelled() || checkKillSwitch()) {
                            return null;
                        }
                        c += 1;
                        if (c > 50) {
                            cancel(true);
                        }
                    }
                    if (Controller.mDevice != null) {
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (Controller.mScanner == null) {
                            Log.i(TAG, "Scanner is null!");
                            Controller.mScanner = new LumidigmMercuryScanner(Controller.mDevice, Controller.mUsbManager);
                        } else {
                            Log.i(TAG, "Scanner already exists!");
                            Log.i(TAG, "Wiping Scanner");
                            Controller.mScanner = new LumidigmMercuryScanner(Controller.mDevice, Controller.mUsbManager);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                popupPrompt.setText(getResources().getString(R.string.please_wait));
                                Button b = cancelPopupButton;
                                b.setVisibility(View.GONE);
                                popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                try {
                                    popupWindow.showAtLocation(parent, Gravity.CENTER_VERTICAL, 0, 0);
                                    popupWindow.update();
                                } catch (WindowManager.BadTokenException e) {
                                    Log.e(TAG, "No window to attach popup: " + e.getMessage());
                                }
                            }
                        });
                        while (!Controller.mScanner.isReady()) {
                            if (isCancelled() || checkKillSwitch()) {
                                return null;
                            }
                            if (Scanner.isScanCancelled()) {
                                return null;
                            }
                            //if scan init failed
                            try {
                                Thread.sleep(250);
                                if (Controller.mScanner == null) {
                                    break;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.i(TAG, "Scanner isn't ready...");
                        }
                        break;
                    } else {
                        Log.i(TAG, "Scanner issue! scanner is null");
                        break;
                    }
                } else {
                    //Cancelled
                    Log.i(TAG, "Cancelled");
                    break;
                }
            }
            if (isCancelled()) {
                return null;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.d(TAG, "interrupted during sleep", e);
            }
            return null;
        }

        @Override
        protected void onCancelled(Void res) {
            try {
                Controller.mScanner = null;
                dismissProgressDialog();
            } catch (Exception e) {
                Log.i(TAG, "Couldn't kill scanner on cancel");
            }
        }

        @Override
        protected void onPostExecute(Void res) {
            try {
                if (Controller.mScanner != null) {
                    for (Map.Entry<String, String> template: templateCache.entrySet()) {
                        Controller.mScanner.setBiometrics(template.getKey(), template.getValue());
                    }
                    for (Map.Entry<String, String> isoTemplate : isoTemplateCache.entrySet()) {
                        Controller.mScanner.setIsoTemplate(isoTemplate.getKey(), isoTemplate.getValue());
                    }
                }
                dismissProgressDialog();
            } catch (NullPointerException np) {
                Log.e(TAG, "Couldn't finish scanner setup", np);
                try {
                    dismissProgressDialog();
                } catch (Exception e2) {
                    Log.e(TAG, "Couldn't dismiss dialog!");
                }
            }
        }
    }

    public static Intent getNextScan(Bundle extrasIn, int index) {

        Bundle extrasOut = new Bundle();

        if (index == 0) {
            for (String field : SCAN_FIELDS) {
                try {
                    String value = extrasIn.getString(field);
                    if (value != null) {
                        extrasOut.putString(field, value);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error constructing intent extras from bundle", e);
                    return null;
                }
            }
        }

        if (extrasOut.isEmpty()) {
            for (String field : SCAN_FIELDS) {
                try {
                    extrasOut.putString(field, extrasIn.getString(field + "_" + index));
                } catch (Exception e) {
                    Log.i(TAG, "error constructing intent extras from bundle", e);
                    return null;
                }
            }
        }

        Intent result = new Intent();
        result.setAction(INTERNAL_SCAN_ACTION);
        result.putExtras(extrasOut);
        return result;
    }

    public static int getScanCount(Bundle data) {
        int out = 1;
        for (int x = 0; x < 10; x++) {
            String left = data.getString(LEFT_FINGER_ASSIGNMENT_KEY + "_" + x);
            if (left == null) {
                out = x;
                break;
            }
        }
        if (out < 1) {
            return 1;
        }
        return out;
    }
}


