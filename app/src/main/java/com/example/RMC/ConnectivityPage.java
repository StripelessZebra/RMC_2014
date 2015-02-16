package com.example.RMC;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import java.util.ArrayList;

/**
 * This is the main Activity that displays the current chat session.
 */
public class ConnectivityPage extends Activity implements SensorEventListener, NumberPicker.OnValueChangeListener {
    // Debugging
    private static final String TAG = "ConnectivityPage";
    private static final boolean D = true;

    // Message types sent from the ConnectivityPageService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the ConnectivityPageService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private ConnectivityPageService mChatService = null;

    /**Imports for sensors**/
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    TextView tv, defaultTV;
    Button eraseAnnotationsBtn, leftMouseBtn, rightMouseBtn, mediaPlayIncrease , mediaPlayDecrease, jumpToSlideBtn,blankScreenBtn;
    LinearLayout programSelectionLL, toggleButtonLL, highlightToggleLL, mouseButtonLL;
    RelativeLayout ppt,mediaPlay;
    Spinner programSelectionSpinner;
    CustomProgramList spinnerAdapter;
    Switch toggle;
    ImageView pptMinimize, pptMaximize, pptLeft, pptRight, mediaPlayMaximize, mediaPlayMute, mediaPlayPlay, mediaPlayStop, mediaPlayNext, mediaPlayPrev;
    SharedPreferences.Editor editor;
    String pairedDeviceAddress,hasOnCreateOptionsMenuBeenCreated,isDeviceConnected = "", isMotionControlSelected="";
    Menu settingsMenu;
    MenuItem connectBT, disconnectBT, userManual, receivedPPTSlides, openPPT, openMP;
    Dialog loadingDialog;
    int currentSlideNumber_Menu = 1;
    boolean wasUpGestureValueChanged = false;
    boolean isLaserPointerOn = false;
    boolean isHighlighterOn = false;
    boolean isOpenMPSelected = false;

    private RadioGroup pptToolsRG;
    private RadioButton pptCursor;
    private RadioButton pptHighlighter;
    private RadioButton pptLaserPointer;
    private RadioButton defaultRadioButton;

    ArrayList<String> receivedPPTNotes = new ArrayList<String>();
    ArrayList<String> receivedPPTTitle = new ArrayList<String>();
    ArrayList<String> receivedPPTText = new ArrayList<String>();
    String pptFilePathFromPC = "";
    int pptNumberOfSlides = 0;
    int selectedSlideNumber = 0;

    String[] program = {
            "Microsoft PowerPoint",
            "Windows Media Player"
    } ;
    Integer[] imageId = {
            R.drawable.powerpoint,
            R.drawable.media_player
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        // Register for broadcasts on BluetoothAdapter state change
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        editor = getSharedPreferences("RMCSP", MODE_PRIVATE).edit();
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if(mBluetoothAdapter.isEnabled()) {
            SharedPreferences prefs = getSharedPreferences("RMCSP", MODE_PRIVATE);
            String deviceName = prefs.getString("deviceName", null);
            String deviceAddress = prefs.getString("deviceAddress", null);

            if (deviceName == null && deviceAddress == null) {
                if(hasOnCreateOptionsMenuBeenCreated =="YES" && isDeviceConnected =="") {
                    connectBT.setVisible(true);
                    disconnectBT.setVisible(false);
                }
            }
            else if (deviceName != null && deviceAddress != null) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                if (mChatService == null) setupChat();
                mChatService.connect(device, true);
                //Toast.makeText(getApplicationContext(),deviceName, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(),deviceAddress, Toast.LENGTH_SHORT).show();
            }
        }

