package com.example.RMC;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class TermsAndConditions extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_terms_and_conditions);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.terms_and_conditions, menu);
		return true;
	}

}
