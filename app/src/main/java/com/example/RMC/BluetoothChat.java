/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.RMC;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

import javax.crypto.Mac;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
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

    TextView tv, defaultTV, deviceDetails;
    LinearLayout programSelectionLL,latestDeviceInfo;
    RelativeLayout ppt,mediaPlay;
    Spinner programSelectionSpinner;
    CustomProgramList spinnerAdapter;
    ImageView pptMinimize, pptMaximize, pptLeft, pptRight, mediaPlayMinimize, mediaPlayMaximize, mediaPlayMute, mediaPlayUnmute, mediaPlayPlay, mediaPlayStop, mediaPlayPause;
    SharedPreferences.Editor editor;
    String pairedDeviceAddress;

    String[] web = {
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

        editor = getSharedPreferences("RMCSP", MODE_PRIVATE).edit();

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


        programSelectionLL = (LinearLayout) findViewById(R.id.programSelectionLL);
        programSelectionLL.setVisibility(View.GONE);

        ppt = (RelativeLayout) findViewById(R.id.ppt);
        ppt.setVisibility(View.GONE);

        mediaPlay = (RelativeLayout) findViewById(R.id.mediaPlay);
        mediaPlay.setVisibility(View.GONE);

        programSelectionSpinner= (Spinner) findViewById(R.id.programSelectionSpinner);
        spinnerAdapter = new CustomProgramList(this, web, imageId);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        programSelectionSpinner.setAdapter(spinnerAdapter);

        programSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(),programSelectionSpinner.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
                if(programSelectionSpinner.getSelectedItem().toString() == "Microsoft PowerPoint") {
                    ppt.setVisibility(View.VISIBLE);
                    mediaPlay.setVisibility(View.GONE);
                }
                else if(programSelectionSpinner.getSelectedItem().toString() == "Windows Media Player") {
                    mediaPlay.setVisibility(View.VISIBLE);
                    ppt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        pptMinimize = (ImageView) findViewById(R.id.pptMinimize);
        pptMaximize = (ImageView) findViewById(R.id.pptMaximize);
        pptLeft = (ImageView) findViewById(R.id.pptLeft);
        pptRight = (ImageView) findViewById(R.id.pptRight);

        pptMinimize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="PowerPoint Minimize";
                sendMessage(message);
            }
        });

        pptMaximize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="PowerPoint Maximize";
                sendMessage(message);
            }
        });

        pptLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="PowerPoint Previous Slide";
                sendMessage(message);
            }
        });

        pptRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="PowerPoint Next Slide";
                sendMessage(message);
            }
        });


        mediaPlayMinimize = (ImageView) findViewById(R.id.mediaPlayMinimize);
        mediaPlayMaximize = (ImageView) findViewById(R.id.mediaPlayMaximize);
        mediaPlayMute = (ImageView) findViewById(R.id.mediaPlayMute);
        mediaPlayUnmute = (ImageView) findViewById(R.id.mediaPlayUnmute);
        mediaPlayPlay = (ImageView) findViewById(R.id.mediaPlayPlay);
        mediaPlayStop = (ImageView) findViewById(R.id.mediaPlayStop);
        mediaPlayPause = (ImageView) findViewById(R.id.mediaPlayPause);

        mediaPlayMinimize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="Media Player Minimize";
                sendMessage(message);
            }
        });

        mediaPlayMaximize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="Media Player Maximize";
                sendMessage(message);
            }
        });

        mediaPlayMute.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="Media Player Mute";
                sendMessage(message);
            }
        });

        mediaPlayUnmute.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="Media Player Unmute";
                sendMessage(message);
            }
        });

        mediaPlayPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="Media Player Play";
                sendMessage(message);
            }
        });
        mediaPlayStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="Media Player Stop";
                sendMessage(message);
            }
        });

        mediaPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String message ="Media Player Pause";
                sendMessage(message);
            }
        });



        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("RMCSP", MODE_PRIVATE);
        String deviceName = prefs.getString("deviceName", null);
        String deviceAddress = prefs.getString("deviceAddress", null);

        if(deviceName==null && deviceAddress ==null){
            //Toast.makeText(getApplicationContext(),"ABC", Toast.LENGTH_SHORT).show();
        }
        else if(deviceName!=null && deviceAddress !=null){
            //deviceDetails = (TextView) findViewById(R.id.deviceDetails);
            //deviceDetails.setText("Device Name: " + deviceName + "\nMAC Address: " + deviceAddress);
            //latestDeviceInfo = (LinearLayout)findViewById(R.id.latestDeviceInfo);
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
            if (mChatService == null) setupChat();
            mChatService.connect(device, true);

            Toast.makeText(getApplicationContext(),deviceName, Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(),deviceAddress, Toast.LENGTH_SHORT).show();
        }

    	/*button1 = (ImageButton) findViewById(R.id.imageButton1);
		button2 = (ImageButton) findViewById(R.id.imageButton2);
		button3 = (ImageButton) findViewById(R.id.imageButton3);
		button4 = (ImageButton) findViewById(R.id.imageButton4);*/

        //ImageButton 1

        //button1.setOnTouchListener(new OnTouchListener() {

        //public boolean onTouch(View v, MotionEvent event){
        //	switch(event.getAction()) {
        //	case MotionEvent.ACTION_DOWN:
        //		String message ="A";
        //		sendMessage(message);
        //	break;

        //		case MotionEvent.ACTION_UP:

        //	break;
        //}
        //	return false;

					/*button1.setOnClickListener(new OnClickListener() {



						public void onClick(View arg0) {

							String message ="A";
							sendMessage(message);

							Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
							// Vibrate for 400 milliseconds
							v.vibrate(400);

			}
			});


				//ImageButton 2

				button2.setOnClickListener(new OnClickListener() {



					public void onClick(View arg0) {

						String message ="D";
						sendMessage(message);

						Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						// Vibrate for 400 milliseconds
						v.vibrate(400);



					}
				});


				//ImageButton 3

				button3.setOnClickListener(new OnClickListener() {

					public void onClick(View arg0) {

					String message = "W";
					sendMessage(message);

					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					// Vibrate for 400 milliseconds
					v.vibrate(400);

					}
				});


				//ImageButton 4

				button4.setOnClickListener(new OnClickListener() {

					public void onClick(View arg0) {

					String message = "S";
					sendMessage(message);

					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					// Vibrate for 400 milliseconds
					v.vibrate(400);

					}
				});
				*/



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
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");


        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);


    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
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
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
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
                            // Vibrate for 400 milliseconds
                            v.vibrate(400);
                            //Toast.makeText(getApplicationContext(),"Pairing successful", Toast.LENGTH_SHORT).show();
                            //mConversationArrayAdapter.clear();
                            if(mConnectedDeviceName!=null && pairedDeviceAddress != null) {
                                editor.putString("deviceName", mConnectedDeviceName);
                                editor.putString("deviceAddress", pairedDeviceAddress);
                                Log.i(TAG, mConnectedDeviceName + " " + pairedDeviceAddress);
                                editor.commit();
                            }

                            programSelectionLL.setVisibility(View.VISIBLE);
                            defaultTV.setVisibility(View.GONE);

                            if (programSelectionSpinner.getSelectedItem().toString().equals("Microsoft PowerPoint")){
                                ppt.setVisibility(View.VISIBLE);
                                mediaPlay.setVisibility(View.GONE);
                            }
                            else if(programSelectionSpinner.getSelectedItem().toString().equals("Windows Media Player")) {
                                ppt.setVisibility(View.GONE);
                                mediaPlay.setVisibility(View.VISIBLE);
                            }
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            tv.setText("");
                            tv.setBackground(getResources().getDrawable(R.drawable.connecting));
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            //setStatus(R.string.title_not_connected);
                            tv.setText("");
                            tv.setBackground(getResources().getDrawable(R.drawable.disconnected));

                            defaultTV.setVisibility(View.VISIBLE);
                            programSelectionLL.setVisibility(View.GONE);
                            ppt.setVisibility(View.GONE);
                            mediaPlay.setVisibility(View.GONE);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    Toast.makeText(getApplicationContext(),writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    Toast.makeText(getApplicationContext(),readMessage,Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
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
        inflater.inflate(R.menu.connectivity_page, menu);
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
                    ensureDiscoverable();
                }
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
                        mChatService.stop();
                        //mBluetoothAdapter.disable();
                        System.exit(0);
                    }

                })
                .setNegativeButton("No", null)
                .show();
        //super.onBackPressed();
    }

}
