package com.covent.aphex.dsp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.audiofx.Equalizer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.ToggleButton;

public class ServiceAphexDSP extends Service {

	//The EQ
	Equalizer mEqualizer;

	//Make Toggle be pretty
	ToggleButton mToggleButton;

	private final String LOG_TAG = "ServiceAPhexDSP";

	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Broadcast receiver for the notification bar
	 * We are sending an intent for the toggle and we will grab the 
	 * intent and action here to toggle the DSP
	 * @author mStanford
	 *
	 */
	public BroadcastReceiver ToggleReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			CONSTANTS.DEBUG_LOG(LOG_TAG, "Broadcast Reciever hit");
			String action = intent.getAction();
			//Check to see if the action is our key
			if(CONSTANTS.ACTION_TOGGLE.equals(action)) {
				//Toggle the DSP
				setEnabled(!getEnabled());
				//Create a new intent
				Intent mActivityToggleIntent = new Intent();
				if(getEnabled()){
					//Set an action we will listen to
					mActivityToggleIntent.setAction(CONSTANTS.ACTIVITY_TOGGLE_TRUE);
				}else{
					mActivityToggleIntent.setAction(CONSTANTS.ACTIVITY_TOGGLE_FALSE);
				}
				//Send the Intent
				sendBroadcast(mActivityToggleIntent);
			} 
		}           
	};
	
	public BroadcastReceiver PresetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			CONSTANTS.DEBUG_LOG(LOG_TAG, "Preset Receiver Hit");
			String action = intent.getAction();
			//Check to see if the action is our key
			if(CONSTANTS.PRESET_ACTION.equals(action)) {
				int mPreset = intent.getIntExtra(CONSTANTS.PRESET_EXTRA, 0);
					mEqualizer.usePreset((short)mPreset);
			} 
		}           
	};


	public void onCreate() {
		super.onCreate();

		mEqualizer = new Equalizer(CONSTANTS.EQ_PRIORITY, 0);
		mEqualizer.setEnabled(true);
		setNotification();

		//Toggle receiver
		IntentFilter mToggleIntent = new IntentFilter();
		mToggleIntent.addAction(CONSTANTS.ACTION_TOGGLE);
		registerReceiver(ToggleReceiver, mToggleIntent); 
		
		//Preset receiver
		IntentFilter mPresetIntent = new IntentFilter();
		mPresetIntent.addAction(CONSTANTS.PRESET_ACTION);
		registerReceiver(PresetReceiver, mPresetIntent); 
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		setNotification();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//release EQ
		mEqualizer.release();
		//release the notification bar
		stopForeground(true);
		//release the reciever
		unregisterReceiver(ToggleReceiver);
		unregisterReceiver(PresetReceiver);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public ServiceAphexDSP getService() {
			return ServiceAphexDSP.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	public void setNotification(){

		//		/*
		//		 * Set the notification and the pending intent for the activity
		//		 * The intent flags and notification flags need bitwise operator | to add more
		//		 */
		//		Notification mNotification=new Notification(R.drawable.ic_launcher_aphex, "Aphex DSP", System.currentTimeMillis());
		//        Intent mIntent=new Intent(this, ActivityAphexDSP.class);
		//        
		//        //Set as the top and clear everything in t he way to make it happen if it's not already there. 
		//        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//        
		//        //Sets the pending intent for the activity, it's an intent that can be called from another activity
		//        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mIntent, 0);
		//        
		//        //Sets the latest event info for the notification and sets it to not be cleared.
		//        mNotification.setLatestEventInfo(this, "Aphex DSP","DSP Enabled: " + getEnabled(), mPendingIntent );
		//        mNotification.flags|=Notification.FLAG_NO_CLEAR;
		//        
		//        //Set as a foreground service so it is not shut down
		//        startForeground(SERVICE_ID, mNotification);

		/*
		 * Set the clickable logic.  
		 * Create an intent that will be received by our broadcast receiver and it will toggle the DSP
		 */

		//Create a new intent
		Intent mToggleIntent = new Intent();

		//Set an action we will listen to
		mToggleIntent.setAction(CONSTANTS.ACTION_TOGGLE);

		//Make the intent a pending intent so it can be called from another activity
		PendingIntent mTogglependingIntent = PendingIntent.getBroadcast(this, CONSTANTS.REQUEST_CODE, mToggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		//Create a new notification
		Notification mToggleNotification=new Notification(R.drawable.ic_launcher_aphex, "DSP Toggle", System.currentTimeMillis());

		String enabled;
		if(getEnabled()){
			enabled = "ON";
		}else{
			enabled = "OFF";
		}
		//Set parameters for the notification
		mToggleNotification.setLatestEventInfo(this, "Aphex DSP : " + enabled,"Click to Toggle.", mTogglependingIntent );
		mToggleNotification.flags|=Notification.FLAG_NO_CLEAR;

		//Set as a foreground service so it is not shut down
		startForeground(CONSTANTS.SERVICE_ID, mToggleNotification);
	}


	/*
	 * SETTERS AND GETTERS 
	 */

	public boolean setEnabled(boolean enabled){
		mEqualizer.release();
		mEqualizer = new Equalizer(CONSTANTS.EQ_PRIORITY, 0);
		mEqualizer.setEnabled(enabled);
		setNotification();
		return mEqualizer.getEnabled();
	}

	public boolean getEnabled(){
		return mEqualizer.getEnabled();
	}	

	public short getNumPresets(){
		return mEqualizer.getNumberOfPresets();
	}

	public String getPresetName(short preset){
		return mEqualizer.getPresetName(preset);
	}

	public Equalizer getEQ() {
		return mEqualizer;
	}
}
