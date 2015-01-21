package com.example.RMC;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.util.ArrayList;


public class UserManual extends Activity {

    private ArrayList<String> parentItems = new ArrayList<String>();
    private ArrayList<Object> childItems = new ArrayList<Object>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //edit row.xml and group.xml to change the styling of user manual

        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_manual);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ExpandableListView expandableList = (ExpandableListView)findViewById(R.id.expandableList);
        expandableList.setDividerHeight(2);
        expandableList.setGroupIndicator(null);
        expandableList.setClickable(true);

        setGroupParents();
        setChildData();

        ExpandableAdapter adapter = new ExpandableAdapter(parentItems, childItems);

        adapter.setInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), this);
        expandableList.setAdapter(adapter);

    }

    public void setGroupParents() {
        parentItems.add("Connect A Device");
        parentItems.add("Connection Status");
        parentItems.add("Program Control");
    }

    public void setChildData() {

        //Connect A Device
        ArrayList<String> child = new ArrayList<String>();
        child.add(getString(R.string.connectADevice));
        child.add(getString(R.string.connectADevice2));
        childItems.add(child);


        //Connection Status
        child = new ArrayList<String>();
        child.add(getString(R.string.connectedIcon));
        child.add(getString(R.string.connectingIcon));
        child.add(getString(R.string.disconnectedIcon));
        //child.add(getString(R.string.disconnectBtn));
        childItems.add(child);

        //Program Control
        child = new ArrayList<String>();
        child.add(getString(R.string.programSelection));
        child.add(getString(R.string.programSelection2));
        child.add(getString(R.string.programSelection3));
        child.add(getString(R.string.programSelection4));
        childItems.add(child);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_manual, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == android.R.id.home){
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
