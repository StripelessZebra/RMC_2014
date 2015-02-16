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
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public class RMCHomePage extends Activity {

    private static final String TAG = RMCHomePage.class.toString();
    private BluetoothAdapter BA;
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

        // Register for broadcasts on BluetoothAdapter state change
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        if (!BA.isEnabled()) {
            //Turn on bluetooth
            BluetoothAdapter.getDefaultAdapter().enable();
        }
        if (BA.isEnabled()) {
            new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {

                }
                public void onFinish() {
                    Intent intent = new Intent(RMCHomePage.this,ConnectivityPage.class);
                    startActivity(intent);
                    RMCHomePage.this.finish();
                }
            }.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            //user rejected to turn on bluetooth/search
            Log.i(TAG, Integer.toString(resultCode));
            finish();
        } else if(resultCode == -1){
            //user accepted to turn on bluetooth
            Log.i(TAG, Integer.toString(resultCode));
            if (BA.isEnabled()) {
                new CountDownTimer(2000, 1000) {
                    public void onTick(long millisUntilFinished) {

                    }
                    public void onFinish() {
                        Intent intent = new Intent(RMCHomePage.this,ConnectivityPage.class);
                        startActivity(intent);
                        RMCHomePage.this.finish();
                    }
                }.start();
            }
        }
        else if(resultCode == 120){
            Log.i(TAG, Integer.toString(resultCode));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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
                        Intent mainPage = new Intent(RMCHomePage.this,ConnectivityPage.class);
                        startActivity(mainPage);
                        RMCHomePage.this.finish();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
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
