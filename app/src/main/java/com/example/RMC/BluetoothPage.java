package com.example.RMC;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BluetoothPage extends Activity {
	
	Button button1;
	Button button2;
	Button button3;

	public void onCreate(Bundle savedInstanceState )
	{
		
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_bluetooth_settings);
		
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		
		
		//Button 1 Turn On Bluetooth
		
		button1.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				Intent intent = new Intent(
						BluetoothPage.this,
						BluetoothSettings.class); //
						startActivity(intent);
						
			}
		
			});
		
		
		
	//Button 2 click listener(Discoverable)
	
			button2.setOnClickListener(new OnClickListener() {
				
			
			public void onClick(View arg0) {
				
				Intent intent = new Intent(
						BluetoothPage.this,
						BluetoothSettings.class); //
						startActivity(intent);
						
			}
		
			});
		
			
			//Button 3 click listener(Discoverable)
			
			button3.setOnClickListener(new OnClickListener() {
				
			
			public void onClick(View arg0) {
				
				Intent intent = new Intent(
						BluetoothPage.this,
						BluetoothSettings.class); //
						startActivity(intent);
						
			}
		
			});
		
	}
}
