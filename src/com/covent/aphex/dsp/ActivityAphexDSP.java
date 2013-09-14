package com.covent.aphex.dsp;

import com.covent.aphex.dsp.ServiceAphexDSP.LocalBinder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ActivityAphexDSP extends Activity {
	//Log tag to be passed into DEBUG_LOG
    private static final String LOG_TAG = "ActivityAphexDSP";

    //Views
    private TextView mStatusTextView;
    
    //Toggle button
    private ToggleButton mToggleButton;
    
    //Nav button
    private Button mButton;
    
	//Service
	ServiceAphexDSP mService;
	boolean mBound = false;
	boolean active = true;

	//Context
	private Context mContext;
	
	/***************************************************
	 * Services
	 *************************************************/

	/**
	 * Service Connection
	 * When you are bound to the service you will get an asynchronous 
	 * callback to onServiceConnected() in your SeviceConnection
	 * @Author Mstanford
	 */
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
			CONSTANTS.DEBUG_LOG("SERVICE IS", "" +mService);
		}

		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};
	
	/**
	 * Broadcast receiver for toggle button
	 * We are sending an intent for the toggle from the service
	 * @author mStanford
	 *
	 */
	public BroadcastReceiver ActivityToggleReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			CONSTANTS.DEBUG_LOG(LOG_TAG, "Activity Broadcast Reciever hit");
			String action = intent.getAction();
			//Check to see if the action is our key
		    if(CONSTANTS.ACTIVITY_TOGGLE_TRUE.equals(action)) {
		    	//Toggle the DSP
		    	mToggleButton.setChecked(true);
		    	mStatusTextView.setText("Effect: " + mService.getEnabled());
		    } 
		    if(CONSTANTS.ACTIVITY_TOGGLE_FALSE.equals(action)) {
		    	//Toggle the DSP
		    	mToggleButton.setChecked(false);
		    	mStatusTextView.setText("Effect: " + mService.getEnabled());
		    } 
		}           
	};
	
	/**
	 * On*** Classes
	 */

	@Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main_activity);
        
        //Need a context for something somewhere Im sure
        mContext = this;

        //Status view
        mStatusTextView = (TextView)findViewById(R.id.tv_DSP_Status);
                
        //Toggle button
        mToggleButton = (ToggleButton)(findViewById(R.id.tb_DSP_toggle_button));
        
        //Nav Button
        mButton = (Button) findViewById(R.id.btn_nav_settings);
        
        //Toggle onClickListener
        mToggleButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(mService.getEnabled()){
					mService.setEnabled(!mService.getEnabled());
					Toast.makeText(getBaseContext(), "DSP OFF: " + mService.getEnabled(), Toast.LENGTH_SHORT).show();
					mStatusTextView.setText("Effect: " + mService.getEnabled());
					mToggleButton.setChecked(false);
				}
				else if(!mService.getEnabled()){
					mService.setEnabled(!mService.getEnabled());
					Toast.makeText(getBaseContext(), "DSP ON: " + mService.getEnabled(), Toast.LENGTH_SHORT).show();
					mStatusTextView.setText("Effect: " + mService.getEnabled());
					mToggleButton.setChecked(true);
				}
			}
        	
        });
        
        mButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent mIntent = new Intent(mContext,PresetActivity.class);
				startActivity(mIntent);
			}
        });
    }
    
	
    @Override
	protected void onResume() {
		super.onResume();
        /**
         * Broadcast receiver
         */
        IntentFilter mToggleIntent = new IntentFilter();
        mToggleIntent.addAction(CONSTANTS.ACTIVITY_TOGGLE_TRUE);
        mToggleIntent.addAction(CONSTANTS.ACTIVITY_TOGGLE_FALSE);
        registerReceiver(ActivityToggleReceiver, mToggleIntent); 
        
        //Make it tight looking for when it stops and starts again
        if(mService != null){
        	mToggleButton.setChecked(mService.getEnabled());
        	mStatusTextView.setText("Effect: " + mService.getEnabled());
        }
	}


	@Override
	protected void onStart(){
		super.onStart();
		/****************************************
		 * SERVICES
		 ***************************************/
		if(mService == null){
			// Bind to LocalService
			Intent mIntent = new Intent(mContext, ServiceAphexDSP.class);
			startService(mIntent);
			//http://developer.android.com/guide/components/bound-services.html
			bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
			CONSTANTS.DEBUG_LOG("ONSTART SERVICE IS", "" +mService);
		}
	}


	@Override
	protected void onStop() {
		super.onStop();
		//Unreg receiver
		unregisterReceiver(ActivityToggleReceiver);
	}
	
	
}
