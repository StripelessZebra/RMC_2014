package com.example.RMC;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public class RMCHomePage extends Activity {

    private static final String TAG = RMCHomePage.class.toString();

    private BluetoothAdapter BA;
    Button connectBtn, discoverBtn, exitBtn;
    TextView status;

    Button btn2;

    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_home_page);
        BA = BluetoothAdapter.getDefaultAdapter();
        spinner = (ProgressBar)findViewById(R.id.progress_bar);
        spinner.setVisibility(View.VISIBLE);
        //connectBtn = (Button) findViewById(R.id.connectBtn);
        //discoverBtn = (Button) findViewById(R.id.discoverBtn);
        //discoverBtn.setVisibility(View.GONE);
        //exitBtn = (Button) findViewById(R.id.exitBtn);
        //status = (TextView) findViewById(R.id.status);

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        if (BA.isEnabled()) {
            //discoverBtn.setVisibility(View.VISIBLE);
            //connectBtn.setVisibility(View.GONE);
            //status.setText("Welcome: " + BA.getName());
        }

        if (!BA.isEnabled()) {
            //Turn on bluetooth with user's permission
            /*Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);*/
            BluetoothAdapter.getDefaultAdapter().enable();

            /*new CountDownTimer(2000, 1000) {

                public void onTick(long millisUntilFinished) {
                    //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    //mTextField.setText("done!");
                    Intent intent = new Intent(
                            RMCHomePage.this,
                            BluetoothChat.class);
                    startActivity(intent);
                    RMCHomePage.this.finish();
                }
            }.start();*/
        }
        if (BA.isEnabled()) {
            //discoverBtn.setVisibility(View.VISIBLE);

            //discoverBtn.setVisibility(View.VISIBLE);
            //connectBtn.setVisibility(View.GONE);
            //status.setText("Welcome: " + BA.getName());
            new CountDownTimer(2000, 1000) {

                public void onTick(long millisUntilFinished) {
                    //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    //mTextField.setText("done!");
                    Intent intent = new Intent(
                            RMCHomePage.this,
                            BluetoothChat.class);
                    startActivity(intent);
                    RMCHomePage.this.finish();
                }
            }.start();


            //connectBtn.setVisibility(View.GONE);
            //status.setText("Welcome: " + BA.getName());
        }

        /*connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/


        /*discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///if (!BA.isDiscovering()) {
                ///    startActivityForResult(new Intent(
                 ///           BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), 0);
                    //BA.startDiscovery();
                ///    registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                ///}
                //Intent intent = new Intent(
                //        RMCHomePage.this,
                //        BluetoothChat.class);
                //startActivity(intent);
            }
        });*/

        /*exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BA.disable();
                System.exit(0);
            }
        });*/
        //btn2 = (Button) findViewById(R.id.ctp);



        //CTP Btn

	/*btn2.setOnClickListener(new OnClickListener(){

		public void onClick(View arg0) {

			Intent intent = new Intent(
						GamePlay.this,
						BluetoothChat.class); //
						startActivity(intent);


		}
	});*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            //user rejected to turn on bluetooth/search
            Log.i(TAG, Integer.toString(resultCode));
            finish();
            //Intent programSelection = new Intent(RMCHomePage.this, ProgramSelection.class);
            //unregisterReceiver(mReceiver);
            //RMCHomePage.this.finish();
            //startActivity(programSelection);
        } else if(resultCode == -1){
            //user accepted to turn on bluetooth
            Log.i(TAG, Integer.toString(resultCode));
            if (BA.isEnabled()) {
                //discoverBtn.setVisibility(View.VISIBLE);
                new CountDownTimer(2000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        //mTextField.setText("done!");
                        Intent intent = new Intent(
                                RMCHomePage.this,
                                BluetoothChat.class);
                        startActivity(intent);
                        RMCHomePage.this.finish();
                    }
                }.start();
                //connectBtn.setVisibility(View.GONE);
                //status.setText("Welcome: " + BA.getName());
            }
        }
        else if(resultCode == 120){
            Log.i(TAG, Integer.toString(resultCode));

            //pairedDevices = BA.getBondedDevices();

            //ArrayList list = new ArrayList();
            //for(BluetoothDevice bt : pairedDevices) {
            //     list.add(bt.getName());
            //    Log.i(TAG, bt.getName());
            //    Log.i(TAG, bt.getAddress());
            // }
            //opens phone's bluetooth paired/scan list
            //startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));

            //unregisterReceiver(bReceiver);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                System.out.println(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:

                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "22222222222222222222222222222222222222");
                        Intent mainPage = new Intent(
                                RMCHomePage.this,
                                BluetoothChat.class);
                        startActivity(mainPage);
                        RMCHomePage.this.finish();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "HEREEEEEEEEEEEEEEEEEEEEE");
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
