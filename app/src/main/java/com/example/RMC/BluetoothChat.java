package com.example.RMC;

import android.annotation.TargetApi;
import android.app.ActionBar;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity implements SensorEventListener, NumberPicker.OnValueChangeListener {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    /**Imports for sensors**/
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    TextView tv, defaultTV, deviceDetails, upGestureTv, downGestureTv, leftGestureTv, rightGestureTv, cursorSpeedTv;
    Button eraseAnnotationsBtn, leftMouseBtn, rightMouseBtn, mediaPlayIncrease , mediaPlayDecrease, jumpToSlideBtn,blankScreenBtn;
    LinearLayout programSelectionLL, toggleButtonLL, latestDeviceInfo, highlightToggleLL, mouseButtonLL;
    RelativeLayout ppt,mediaPlay;
    Spinner programSelectionSpinner, pptToolsSpinner;
    CustomProgramList spinnerAdapter, pptSpinnerAdapter;
    Switch toggle;
    ToggleButton highlightToggle, laserPointerToggle;
    ImageView pptMinimize, pptMaximize, pptLeft, pptRight, mediaPlayMinimize, mediaPlayMaximize, mediaPlayMute, mediaPlayUnmute, mediaPlayPlay, mediaPlayStop, mediaPlayPause ,mediaPlayNext, mediaPlayPrev;
    SharedPreferences.Editor editor;
    String pairedDeviceAddress,hasOnCreateOptionsMenuBeenCreated,isDeviceConnected = "", isMotionControlSelected="";
    Menu settingsMenu;
    MenuItem connectBT, disconnectBT, settingOption, userManual, receivedPPTSlides, openPPT, openMP;
    private SeekBar upSeek = null;
    private SeekBar downSeek = null;
    private SeekBar leftSeek=null;
    private SeekBar rightSeek = null;
    private SeekBar cursorSeek = null;
    Dialog loadingDialog;
    int upValue;
    int downValue;
    int leftValue;
    int rightValue;
    int cursorValue;
    int currentSlideNumber_Menu = 1;
    boolean wasUpGestureValueChanged = false;
    boolean wasDownGestureValueChanged = false;
    boolean wasLeftGestureValueChanged = false;
    boolean wasRightGestureValueChanged = false;
    boolean wasCursorSpeedValueChanged = false;
    boolean isLaserPointerOn = false;
    boolean isHighlighterOn = false;
    boolean isOpenMPSelected = false;

    private RadioGroup pptToolsRG;
    private RadioButton pptCursor;
    private RadioButton pptHighlighter;
    private RadioButton pptLaserPointer;
    private RadioButton defaultRadioButton;

    ArrayList<String> receivedPPTText = new ArrayList<String>();
    ArrayList<String> receivedPPTTitle = new ArrayList<String>();
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

    String[] pptTools = {"Cursor", "Highlighter", "Laser Pointer"};
    Integer[] pptToolsImage = {R.drawable.cursor, R.drawable.highlighter, R.drawable.laser};

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
                //deviceDetails = (TextView) findViewById(R.id.deviceDetails);
                //deviceDetails.setText("Device Name: " + deviceName + "\nMAC Address: " + deviceAddress);
                //latestDeviceInfo = (LinearLayout)findViewById(R.id.latestDeviceInfo);
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

        ImageButton button1;
        ImageButton button2;
        ImageButton button3;
        ImageButton button4;

        tv = (TextView) findViewById(R.id.textView1);
        defaultTV=(TextView) findViewById(R.id.defaultTextTV);

        /*CustomProgramList adapter = new CustomProgramList(BluetoothChat.this, web, imageId);
        ListView programListView = (ListView) findViewById(R.id.programLV);
        programListView .setAdapter(adapter);
        programListView .setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(BluetoothChat.this, "Selected: " + web[position], Toast.LENGTH_SHORT).show();
                if(web[position]=="Microsoft PowerPoint"){
                    Intent ppt = new Intent(BluetoothChat.this, PowerPointControls.class);
                    startActivity(ppt);
                }
            }
        });*/

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
                        if(receivedPPTText.size()!=0){
                            receivedPPTSlides.setVisible(true);
                        }
                        else if(receivedPPTText.size()==0){
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
                        if(receivedPPTText.size()!=0){
                            receivedPPTSlides.setVisible(true);
                        }
                        else if(receivedPPTText.size()==0){
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

        /*pptToolsSpinner = (Spinner) findViewById(R.id.pptToolsSpinner);
        pptSpinnerAdapter = new CustomProgramList(this, pptTools, pptToolsImage);
        pptSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pptToolsSpinner.setAdapter(pptSpinnerAdapter);

        pptToolsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (pptToolsSpinner.getSelectedItem().toString().equals("Cursor")) {
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
                }
                else if (pptToolsSpinner.getSelectedItem().toString().equals("Highlighter")) {
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
                }
                else if (pptToolsSpinner.getSelectedItem().toString().equals("Laser Pointer")) {
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
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/

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


        //mediaPlayMinimize = (ImageView) findViewById(R.id.mediaPlayMinimize);
        mediaPlayMaximize = (ImageView) findViewById(R.id.mediaPlayMaximize);
        mediaPlayMute = (ImageView) findViewById(R.id.mediaPlayMute);
        //mediaPlayUnmute = (ImageView) findViewById(R.id.mediaPlayUnmute);
        mediaPlayPlay = (ImageView) findViewById(R.id.mediaPlayPlay);
        mediaPlayStop = (ImageView) findViewById(R.id.mediaPlayStop);
        //mediaPlayPause = (ImageView) findViewById(R.id.mediaPlayPause);
        mediaPlayNext = (ImageView) findViewById(R.id.mediaPlayNext);
        mediaPlayPrev = (ImageView) findViewById(R.id.mediaPlayPrev);
        mediaPlayIncrease = (Button) findViewById(R.id.mediaPlayIncrease);
        mediaPlayDecrease = (Button) findViewById(R.id.mediaPlayDecrease);

        /*mediaPlayMinimize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp min";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });*/

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

        /*mediaPlayUnmute.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp unmt";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });*/

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

        /*mediaPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="mp paus";
                sendMessage(message);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
            }
        });*/

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
                    //eraseAnnotationsBtn.setVisibility(View.VISIBLE);
                    pptToolsRG.check(R.id.pptCursor);
                    sendMessage("show cr");
                    //pptToolsSpinner.setSelection(0);
                } else {
                    // The toggle is disabled

                    pptCursor.setChecked(true);
                    pptHighlighter.setChecked(false);
                    pptLaserPointer.setChecked(false);

                    highlightToggleLL.setVisibility(View.GONE);
                    //highlightToggle.setChecked(false);
                    //laserPointerToggle.setChecked(false);
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
        String cursorSeekValue = "0" + String.valueOf(prefs.getInt("cursorSeekValue", 3));

        if(isMotionControlSelected=="YES") {

            pptToolsRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    defaultRadioButton = (RadioButton) findViewById(checkedId);

                    //Toast.makeText(BluetoothChat.this,pptCursor.getText(), Toast.LENGTH_SHORT).show();
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

                /*highlightToggle = (ToggleButton) findViewById(R.id.highlightToggleButton);
                laserPointerToggle = (ToggleButton) findViewById(R.id.laserPointerToggleButton);

                highlightToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            // The toggle is enabled
                            String message = "HL on";
                            sendMessage(message);
                            laserPointerToggle.setChecked(false);
                            laserPointerToggle.setVisibility(View.GONE);
                            mouseButtonLL.setVisibility(View.GONE);
                        } else {
                            String message = "HL off";
                            sendMessage(message);
                            laserPointerToggle.setVisibility(View.VISIBLE);
                            mouseButtonLL.setVisibility(View.VISIBLE);
                        }
                    }
                });

                laserPointerToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            // The toggle is enabled
                            String message = "LP on";
                            sendMessage(message);
                            highlightToggle.setChecked(false);
                            highlightToggle.setVisibility(View.GONE);
                            eraseAnnotationsBtn.setVisibility(View.GONE);
                            mouseButtonLL.setVisibility(View.GONE);
                        } else {
                            String message = "LP off";
                            sendMessage(message);
                            highlightToggle.setVisibility(View.VISIBLE);
                            eraseAnnotationsBtn.setVisibility(View.VISIBLE);
                            mouseButtonLL.setVisibility(View.VISIBLE);
                        }
                    }
                });*/
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
                /*//Left and Up
                if ((Math.round(x) >= leftGestureValueSensitivity) && (Math.round(y) >= upGestureValueSensitivity)) {
                    Log.i("ACCELEROMETER X: ", "LEFTUP " + String.valueOf(x));
                    String message = "aLU  " + cursorSeekValue;
                    sendMessage(message);
                }
                //Right and Up
                if (Math.round(x) <= Integer.parseInt(rightGestureValueSensitivityConverted) && (Math.round(y) >= upGestureValueSensitivity)) {
                    Log.i("ACCELEROMETER X: ", "RIGHTUP " + String.valueOf(x));
                    String message = "aRU  " + cursorSeekValue;
                    sendMessage(message);
                }
                //Left and Down
                if ((Math.round(x) <= Integer.parseInt(rightGestureValueSensitivityConverted)) && (Math.round(y) <= Integer.parseInt(downGestureValueSensitivityConverted))) {
                    Log.i("ACCELEROMETER X: ", "LEFTDOWN " + String.valueOf(x));
                    String message = "aLD  " + cursorSeekValue;
                    sendMessage(message);
                }
                //Right and Down
                if ((Math.round(x) <= Integer.parseInt(rightGestureValueSensitivityConverted)) && (Math.round(y) <= Integer.parseInt(downGestureValueSensitivityConverted))) {
                    Log.i("ACCELEROMETER X: ", "RIGHTDOWN " + String.valueOf(x));
                    String message = "aRD  " + cursorSeekValue;
                    sendMessage(message);
                }*/
            }
    }

    private AdapterView.OnItemClickListener programClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        }
    };

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
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
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
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
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

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            //Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
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

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        //actionBar.setSubtitle(resId);
        tv.setText(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        //actionBar.setSubtitle(subTitle);
        tv.setText(subTitle);
    }

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
        //settingOption = settingsMenu.findItem(R.id.calibrationSettings);
        //settingOption.setVisible(false);
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
                //BluetoothAdapter.getDefaultAdapter().disable();
                //BluetoothAdapter.getDefaultAdapter().enable();
                return true;
            case R.id.userManual:
                Intent userManual = new Intent(BluetoothChat.this, UserManual.class);
                startActivity(userManual);
                return true;

            /*case R.id.calibrationSettings:
                displayDialog();
                return true;*/

            case R.id.openMP:
                openMPPlaylist();
                return true;

            case R.id.openPPT:
                openPPTOnceConnected();
                return true;

            case R.id.receivedPPTSlides:
                displayPPTSlides();
                return true;
        /*case R.id.insecure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            return true;*/
            //case R.id.discoverable:

            //    return true;
        }
        return false;
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            tv.setText("");
                            tv.setBackground(getResources().getDrawable(R.drawable.connected));
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(400);
                            //Toast.makeText(getApplicationContext(),"Pairing successful", Toast.LENGTH_SHORT).show();
                            //mConversationArrayAdapter.clear();
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
                            //defaultTV.setVisibility(View.GONE);
                            defaultTV.setText("Connected to: " + mConnectedDeviceName + ".");
                            if(hasOnCreateOptionsMenuBeenCreated =="YES" && isDeviceConnected=="YES") {
                                connectBT.setVisible(false);
                                disconnectBT.setVisible(true);
                                //settingOption.setVisible(true);
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
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            tv.setText("");
                            tv.setBackground(getResources().getDrawable(R.drawable.connecting));
                            defaultTV.setText(R.string.title_connecting);
                            if(hasOnCreateOptionsMenuBeenCreated =="YES") {
                                connectBT.setVisible(false);
                                disconnectBT.setVisible(false);
                                userManual.setVisible(false);
                            }
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            //setStatus(R.string.title_not_connected);
                            tv.setText("");
                            tv.setBackground(getResources().getDrawable(R.drawable.disconnected));
                            receivedPPTText.clear();
                            receivedPPTTitle.clear();
                            pptFilePathFromPC="";
                            pptNumberOfSlides=0;
                            defaultTV.setText(R.string.not_connected);
                            //defaultTV.setVisibility(View.VISIBLE);
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

                            if(hasOnCreateOptionsMenuBeenCreated =="YES" && isDeviceConnected =="") {
                                connectBT.setVisible(true);
                                disconnectBT.setVisible(false);
                                //settingOption.setVisible(false);
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
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
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
                                receivedPPTText.add(removeWords(readMessage, "contains"));
                                Log.i("RECEIVED: ", "RECEIVED: " + readMessage);
                            }
                            else if(readMessage.contains("slideTitle")){
                                receivedPPTTitle.add(removeWords(readMessage, "slideTitle"));
                                Log.i("TITLE: ", "TITLE: " + readMessage);
                            }
                            else if(readMessage.equals("startOfRetrieving")){
                                displayLoadingDialog();
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
                    //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    //Toast.makeText(getApplicationContext(), "Connected to "
                    //        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_TOAST:
                    //Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                    //        Toast.LENGTH_SHORT).show();
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
            final Dialog d = new Dialog(BluetoothChat.this);
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.slides_layout);
            d.setCanceledOnTouchOutside(false);
            final TextView pptSlideTitle = (TextView) d.findViewById(R.id.pptSlideTitle);
            final TextView slideNumberTv = (TextView) d.findViewById(R.id.slideNumberTv);
            slideNumberTv.setText("Slide Number: " + String.valueOf(currentSlideNumber_Menu));
            SeekBar slideSeek = (SeekBar) d.findViewById(R.id.slideSeek);
            slideSeek.setMax(pptNumberOfSlides - 1);
            slideSeek.setProgress(currentSlideNumber_Menu-1);
            final TextView pptSlideText = (TextView) d.findViewById(R.id.pptSlideText);
            pptSlideText.setMovementMethod(new ScrollingMovementMethod());
            Button goToSlideBtn = (Button) d.findViewById(R.id.goToSlideBtn);
            Button closeSlideBtn = (Button) d.findViewById(R.id.closeSlideBtn);
            if(receivedPPTText.get(currentSlideNumber_Menu - 1).toString().equals("")) {
                pptSlideText.setText("*No Notes Found.");
            }
            else {
               pptSlideText.setText(receivedPPTText.get(currentSlideNumber_Menu - 1).toString());
            }

            /*String fileNameWithPPTExtension = pptFilePathFromPC.substring(pptFilePathFromPC.lastIndexOf("\\") + 1);
            if(fileNameWithPPTExtension.length()>31){
                String shorterFileNameWithPPTExtension = fileNameWithPPTExtension.substring(0, Math.min(fileNameWithPPTExtension.length(), 30)) + "...";
                pptSlideTitle.setText(shorterFileNameWithPPTExtension);
            }
            else if(fileNameWithPPTExtension.length()<=31){
                pptSlideTitle.setText(fileNameWithPPTExtension);
            }*/
            if(receivedPPTTitle.get(currentSlideNumber_Menu - 1).toString().equals("")) {
                pptSlideTitle.setText("*No Title Found.");
            }
            else{
                pptSlideTitle.setText(receivedPPTTitle.get(currentSlideNumber_Menu - 1).toString());
            }

            slideSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                    currentSlideNumber_Menu = progress + 1;
                    slideNumberTv.setText("Slide Number: " + String.valueOf(currentSlideNumber_Menu));
                    wasUpGestureValueChanged = true;
                    if(receivedPPTText.get(progress).toString().equals("")) {
                        pptSlideText.setText("*No Notes Found.");
                    }
                    else {
                        pptSlideText.setText(receivedPPTText.get(progress).toString());
                    }
                    if(receivedPPTTitle.get(progress).toString().equals("")) {
                        pptSlideTitle.setText("*No Title Found.");
                    }
                    else{
                        pptSlideTitle.setText(receivedPPTTitle.get(progress).toString());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
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
            //sendMessage("PPTmenu");
        }
    }

    public void displayLoadingDialog(){
        loadingDialog = new Dialog(BluetoothChat.this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_layout);
        ProgressBar pb = (ProgressBar)loadingDialog.findViewById(R.id.progress_bar);
        pb.setVisibility(View.VISIBLE);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }

    public void displayDialog()
    {

        final Dialog d = new Dialog(BluetoothChat.this);
        //d.setTitle("Motion Control Calibration");
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.settings);
        Button saveSettings = (Button) d.findViewById(R.id.saveSettings);
        Button cancelSettings = (Button) d.findViewById(R.id.cancelSettings);
        SharedPreferences prefs = getSharedPreferences("RMCSP", MODE_PRIVATE);
        upSeek = (SeekBar) d.findViewById(R.id.upSeek);
        downSeek = (SeekBar) d.findViewById(R.id.downSeek);
        leftSeek = (SeekBar) d.findViewById(R.id.leftSeek);
        rightSeek = (SeekBar) d.findViewById(R.id.rightSeek);
        cursorSeek = (SeekBar) d.findViewById(R.id.cursorSeek);
        upGestureTv = (TextView) d.findViewById(R.id.upGestureTV);
        downGestureTv = (TextView) d.findViewById(R.id.downGestureTV);
        leftGestureTv = (TextView) d.findViewById(R.id.leftGestureTV);
        rightGestureTv = (TextView) d.findViewById(R.id.rightGestureTV);
        cursorSpeedTv = (TextView) d.findViewById(R.id.cursorSpeedTV);

        upGestureTv.setText("Upward Gesture Sensitivity: " + String.valueOf(prefs.getInt("upGestureValue", 2)));
        int upSeekValue = prefs.getInt("upGestureValue", 2)-1;
        upSeek.setProgress(upSeekValue);
        downGestureTv.setText("Downward Gesture Sensitivity: " + String.valueOf(prefs.getInt("downGestureValue", 2)));
        int downSeekValue = prefs.getInt("downGestureValue", 2)-1;
        downSeek.setProgress(downSeekValue);
        leftGestureTv.setText("Left Gesture Sensitivity: " + String.valueOf(prefs.getInt("leftGestureValue", 2)));
        int leftSeekValue = prefs.getInt("leftGestureValue", 2)-1;
        leftSeek.setProgress(leftSeekValue);
        rightGestureTv.setText("Right Gesture Sensitivity: " + String.valueOf(prefs.getInt("rightGestureValue", 2)));
        int rightSeekValue = prefs.getInt("rightGestureValue", 2)-1;
        rightSeek.setProgress(rightSeekValue);
        cursorSpeedTv.setText("Speed Of Cursor: " + String.valueOf(prefs.getInt("cursorSeekValue", 3)));
        int cursorSpeedValue = prefs.getInt("cursorSeekValue", 3)-1;
        cursorSeek.setProgress(cursorSpeedValue);

        upSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                upValue = progress + 1;
                upGestureTv.setText("Upward Gesture Sensitivity: " + String.valueOf(upValue));
                wasUpGestureValueChanged = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        downSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                downValue = progress + 1;
                downGestureTv.setText("Downward Gesture Sensitivity: " + String.valueOf(downValue));
                wasDownGestureValueChanged = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        leftSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                leftValue = progress + 1;
                leftGestureTv.setText("Left Gesture Sensitivity: " + String.valueOf(leftValue));
                wasLeftGestureValueChanged = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        rightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                rightValue = progress + 1;
                rightGestureTv.setText("Right Gesture Sensitivity: " + String.valueOf(rightValue));
                wasRightGestureValueChanged = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        cursorSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                cursorValue = progress + 1;
                cursorSpeedTv.setText("Speed Of Cursor: " + String.valueOf(cursorValue));
                wasCursorSpeedValueChanged = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /*final NumberPicker leftNP = (NumberPicker) d.findViewById(R.id.leftNumberPicker);
        leftNP.setMaxValue(10);
        leftNP.setMinValue(1);
        leftNP.setValue(prefs.getInt("leftGestureValue", 9));
        leftNP.setWrapSelectorWheel(false);
        leftNP.setOnValueChangedListener(this);
        final NumberPicker rightNP = (NumberPicker) d.findViewById(R.id.rightNumberPicker);
        rightNP.setMaxValue(10);
        rightNP.setMinValue(1);
        rightNP.setValue(prefs.getInt("rightGestureValue", 9));
        rightNP.setWrapSelectorWheel(false);
        rightNP.setOnValueChangedListener(this);*/

        saveSettings.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // dismiss the dialog
                if(wasUpGestureValueChanged==true){
                    editor.putInt("upGestureValue", upValue);
                }
                if(wasDownGestureValueChanged==true){
                    editor.putInt("downGestureValue", downValue);
                }
                if(wasLeftGestureValueChanged==true) {
                    editor.putInt("leftGestureValue", leftValue);
                }
                if(wasRightGestureValueChanged==true) {
                    editor.putInt("rightGestureValue", rightValue);
                }
                if(wasCursorSpeedValueChanged==true){
                    editor.putInt("cursorSeekValue", cursorValue);
                }
                wasUpGestureValueChanged = false;
                wasDownGestureValueChanged = false;
                wasRightGestureValueChanged = false;
                wasLeftGestureValueChanged = false;
                wasCursorSpeedValueChanged = false;
                editor.commit();
                d.dismiss();
            }
        });
        cancelSettings.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // dismiss the dialog
                wasUpGestureValueChanged = false;
                wasDownGestureValueChanged = false;
                wasRightGestureValueChanged = false;
                wasLeftGestureValueChanged = false;
                wasCursorSpeedValueChanged = false;
                d.dismiss();
            }
        });
        d.show();


    }

    public void jumpToSlideDialog()
    {

        if(pptNumberOfSlides!=0) {
            Log.i("MENU PRESSED", "NUMBER OF SLIDES: " + pptNumberOfSlides);

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

            final Dialog d = new Dialog(BluetoothChat.this);
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.jump_to_slide);
            //final EditText jumpToSlideInput = (EditText) d.findViewById(R.id.jumpToSlideInput);
            final Spinner jumpToSlideInput = (Spinner) d.findViewById(R.id.jumpToSlideInput);
            Button goToSlide = (Button) d.findViewById(R.id.goToSlide);
            Button cancelJump = (Button) d.findViewById(R.id.cancelJump);

            ArrayAdapter<String> adapter= new ArrayAdapter<String>(this,android.
                    R.layout.simple_spinner_dropdown_item ,slideNumber);
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


            /*jumpToSlideInput.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(jumpToSlideInput.getText().toString().equals("0")){
                        //jumpToSlideInput.setText("");
                    }
                }
            });*/

            goToSlide.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedSlideNumber!=0){
                        sendMessage("goto " + selectedSlideNumber);
                    }
                    d.dismiss();
                    selectedSlideNumber=0;

                    // dismiss the dialog
                    /*if(jumpToSlideInput.getText().toString().equals("")||jumpToSlideInput.getText().toString().equals("0")||jumpToSlideInput.getText().toString().equals("00")){
                        jumpToSlideInput.setText("");
                    }
                    else {
                        String slideNum = jumpToSlideInput.getText().toString();
                        if(slideNum.length()<2){
                            slideNum = "0"+slideNum;
                            //Toast.makeText(getApplicationContext(),slideNum,Toast.LENGTH_SHORT).show();
                            sendMessage("goto " + slideNum);
                        }
                        else if(slideNum.length()==2){
                            sendMessage("goto " + slideNum);
                            //Toast.makeText(getApplicationContext(),slideNum,Toast.LENGTH_SHORT).show();
                        }
                        d.dismiss();
                    }*/
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

    public void openPPTOnceConnected(){
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
                        //finish();0
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