        Drawable dr = getResources().getDrawable(R.drawable.cursor);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));


        // Set up the window layout
        setContentView(R.layout.connectivity_page);

        tv = (TextView) findViewById(R.id.textView1);
        defaultTV=(TextView) findViewById(R.id.defaultTextTV);

        toggleButtonLL = (LinearLayout) findViewById(R.id.toggleButtonLL);
        toggleButtonLL.setVisibility(View.GONE);

        highlightToggleLL = (LinearLayout) findViewById(R.id.highlightToggleButtonLL);
        highlightToggleLL.setVisibility(View.GONE);

        mouseButtonLL = (LinearLayout)findViewById(R.id.mouseButtonLL);
        mouseButtonLL.setVisibility(View.GONE);

        eraseAnnotationsBtn = (Button) findViewById(R.id.eraseAnnotationBtn);
        eraseAnnotationsBtn.setVisibility(View.GONE);

        leftMouseBtn = (Button) findViewById(R.id.leftMouseBtn);
        rightMouseBtn = (Button) findViewById(R.id.rightMouseBtn);

        programSelectionLL = (LinearLayout) findViewById(R.id.programSelectionLL);
        programSelectionLL.setVisibility(View.GONE);

        ppt = (RelativeLayout) findViewById(R.id.ppt);
        ppt.setVisibility(View.GONE);

        mediaPlay = (RelativeLayout) findViewById(R.id.mediaPlay);
        mediaPlay.setVisibility(View.GONE);

        programSelectionSpinner = (Spinner) findViewById(R.id.programSelectionSpinner);
        spinnerAdapter = new CustomProgramList(this, program, imageId);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        programSelectionSpinner.setAdapter(spinnerAdapter);

        programSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(),programSelectionSpinner.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
                if(isMotionControlSelected!="YES"){
                    if (programSelectionSpinner.getSelectedItem().toString().equals("Microsoft PowerPoint")) {
                        ppt.setVisibility(View.VISIBLE);
                        mediaPlay.setVisibility(View.GONE);
                        toggleButtonLL.setVisibility(View.VISIBLE);
                        openMP.setVisible(false);
                        if(receivedPPTNotes.size()!=0){
                            receivedPPTSlides.setVisible(true);
                        }
                        else if(receivedPPTNotes.size()==0){
                            openPPT.setVisible(true);
                        }
                    } else if (programSelectionSpinner.getSelectedItem().toString().equals("Windows Media Player")) {
                        ppt.setVisibility(View.GONE);
                        mediaPlay.setVisibility(View.VISIBLE);
                        toggleButtonLL.setVisibility(View.GONE);
                        highlightToggleLL.setVisibility(View.GONE);
                        mouseButtonLL.setVisibility(View.GONE);
                        receivedPPTSlides.setVisible(false);
                        openPPT.setVisible(false);
                        if(isOpenMPSelected==false) {
                            openMP.setVisible(true);
                        }
                    }
                }
                else if(isMotionControlSelected=="YES"){
                    if (programSelectionSpinner.getSelectedItem().toString().equals("Microsoft PowerPoint")) {
                        ppt.setVisibility(View.VISIBLE);
                        mediaPlay.setVisibility(View.GONE);
                        toggleButtonLL.setVisibility(View.VISIBLE);
                        openMP.setVisible(false);
                        if(receivedPPTNotes.size()!=0){
                            receivedPPTSlides.setVisible(true);
                        }
                        else if(receivedPPTNotes.size()==0){
                            openPPT.setVisible(true);
                        }
                    } else if (programSelectionSpinner.getSelectedItem().toString().equals("Windows Media Player")) {
                        ppt.setVisibility(View.GONE);
                        mediaPlay.setVisibility(View.VISIBLE);
                        toggleButtonLL.setVisibility(View.GONE);
                        toggle.setChecked(false);
                        highlightToggleLL.setVisibility(View.GONE);
                        mouseButtonLL.setVisibility(View.GONE);
                        receivedPPTSlides.setVisible(false);
                        openPPT.setVisible(false);
                        if(isOpenMPSelected==false) {
                            openMP.setVisible(true);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        pptToolsRG = (RadioGroup) findViewById(R.id.pptToolsRG);
        pptCursor = (RadioButton) findViewById(R.id.pptCursor);
        Drawable cursorImg = getResources().getDrawable(R.drawable.cursor);
        cursorImg.setBounds( 0, 0, 100, 100 );
        pptCursor.setCompoundDrawables(cursorImg, null, null, null);
        pptHighlighter = (RadioButton) findViewById(R.id.pptHighlighter);
        Drawable highlighterImg = getResources().getDrawable(R.drawable.highlighter);
        highlighterImg.setBounds( 0, 0, 100, 100 );
        pptHighlighter.setCompoundDrawables(highlighterImg, null, null, null );
        pptLaserPointer = (RadioButton) findViewById(R.id.pptLaserPointer);
        Drawable laserPointerImg = getResources().getDrawable(R.drawable.laser_icon);
        laserPointerImg.setBounds( 0, 0, 100, 100 );
        pptLaserPointer.setCompoundDrawables(laserPointerImg, null, null, null );

        pptMinimize = (ImageView) findViewById(R.id.pptMinimize);
        pptMaximize = (ImageView) findViewById(R.id.pptMaximize);
        pptLeft = (ImageView) findViewById(R.id.pptLeft);
        pptRight = (ImageView) findViewById(R.id.pptRight);
        blankScreenBtn = (Button) findViewById(R.id.blankScreenBtn);
        jumpToSlideBtn = (Button) findViewById(R.id.jumpToSlideBtn);
        jumpToSlideBtn.setVisibility(View.GONE);

        pptMinimize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="ppt min";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 200 milliseconds
                v.vibrate(200);
            }
        });

        pptMaximize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="ppt max";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        pptLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="ppt pre";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        pptRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="ppt nex";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        blankScreenBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="blanksc";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        jumpToSlideBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpToSlideDialog();
            }
        });

        mediaPlayMaximize = (ImageView) findViewById(R.id.mediaPlayMaximize);
        mediaPlayMute = (ImageView) findViewById(R.id.mediaPlayMute);
        mediaPlayPlay = (ImageView) findViewById(R.id.mediaPlayPlay);
        mediaPlayStop = (ImageView) findViewById(R.id.mediaPlayStop);
        mediaPlayNext = (ImageView) findViewById(R.id.mediaPlayNext);
        mediaPlayPrev = (ImageView) findViewById(R.id.mediaPlayPrev);
        mediaPlayIncrease = (Button) findViewById(R.id.mediaPlayIncrease);
        mediaPlayDecrease = (Button) findViewById(R.id.mediaPlayDecrease);

        mediaPlayMaximize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp max";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        mediaPlayMute.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp mute";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        mediaPlayPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp play";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        mediaPlayStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp stop";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        mediaPlayNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp next";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        mediaPlayPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp prev";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        mediaPlayIncrease.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp incr";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        mediaPlayDecrease.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp decr";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        toggle = (Switch) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    ppt.setVisibility(View.GONE);
                    mediaPlay.setVisibility(View.GONE);
                    isMotionControlSelected="YES";
                    highlightToggleLL.setVisibility(View.VISIBLE);
                    mouseButtonLL.setVisibility(View.VISIBLE);
                    pptToolsRG.check(R.id.pptCursor);
                    sendMessage("show cr");
                } else {
                    // The toggle is disabled
                    pptCursor.setChecked(true);
                    pptHighlighter.setChecked(false);
                    pptLaserPointer.setChecked(false);
                    highlightToggleLL.setVisibility(View.GONE);
                    mouseButtonLL.setVisibility(View.GONE);
                    eraseAnnotationsBtn.setVisibility(View.GONE);
                    new CountDownTimer(100, 100) {
                        public void onTick(long millisUntilFinished) {

                        }
                        public void onFinish() {
                            if(isLaserPointerOn == true) {
                                isLaserPointerOn = false;
                                sendMessage("LP off");
                            }
                            if(isHighlighterOn == true) {
                                isHighlighterOn = false;
                                sendMessage("HL off");
                            }
                            sendMessage("hide cr");
                        }
                    }.start();

                    if (programSelectionSpinner.getSelectedItem().toString().equals("Microsoft PowerPoint")){
                        ppt.setVisibility(View.VISIBLE);
                        mediaPlay.setVisibility(View.GONE);
                    }
                    else if(programSelectionSpinner.getSelectedItem().toString().equals("Windows Media Player")) {
                        ppt.setVisibility(View.GONE);
                        mediaPlay.setVisibility(View.VISIBLE);
                        toggleButtonLL.setVisibility(View.GONE);
                        highlightToggleLL.setVisibility(View.GONE);
                        mouseButtonLL.setVisibility(View.GONE);
                    }
                    isMotionControlSelected="";
                }
            }
        });

        eraseAnnotationsBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("eraseAn");
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        leftMouseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("leftck");
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });

        rightMouseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("rightck");
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        SharedPreferences prefs = getSharedPreferences("RMCSP", MODE_PRIVATE);
        int upGestureValueSensitivity = prefs.getInt("upGestureValue", 2);
        int downGestureValueSensitivity = prefs.getInt("downGestureValue", 2);
        String downGestureValueSensitivityConverted = "-"+downGestureValueSensitivity;
        int leftGestureValueSensitivity = prefs.getInt("leftGestureValue", 2);
        int rightGestureValueSensitivity = prefs.getInt("rightGestureValue", 2);
        String rightGestureValueSensitivityConverted = "-"+rightGestureValueSensitivity;

        if(isMotionControlSelected=="YES") {

            pptToolsRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    defaultRadioButton = (RadioButton) findViewById(checkedId);
                    if (defaultRadioButton.getText().toString().equals("Cursor")) {
                        if(isLaserPointerOn == true) {
                            isLaserPointerOn = false;
                            sendMessage("LP off");
                        }
                        if(isHighlighterOn == true) {
                            isHighlighterOn = false;
                            sendMessage("HL off");
                        }
                        mouseButtonLL.setVisibility(View.VISIBLE);
                        eraseAnnotationsBtn.setVisibility(View.GONE);
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(50);
                    }
                    else if (defaultRadioButton.getText().toString().equals("Highlighter")) {
                        if(isLaserPointerOn == true) {
                            isLaserPointerOn = false;
                            sendMessage("LP off");
                        }
                        new CountDownTimer(100, 100) {
                            public void onTick(long millisUntilFinished) {

                            }
                            public void onFinish() {
                                isHighlighterOn = true;
                                sendMessage("HL on");
                                mouseButtonLL.setVisibility(View.GONE);
                                eraseAnnotationsBtn.setVisibility(View.VISIBLE);
                            }
                         }.start();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(50);
                    }
                    else if (defaultRadioButton.getText().toString().equals("Laser Pointer")) {
                        if(isHighlighterOn == true) {
                            isHighlighterOn = false;
                            sendMessage("HL off");
                        }
                        new CountDownTimer(100, 100) {
                            public void onTick(long millisUntilFinished) {

                            }
                            public void onFinish() {
                                isLaserPointerOn = true;
                                sendMessage("LP on");
                                mouseButtonLL.setVisibility(View.GONE);
                                eraseAnnotationsBtn.setVisibility(View.GONE);
                            }
                        }.start();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(50);
                    }

                }
            });

            //Left
            if (Math.round(x) >= leftGestureValueSensitivity) {
                //Log.i("ACCELEROMETER X: ", "LEFT " + String.valueOf(x));
                //String message = "ppt pre";
                String accValue = "";
                if(Math.round(x)<10){
                    accValue = "0" + String.valueOf(Math.round(x)-1);
                }
                else if(Math.round(x)>=10){
                    accValue = String.valueOf(Math.round(x)-1);
                }

                //String message = "aLt  " + cursorSeekValue;
                String message = "aLt  " + accValue;
                sendMessage(message);
            }
            //Right
            else if (Math.round(x) <= Integer.parseInt(rightGestureValueSensitivityConverted)) {
                //Log.i("ACCELEROMETER X: ", "RIGHT" + String.valueOf(x));
                //String message = "ppt nex";
                String accValue = "";
                if(Math.round(Math.abs(x))<10){
                    accValue = "0" + String.valueOf(Math.round(Math.abs(x))-1);
                }
                else if(Math.round(Math.abs(x))>=10){
                    accValue = String.valueOf(Math.round(Math.abs(x))-1);
                }
                //String message = "aRt  " + cursorSeekValue;
                String message = "aRt  " + accValue;
                sendMessage(message);
            }
            //UP
            if (Math.round(y) >= upGestureValueSensitivity) {
                //Log.i("ACCELEROMETER Y: ", "UP " + String.valueOf(y));
                String accValue = "";
                if(Math.round(y)<10){
                    accValue = "0" + String.valueOf(Math.round(y)-1);
                }
                else if(Math.round(y)>=10){
                    accValue = String.valueOf(Math.round(y)-1);
                }
                //String message = "aUp  " + cursorSeekValue;
                String message = "aUp  " + accValue;
                sendMessage(message);
            }
            //Down
            else if (Math.round(y) <= Integer.parseInt(downGestureValueSensitivityConverted)) {
                //Log.i("ACCELEROMETER Y: ", "DOWN " +String.valueOf(y));
                String accValue = "";
                if(Math.round(Math.abs(y))<10){
                    accValue = "0" + String.valueOf(Math.round(Math.abs(y))-1);
                }
                else if(Math.round(Math.abs(y))>=10){
                    accValue = String.valueOf(Math.round(Math.abs(y))-1);
                }
                //String message = "aDn  " + cursorSeekValue;
                String message = "aDn  " + accValue;
                sendMessage(message);
            }
            }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == ConnectivityPageService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
        if(hasOnCreateOptionsMenuBeenCreated =="YES" && isDeviceConnected=="YES") {
            connectBT.setVisible(false);
            disconnectBT.setVisible(true);
        }
        else if(hasOnCreateOptionsMenuBeenCreated =="YES" && isDeviceConnected =="") {
            connectBT.setVisible(true);
            disconnectBT.setVisible(false);
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");
        // Initialize the ConnectivityPageService to perform bluetooth connections
        mChatService = new ConnectivityPageService(this, mHandler);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        sendMessage("Disconn");
        mChatService.stop();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        mChatService.stop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        sendMessage("Disconn");
        if (mChatService != null) mChatService.stop();
        mBluetoothAdapter.disable();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != ConnectivityPageService.STATE_CONNECTED) {
            //Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the ConnectivityPageService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    if(D) Log.i(TAG, "END onEditorAction");
                    return true;
                }
            };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        pairedDeviceAddress = address;
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        this.settingsMenu = menu;
        inflater.inflate(R.menu.connectivity_page, menu);
        connectBT = settingsMenu.findItem(R.id.secure_connect_scan);
        disconnectBT = settingsMenu.findItem(R.id.disconnectDevice);
        openPPT = settingsMenu.findItem(R.id.openPPT);
        openPPT.setVisible(false);
        openMP = settingsMenu.findItem(R.id.openMP);
        openMP.setVisible(false);
        receivedPPTSlides = settingsMenu.findItem(R.id.receivedPPTSlides);
        receivedPPTSlides.setVisible(false);
        userManual = settingsMenu.findItem(R.id.userManual);
        userManual.setVisible(false);
        //Checking if user has previously connected to any device through this app
        SharedPreferences prefs = getSharedPreferences("RMCSP", MODE_PRIVATE);
        String deviceName = prefs.getString("deviceName", null);
        String deviceAddress = prefs.getString("deviceAddress", null);
        hasOnCreateOptionsMenuBeenCreated = "YES";
        if (deviceName == null && deviceAddress == null) {
            if(isDeviceConnected =="") {
                connectBT.setVisible(true);
                disconnectBT.setVisible(false);
            }
        } else{
            connectBT.setVisible(false);
            disconnectBT.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                if(mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    // device is discoverable & connectable
                    //Toast.makeText(getApplicationContext(),"Already Discoverable",Toast.LENGTH_SHORT).show();
                } else {
                    // device is not discoverable & connectable
                    // Ensure this device is discoverable by others
                    //ensureDiscoverable();
                }
                return true;
            case R.id.disconnectDevice:
                sendMessage("Disconn");
                mChatService.stop();
                connectBT.setVisible(true);
                disconnectBT.setVisible(false);
                return true;
            case R.id.userManual:
                Intent userManual = new Intent(ConnectivityPage.this, UserManual.class);
                startActivity(userManual);
                return true;
            case R.id.openMP:
                openMPPlaylist();
                return true;

            case R.id.openPPT:
                openPPTFile();
                return true;

            case R.id.receivedPPTSlides:
                displayPPTSlides();
                return true;
        }
        return false;
    }

    // The Handler that gets information back from the ConnectivityPageService
    private final Handler mHandler = new Handler() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case ConnectivityPageService.STATE_CONNECTED:
                            tv.setText("");
                            tv.setBackground(getResources().getDrawable(R.drawable.connected));
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(400);
                            //Toast.makeText(getApplicationContext(),"Pairing successful", Toast.LENGTH_SHORT).show();
                            if(mConnectedDeviceName!=null && pairedDeviceAddress != null) {
                                editor.putString("deviceName", mConnectedDeviceName);
                                editor.putString("deviceAddress", pairedDeviceAddress);
                                Log.i(TAG, mConnectedDeviceName + " " + pairedDeviceAddress);
                                editor.commit();
                            }
                            toggle.setChecked(false);
                            isDeviceConnected = "YES";
                            programSelectionLL.setVisibility(View.VISIBLE);
                            toggleButtonLL.setVisibility(View.VISIBLE);
                            defaultTV.setText("Connected to: " + mConnectedDeviceName + ".");
                            if(hasOnCreateOptionsMenuBeenCreated =="YES" && isDeviceConnected=="YES") {
                                connectBT.setVisible(false);
                                disconnectBT.setVisible(true);
                                userManual.setVisible(false);
                            }
                            if(isMotionControlSelected!="YES"){
                                if (programSelectionSpinner.getSelectedItem().toString().equals("Microsoft PowerPoint")) {
                                    ppt.setVisibility(View.VISIBLE);
                                    mediaPlay.setVisibility(View.GONE);
                                    openPPT.setVisible(true);
                                    openMP.setVisible(false);
                                } else if (programSelectionSpinner.getSelectedItem().toString().equals("Windows Media Player")) {
                                    ppt.setVisibility(View.GONE);
                                    mediaPlay.setVisibility(View.VISIBLE);
                                    toggleButtonLL.setVisibility(View.GONE);
                                    highlightToggleLL.setVisibility(View.GONE);
                                    mouseButtonLL.setVisibility(View.GONE);
                                    openPPT.setVisible(false);
                                    if(isOpenMPSelected==false) {
                                        openMP.setVisible(true);
                                    }
                                }
                            }
                            isOpenMPSelected = false;
                            currentSlideNumber_Menu = 1;
                            loadingDialog.dismiss();
                            break;
                        case ConnectivityPageService.STATE_CONNECTING:
                            tv.setText("");
                            tv.setBackground(getResources().getDrawable(R.drawable.connecting));
                            defaultTV.setText(R.string.title_connecting);
                            displayLoadingDialog("connect");
                            if(hasOnCreateOptionsMenuBeenCreated =="YES") {
                                connectBT.setVisible(false);
                                disconnectBT.setVisible(false);
                                userManual.setVisible(false);
                            }
                            break;
                        case ConnectivityPageService.STATE_LISTEN:
                        case ConnectivityPageService.STATE_NONE:
                            tv.setText("");
                            tv.setBackground(getResources().getDrawable(R.drawable.disconnected));
                            receivedPPTNotes.clear();
                            receivedPPTTitle.clear();
                            receivedPPTText.clear();
                            pptFilePathFromPC="";
                            pptNumberOfSlides=0;
                            defaultTV.setText(R.string.not_connected);
                            programSelectionLL.setVisibility(View.GONE);
                            ppt.setVisibility(View.GONE);
                            mediaPlay.setVisibility(View.GONE);
                            toggleButtonLL.setVisibility(View.GONE);
                            highlightToggleLL.setVisibility(View.GONE);
                            mouseButtonLL.setVisibility(View.GONE);
                            eraseAnnotationsBtn.setVisibility(View.GONE);
                            isDeviceConnected = "";
                            jumpToSlideBtn.setVisibility(View.GONE);
                            currentSlideNumber_Menu = 1;
                            isOpenMPSelected = false;
                            loadingDialog.dismiss();
                            if(hasOnCreateOptionsMenuBeenCreated =="YES" && isDeviceConnected =="") {
                                connectBT.setVisible(true);
                                disconnectBT.setVisible(false);
                                openMP.setVisible(false);
                                openPPT.setVisible(false);
                                receivedPPTSlides.setVisible(false);
                                userManual.setVisible(true);
                            }
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //Toast.makeText(getApplicationContext(),writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    final String readMessage = new String(readBuf, 0, msg.arg1);
                    new CountDownTimer(100, 100) {
                        public void onTick(long millisUntilFinished) {

                        }
                        public void onFinish() {
                            //Toast.makeText(getApplicationContext(),readMessage,Toast.LENGTH_SHORT).show();
                            if(readMessage.contains("pptFilePath")){
                                pptFilePathFromPC = removeWords(readMessage, "pptFilePath");
                            }
                            else if(readMessage.contains("pptSlideCount")){
                                pptNumberOfSlides = Integer.parseInt(removeWords(readMessage, "pptSlideCount"));
                            }
                            else if(readMessage.contains("contains")) {
                                receivedPPTNotes.add(removeWords(readMessage, "contains"));
                                Log.i("RECEIVED: ", "RECEIVED: " + readMessage);
                            }
                            else if(readMessage.contains("contentText")){
                                receivedPPTText.add(removeWords(readMessage, "contentText"));
                                Log.i("Content: ", "Content: " + readMessage);
                            }
                            else if(readMessage.contains("slideTitle")){
                                receivedPPTTitle.add(removeWords(readMessage, "slideTitle"));
                                Log.i("TITLE: ", "TITLE: " + readMessage);
                            }
                            else if(readMessage.equals("startOfRetrieving")){
                                displayLoadingDialog("slides");
                            }
                            else if(readMessage.equals("endOfRetrieving")){
                                if(pptFilePathFromPC.equals("")){
                                    receivedPPTSlides.setVisible(false);
                                    openPPT.setVisible(true);
                                }
                                else{
                                    if (programSelectionSpinner.getSelectedItem().toString().equals("Microsoft PowerPoint")) {
                                        receivedPPTSlides.setVisible(true);
                                        openPPT.setVisible(false);
                                    } else if (programSelectionSpinner.getSelectedItem().toString().equals("Windows Media Player")) {
                                        receivedPPTSlides.setVisible(false);
                                        openPPT.setVisible(false);
                                    }
                                }
                                if(pptNumberOfSlides!=0){
                                    jumpToSlideBtn.setVisibility(View.VISIBLE);
                                }
                                else{
                                    jumpToSlideBtn.setVisibility(View.GONE);
                                }
                                loadingDialog.dismiss();
                            }
                            else if(readMessage.equals("endOfOpeningMP")){
                                isOpenMPSelected = true;
                                openMP.setVisible(false);
                            }
                        }
                    }.start();
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    //Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    //Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // It means the user has changed his bluetooth state.
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
                    // The user bluetooth is turning off yet, but it is not disabled yet.
                    mChatService.stop();
                    return;
                }

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    // The user bluetooth is already disabled.
                    return;
                }

            }
        }
    };

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        Log.i("value is",""+newVal);
    }

    public static String removeWords(String word ,String remove) {
        return word.replace(remove,"");
    }

    public void displayPPTSlides(){
        if(pptNumberOfSlides!=0) {
            final Dialog d = new Dialog(ConnectivityPage.this);
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.slide_content_layout);
            d.setCanceledOnTouchOutside(false);
            final TextView pptSlideTitle = (TextView) d.findViewById(R.id.pptSlideTitle);
            pptSlideTitle.setMovementMethod(new ScrollingMovementMethod());
            final TextView slideNumberTv = (TextView) d.findViewById(R.id.slideNumberTv);
            slideNumberTv.setText("Slide Number: " + String.valueOf(currentSlideNumber_Menu));
            SeekBar slideSeek = (SeekBar) d.findViewById(R.id.slideSeek);
            slideSeek.setMax(pptNumberOfSlides - 1);
            slideSeek.setProgress(currentSlideNumber_Menu-1);
            Button goToSlideBtn = (Button) d.findViewById(R.id.goToSlideBtn);
            Button closeSlideBtn = (Button) d.findViewById(R.id.closeSlideBtn);
            Log.i(TAG, TAG + ": " + receivedPPTText.size());
            ExpandableListView expandableList = (ExpandableListView)d.findViewById(R.id.expandableList);
            expandableList.setDividerHeight(2);
            expandableList.setGroupIndicator(null);
            expandableList.setClickable(true);
            final ArrayList<String> parentItems = new ArrayList<String>();
            final ArrayList<Object> childItems = new ArrayList<Object>();
            final ExpandableAdapter adapter = new ExpandableAdapter(parentItems, childItems);
            adapter.setInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), this);
            expandableList.setAdapter(adapter);
            parentItems.add("Content");
            parentItems.add("Notes");
            //Content
            final ArrayList<String> contentChild = new ArrayList<String>();
            contentChild.clear();
            if(receivedPPTText.get(currentSlideNumber_Menu - 1).toString().equals("")) {
                contentChild.add("*No Content Found.");
                childItems.add(contentChild);
            }
            else {
                contentChild.add(receivedPPTText.get(currentSlideNumber_Menu - 1).toString());
                childItems.add(contentChild);
            }
            //Notes
            final ArrayList<String> notesChild = new ArrayList<String>();
            notesChild.clear();
            if(receivedPPTNotes.get(currentSlideNumber_Menu - 1).toString().equals("")) {
                notesChild.add("*No Notes Found.");
                childItems.add(notesChild);
            }
            else {
                notesChild.add(receivedPPTNotes.get(currentSlideNumber_Menu - 1).toString());
                childItems.add(notesChild);
            }
            if(receivedPPTTitle.get(currentSlideNumber_Menu - 1).toString().equals("")) {
                pptSlideTitle.setText("*No Title Found.");
            }
            else{
                pptSlideTitle.setText(receivedPPTTitle.get(currentSlideNumber_Menu - 1).toString());
            }
            slideSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    contentChild.clear();
                    notesChild.clear();
                    currentSlideNumber_Menu = progress + 1;
                    slideNumberTv.setText("Slide Number: " + String.valueOf(currentSlideNumber_Menu));
                    wasUpGestureValueChanged = true;
                    if(receivedPPTText.get(progress).toString().equals("")) {
                        contentChild.add("*No Content Found.");
                        childItems.add(contentChild);
                    }
                    else {
                        contentChild.add(receivedPPTText.get(progress).toString());
                        childItems.add(contentChild);
                    }
                    if(receivedPPTNotes.get(progress).toString().equals("")) {
                        notesChild.add("*No Notes Found.");
                        childItems.add(notesChild);
                    }
                    else {
                        notesChild.add(receivedPPTNotes.get(progress).toString());
                        childItems.add(notesChild);
                    }
                    if(receivedPPTTitle.get(progress).toString().equals("")) {
                        pptSlideTitle.setText("*No Title Found.");
                    }
                    else{
                        pptSlideTitle.setText(receivedPPTTitle.get(progress).toString());
                    }
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            //set Expandable list to open first parent group
            expandableList.expandGroup(1);
            goToSlideBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                     sendMessage("goto " + currentSlideNumber_Menu);
                     Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                     v.vibrate(200);
                }
            });
            closeSlideBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    d.dismiss();
                }
            });
            d.show();
        }
        else{

        }
    }

    public void displayLoadingDialog(String loadingType){
        loadingDialog = new Dialog(ConnectivityPage.this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_layout);
        ProgressBar pb = (ProgressBar)loadingDialog.findViewById(R.id.progress_bar);
        pb.setVisibility(View.VISIBLE);
        TextView tv = (TextView) loadingDialog.findViewById(R.id.loading_message);
        if(loadingType.equals("slides")) {
            tv.setText("Retrieving Slides, Please Wait...");
        }
        else if(loadingType.equals("connect")){
            tv.setText("Attempting To Connect...");
        }
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }

    public void jumpToSlideDialog()
    {
        if(pptNumberOfSlides!=0) {
            Log.i(TAG, "NUMBER OF SLIDES: " + pptNumberOfSlides);
            final ArrayList <String> slideNumber= new ArrayList<String>();
            for(int i = 1; i<=pptNumberOfSlides; i++){
                if(receivedPPTTitle.get(i-1).equals("")) {
                    if(i<10){
                        slideNumber.add("0"+String.valueOf(i) + " - Slide 0" + String.valueOf(i));
                    }
                    else{
                        slideNumber.add(String.valueOf(i) + " - Slide " + String.valueOf(i));
                    }
                }
                else{
                    if(i<10){
                        slideNumber.add("0"+String.valueOf(i) + " - " + receivedPPTTitle.get(i - 1).toString());
                    }
                    else {
                        slideNumber.add(String.valueOf(i) + " - " + receivedPPTTitle.get(i - 1).toString());
                    }
                }
            }
            final Dialog d = new Dialog(ConnectivityPage.this);
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.jump_to_slide);
            final Spinner jumpToSlideInput = (Spinner) d.findViewById(R.id.jumpToSlideInput);
            Button goToSlide = (Button) d.findViewById(R.id.goToSlide);
            Button cancelJump = (Button) d.findViewById(R.id.cancelJump);
            ArrayAdapter<String> adapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item ,slideNumber);
            jumpToSlideInput.setAdapter(adapter);
            jumpToSlideInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                    selectedSlideNumber = position + 1;
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO Auto-generated method stub
                }
            });
            goToSlide.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedSlideNumber!=0){
                        sendMessage("goto " + selectedSlideNumber);
                    }
                    d.dismiss();
                    selectedSlideNumber=0;
                }
            });
            cancelJump.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    // dismiss the dialog
                    d.dismiss();
                    selectedSlideNumber = 0;
                }
            });
            d.show();
        }
        else{
            sendMessage("PPTjump");
        }
    }

    public void openMPPlaylist(){sendMessage("pickMPL");}

    public void openPPTFile(){
        sendMessage("pickPPT");
    }

    @Override
    public void onBackPressed() {
        //Toast.makeText(getApplicationContext(),"Exiting...", Toast.LENGTH_SHORT).show();
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Application")
                .setMessage("Are you sure you want to close this application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessage("Disconn");
                        mChatService.stop();
                        mBluetoothAdapter.disable();
                        System.exit(0);
                    }

                })
                .setNegativeButton("No", null)
                .show();
        //super.onBackPressed();
    }
}