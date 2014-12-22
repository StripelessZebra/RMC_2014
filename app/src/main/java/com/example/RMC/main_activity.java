package com.example.RMC;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class main_activity extends Activity{
	
	Button button1;
	Button button2;
	Button button3;
	Button button4;
	
	public void onCreate(Bundle savedInstanceState )
	{
		
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_main);
		
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.ctp);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		
		//Button 1 click listener(Game)
		
		button1.setOnClickListener(new OnClickListener() {
			
		
		public void onClick(View arg0) {
			
			Intent intent = new Intent(
					main_activity.this,
					RMCHomePage.class); //
					startActivity(intent);
					
		}
	
		});
	
	
	//Button 2 click listener(How To Play)
		
		button2.setOnClickListener(new OnClickListener() {
			
		
		public void onClick(View arg0) {
			
			Intent intent = new Intent(
					main_activity.this,
					HowToPlay.class); //
					startActivity(intent);
					
		}
	
		});

   //Button 3 click listener(Terms and Conditions)
		
		button3.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				Intent intent = new Intent(
						main_activity.this,
						TermsAndConditions.class);
						startActivity(intent);
			}
			
			});
		

// Button 4 click listener(Turn On Bluetooth)
		
		button4.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				Intent intent = new Intent(
						main_activity.this,
						BluetoothSettings.class);
						startActivity(intent);
			}
			
		});
		

	}
}
	
	
	
	
	
	
	
		
	