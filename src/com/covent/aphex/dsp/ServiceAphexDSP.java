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
	//private final ServiceBinder mBinder = new ServiceBinder();

	//Custom preset
	private short[] mCustomPreset = {50,50,50,50,50,50};
	
	//Preferences manager
	private PreferencesManager mPreferenceManager;


	/**
	 * Broadcast receiver for the notification bar
	 * We are sending an intent for the toggle and we will grab the 
	 * intent and action here to toggle the DSP.  We send an intent to the Main UI for the 
	 * toggle.  That way if you have the main screen up and hit the toggle from the notification bar
	 * it will change the UI in the main screen too.
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
				//Create a new intent, this is for the main screen toggle button
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

	/**
	 * Broadcast receiver for the presets.  
	 * Grabs the extra then sends that value to the set preset method
	 */
	public BroadcastReceiver PresetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			CONSTANTS.DEBUG_LOG(LOG_TAG, "Preset Receiver Hit");
			String action = intent.getAction();
			//Check to see if the action is our key
			if(CONSTANTS.PRESET_ACTION.equals(action)) {
				//Grab the preset from the extra
				int mPreset = intent.getIntExtra(CONSTANTS.PRESET_EXTRA, 1);
				setPreset(mPreset);
				
				//Debugging information
				CONSTANTS.DEBUG_LOG(LOG_TAG, "BAND LVL :" + 
						mEqualizer.getBandLevel((short)0) + "\n " + "BAND LVL :" +
						mEqualizer.getBandLevel((short)1) + "\n " + "BAND LVL :" +
						mEqualizer.getBandLevel((short)2) + "\n " + "BAND LVL :" +
						mEqualizer.getBandLevel((short)3) + "\n " + "BAND LVL :" +
						mEqualizer.getBandLevel((short)4) +"\n " + "BAND LVL :" +
						mEqualizer.getBandLevel((short)5));
			} 
		}           
	};


	public void onCreate() {
		super.onCreate();

		mEqualizer = new Equalizer(CONSTANTS.EQ_PRIORITY, 0);
		mEqualizer.setEnabled(true);
		setNotification();
		
		//Start the preferences manager
		mPreferenceManager = new PreferencesManager(getApplicationContext());

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
		recallPreset();
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

	/**
	 * Sets the notification up.  Call this everytime you update the toggle status
	 */
	public void setNotification(){
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

	/**
	 * Sets the DSP as enabled
	 * Releases the DSP first, this may solve some issues where you need to toggle the DSP
	 * to get the effect global again after setting a different effect global
	 * @param enabled
	 * @return
	 */
	public boolean setEnabled(boolean enabled){
		mEqualizer.release();
		mEqualizer = new Equalizer(CONSTANTS.EQ_PRIORITY, 0);
		mEqualizer.setEnabled(enabled);
		if(enabled){
			recallPreset();
			updatePresetUI();
		}
		setNotification();
		return mEqualizer.getEnabled();
	}

	/**
	 * Returns whether the DSP is enabled
	 * @return
	 */
	public boolean getEnabled(){
		return mEqualizer.getEnabled();
	}	

	/**
	 * Returns how many presets there are.  
	 * Eventually, there will be a setter and getter for everyhting in the EQ that way I dont have
	 * to pass the EQ to the activities
	 * @return
	 */
	public short getNumPresets(){
		return mEqualizer.getNumberOfPresets();
	}

	/**
	 * Gets the preset name
	 * @param preset
	 * @return
	 */
	public String getPresetName(short preset){
		return mEqualizer.getPresetName(preset);
	}

	/**
	 * Mostly, for debugging, I will make setters and getters for everything I'm currently 
	 * using this for
	 * @return
	 */
	public Equalizer getEQ() {
		return mEqualizer;
	}

	/**
	 * I think the native code returns -1 if it is custom but I cant find that for sure
	 * in the java or JNI files.  The native code says it does, but I can't trust that to fall down the
	 * stack to here.  I am checking myself to see if there is a custom preset
	 * @return
	 */
	public short getPreset() {
		if(mPreferenceManager.isCustom())
			return -1;
		return mEqualizer.getCurrentPreset();
	}

	/**
	 * Sets the gain value for the custom preset in the custom preset data structure
	 * @param band
	 * @param progress
	 */
	public void setSharedCustomBand(short band, short gain) {
		mCustomPreset[band] = gain;
	}
	
	/**
	 * Sets the whether it is a custom preset or not
	 * @param mIsCustom
	 */
	public void setSharedIsCustom(boolean mIsCustom){
		mPreferenceManager.setIsCustom(mIsCustom);
	}
	
	/**
	 * Sets the preset in the shared prefs
	 * @param preset
	 */
	public void setSharedPreset(short preset){
		mPreferenceManager.setPreset(preset);
	}
	
	/**
	 * For some reason this is returning a more accurate preset number than the EQ
	 * The EQ is always behind by 1 preset
	 * @param preset
	 */
	public short getSharedPreset(){
		return mPreferenceManager.getPreset();
	}

	/**
	 * Sets the band level for the EQ
	 * Also sets the band level in the custom preset data structure and sets the 
	 * custom flag for the shared prefs
	 * @param band
	 * @param level
	 */
	public void setBandLevel(short band, short level) {
		for(short i = 0; i < CONSTANTS.bands; i++){
			setSharedCustomBand(i, getBandLevel(i));
		}
		mEqualizer.setBandLevel(band, level);
		setSharedIsCustom(true);
		setSharedCustomBand(band, level);
		updatePresetUI();
	}
	
	/**
	 * Grabs the Band Level from the EQ
	 */
	public short getBandLevel(short band){
		return mEqualizer.getBandLevel(band);
	}
	
	/**
	 * Sets the preset
	 * If the preset is -1 then that means it is custom.  This is a byproduct of having custom 
	 * as preset 0 in the spinner.  That means that I have to send itemPosition - 1 to the 
	 * broadcast receiever.
	 * @param mPreset The preset to set
	 */
	public void setPreset(int mPreset) {
		if(mPreset == -1){
			//-1 means it is a custom preset
			setSharedIsCustom(true);
			//recall the settings from custom
			recallPreset();
			//leave the loop
			return;
		}
		//Set preset to the EQ
		mEqualizer.usePreset((short)mPreset);
		//Set custom to off
		setSharedIsCustom(false);
		//Set the current preset
		setSharedPreset((short)mPreset);
		//Set the Preset values to the custom values, so when you move the slider it doesn't jump all the values back to the 
		//previous custom view.  Comment this out to have custom setting jump when slider is moved or custom is selected TODO
//		for(short i = 0; i < CONSTANTS.bands; i++){
//			setSharedCustomBand(i, getBandLevel(i));
//		}
		//Update the preset UI
		updatePresetUI();
	}
	
	/**
	 * Sets the saved values to the EQ
	 * Checks to see if it is custom, if it is, then it sets the values to the custom
	 * If it's a preset then it sets the EQ to the preset saved in the shared prefs
	 */
	public void recallPreset(){
	//Check to see if the preset is custom
		if(mPreferenceManager.isCustom()){
			for(int i = 0; i < mEqualizer.getNumberOfBands(); i++){
				mEqualizer.setBandLevel((short)i, mCustomPreset[i]);
			}
			updatePresetUI();
		} else {
			setPreset(mPreferenceManager.getPreset());
		}
	}
	
	/**
	 * Sends a broadcast for the preset UI to update
	 * There was a large delay between when sending the preset and when the preset 
	 * would be set in the EQ, so I made a broadcast for when it wen through.  
	 * This fixed all the sync issues.
	 */
	public void updatePresetUI(){
		//Create a new intent for the completed preset
		Intent mPresetIntent = new Intent();
		//set the action to be preset completed
		mPresetIntent.setAction(CONSTANTS.ACTION_PRESET_COMPLETED);
		//Broadcast so the service can receive and set the EQ
		sendBroadcast(mPresetIntent);
	}
}
