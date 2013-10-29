package com.covent.aphex.dsp;

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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.covent.aphex.dsp.ServiceAphexDSP.LocalBinder;

public class ActivityAphexDSP extends Activity {
	//Log tag to be passed into DEBUG_LOG
    private static final String LOG_TAG = "ActivityAphexDSP";
    
    //Toggle button
    private Switch mToggleButton;
    
    //Nav button
    private Button mSettingsNavButton;
    
	//Service
	ServiceAphexDSP mService;
	boolean mBound = false;
	boolean active = true;

	//Context
	private Context mContext;
	
	private RadioGroup mRadioGroup;
	
	private TextView[] mTextViewArray = new TextView[6]; 
	
	
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
	 * BROADCAST RECEIVER
	 * 
	 * Receiver for when the preset is set in the Service.  It tells me to update the UI
	 * I moved all my logic to the Service as it fixed the syncing issues
	 * @author mStanford
	 *
	 */
	public BroadcastReceiver SetPresetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			CONSTANTS.DEBUG_LOG(LOG_TAG, "Preset has gone through, UI update");
			if(CONSTANTS.DEBUG)
				Toast.makeText(mContext, "Preset: " + mService.getPreset(), Toast.LENGTH_SHORT).show();
			setRadioButtons();
			setTextOutput();
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
		    	setRadioButtons();
		    } 
		    if(CONSTANTS.ACTIVITY_TOGGLE_FALSE.equals(action)) {
		    	//Toggle the DSP
		    	mToggleButton.setChecked(false);
		    	setRadioButtons();
		    } 
		}           
	};
	
	/**
	 * The method called when user selects radio buttons
	 * @param checkedId
	 */
	private void setRadioButtons(int checkedId){
		int mIndex;
		mIndex=mRadioGroup.indexOfChild(findViewById(checkedId));
		//If custom then send preset -1
		if(mIndex == 3){
			mService.setPreset(-1);
			return;
		}
		mService.setPreset(mIndex);
	}
	
	/**
	 * method called when updating radio buttons to reflect changes
	 * @param checkedId
	 */
	private void setRadioButtons(){
		switch (mService.getPreset()){
			case -1:
				mRadioGroup.check(R.id.preset_custom);
				break;
			case 0:
				mRadioGroup.check(R.id.preset_low);
				break;
			case 1:
				mRadioGroup.check(R.id.preset_medium);
				break;
			case 2:
				mRadioGroup.check(R.id.preset_high);
				break;
			default:
				mRadioGroup.check(R.id.preset_custom);
				break;
		}
	}

	private void setTextOutput(){
		for(int i = 0; i < mService.getEQ().getNumberOfBands(); i++){
			mTextViewArray[i].setText("" + mService.getBandLevel((short)i)/100);
		}
	}
	
	/**
	 * On*** Classes
	 */

	@Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main_activity);
        
        //Need a context for something somewhere I'm sure
        mContext = this;
                
        mTextViewArray[0] = (TextView) findViewById(R.id.tv_ae_tune_out_main);
        mTextViewArray[1] = (TextView) findViewById(R.id.tv_ae_harm_out_main);
        mTextViewArray[2] = (TextView) findViewById(R.id.tv_ae_mix_out_main);
        mTextViewArray[3] = (TextView) findViewById(R.id.tv_bb_tune_out_main);
        mTextViewArray[4] = (TextView) findViewById(R.id.tv_bb_drive_out_main);
        mTextViewArray[5] = (TextView) findViewById(R.id.tv_bb_mix_out_main);
        
        //Toggle button
        mToggleButton = (Switch) findViewById(R.id.main_dsp_switch);
        
        //Nav Button
        mSettingsNavButton = (Button) findViewById(R.id.btn_nav_settings);
        
        //Toggle onClickListener
        mToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
					mService.setEnabled(isChecked);
					mToggleButton.setChecked(isChecked);
			} 	
        });
        
        mSettingsNavButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent mIntent = new Intent(mContext,PresetActivity.class);
				startActivity(mIntent);
			}
        });
        
        mRadioGroup = (RadioGroup) findViewById(R.id.preset);
        
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				setRadioButtons(checkedId);
		        setTextOutput();
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
        
		/**
		 * Broadcast receiver for when it gets a preset
		 */
		IntentFilter mPresetIntent = new IntentFilter();
		mPresetIntent.addAction(CONSTANTS.ACTION_PRESET_COMPLETED);
        registerReceiver(SetPresetReceiver, mPresetIntent); 
        
        //Make it tight looking for when it stops and starts again
        if(mService != null){
        	mToggleButton.setChecked(mService.getEnabled());
        	setRadioButtons();
            setTextOutput();
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
		unregisterReceiver(SetPresetReceiver);
	}
	
	
}
