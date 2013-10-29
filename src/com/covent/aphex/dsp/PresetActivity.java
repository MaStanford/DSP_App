package com.covent.aphex.dsp;

import com.covent.aphex.dsp.ServiceAphexDSP.LocalBinder;

import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * The preset screen for the DSP app.  
 * It has a spinner for selecting a preset.  
 * It has seekbars that send the band level to the EQ
 * It binds to the ongoing service, where we can accesss the EQ, presets and the preferences manager
 * There is a broadcast listener for when the EQ is toggled, it sets the correct preset or band level if custom
 * When debugging is set in the constants there are text views that print out the values and print out the presets
 * 
 * About the wierd SeekBar logic:
 * I decided to go with having the seekbars in the xml instead of programmaticly adding them to the 
 * linear layout.  This is a trade off between ease of changing individual seekbars in case we need to do
 * that in the future and being too verbose.  Right now it is fairly verbose but I felt that programmatically adding
 * the SeekBars was less verbose but didn't offer enough customisation.
 * 
 * 
 * @author mStanford
 *
 */
public class PresetActivity extends Activity {

	//Debugging views
	String LOG_TAG = "Aphex DSP PresetActivity";
	LinearLayout mLinearLayout;
	EditText mEditText;
	Button mOkButton;
	
	//navigation button
	private Button mHomeButton;
	private Button mCreditsButton;
	
	//Context
	private Context mContext;
	
	//Service
	ServiceAphexDSP mService;
	boolean mBound = false;
	boolean active = true;
	
	//SeekBars
	SeekBar mSliderAETune,mSliderAEHarm,mSliderAEMix,mSliderBBTune,mSliderBBDrive,mSliderBBMix;
	SeekBar[] mSeekBarArray = {mSliderAETune,mSliderAEHarm,mSliderAEMix,
			mSliderBBTune,mSliderBBDrive,mSliderBBMix};
	TextView mSeekAETune,mSeekAEHarm,mSeekAEMix,mSeekBBTune,mSeekBBDrive,mSeekBBMix;
	TextView[] mSeekPos = {mSeekAETune,mSeekAEHarm,mSeekAEMix,
			mSeekBBTune,mSeekBBDrive,mSeekBBMix};
	
	//Spinner
	Spinner mSpinner;
	
    //Toggle button
    private ToggleButton mToggleButton;
    
    //Radio Group
    RadioGroup mRadioGroup;

	/***************************************************
	 * Services
	 *************************************************/

	/**
	 * Service Connection
	 * When you are bound to the service you will get an asynchronous 
	 * callback to onServiceConnected() in your SeviceConnection
	 * @Author mStanford
	 */
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
			CONSTANTS.DEBUG_LOG("Preset SERVICE IS", "" +mService);
			//Sets the parameters, max, min, listeners. 
			setupSeekBars();
			//Setup the Spinner
			//setupSpinner();
			//Update the screen when the service is connected, needs to be after seekbar and spinner
			//are set otherwise get null pointers
			updateUI();
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
			updateUI();
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
	 * Calls the methods to update the UI
	 * In debugging mode there are two extra methods called which write out textviews
	 * with the current values of the bands and the list of presets
	 * @param preset
	 */
	private void updateUI(){
		if(CONSTANTS.DEBUG){
			mLinearLayout.removeAllViews();
			setupDebugInput();
			writePresetValuesToScreen();
			writePresetList();
		}
		//Set values to seekbars
		setSeekBars();
		//Set value to spinner
		//setSpinner();
		setRadioButtons();
	}
	
	/*****************************************
	 * 
	 * DEBUG METHODS
	 * 
	 ****************************************/

	/**
	 * Writes the current band levels to screen during debug mode
	 * @param preset
	 */
	private void writePresetValuesToScreen(){
		//Just grab the EQ values
		TextView mTVPresetValues = new TextView(mContext);
		mTVPresetValues.setText("Band Level: \n");
		Equalizer mEQ = mService.getEQ();
		int bands = mEQ.getNumberOfBands();
		for(int i = 0; i < bands; i++){
			mTVPresetValues.append("Band[" + i +"] = " + mEQ.getBandLevel((short)i) + "\n");
		}
		mLinearLayout.addView(mTVPresetValues);
	}

	/**
	 * Writes the preset list to screen during debug mode
	 */
	private void writePresetList(){
		//Presets List
		TextView mTVPresetList = new TextView(mContext);
		mTVPresetList.setText("Presets: \n");
		/**
		 * Preset logic
		 */
		for(short k=0; k < mService.getEQ().getNumberOfPresets(); k++){ 
			mTVPresetList.append("# " + k + " " + mService.getEQ().getPresetName(k) + " \n");
		}
		mTVPresetList.append("Current Preset: " + mService.getSharedPreset());
		mLinearLayout.addView(mTVPresetList);
	}
	
	/**
	 * Set up the button and text edit for the debugging view
	 */
	private void setupDebugInput(){
		
		//Edit Text
		mEditText = new EditText(mContext);
		//2 is for type number
		mEditText.setInputType(2);
		//Set to layout
		mLinearLayout.addView(mEditText);
		
		//ok button
		mOkButton = new Button(mContext);
		mOkButton.setText("Set Preset");
		//Broadcasts intent that preset has been selected and then puts extra for the preset.
		//Also makes a toast and writes the preset to screen and the sliders
		mOkButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//Grab the preset from the Text view
				int preset = Integer.parseInt(mEditText.getText().toString());
				sendPreset(preset);
			}
		});
		mLinearLayout.addView(mOkButton);
	}
	
	/*****************************************
	 * 
	 * END DEBUG METHODS
	 * 
	 ****************************************/

	/**
	 * Sets the seeksbars when the update UI is called. 
	 * It grabs the values from the Service
	 */
	private void setSeekBars() {
		for(int i = 0; i < mService.getEQ().getNumberOfBands(); i++){
			mSeekBarArray[i].setProgress((int) ((float)mService.getEQ().getBandLevel((short) i)/100.00f));
			mSeekPos[i].setText(String.format( "%1.2f", ((float)mService.getEQ().getBandLevel((short) i)/10000.00f)));
		}
	}

	/**
	 * Sets the listeners for the seekbars
	 */
	private void setupSeekBars() {
		for(int i = 0; i < mService.getEQ().getNumberOfBands(); i++){
			final short band = (short) i;
			//Set the max to 100.  We actually need to send 1,000 to the java effect.  100 is easier to math
			mSeekBarArray[i].setMax(100);
			//Sets the listeners
			mSeekBarArray[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					//Makes sure we aren't double dipping
					if(fromUser)
						mService.setBandLevel(band, (short) (progress * 100));
					mSeekPos[band].setText(String.format( "%1.2f", ((float)progress/100.0f)));
				}

				public void onStartTrackingTouch(SeekBar seekBar) {

				}
				
				//Update the UI when stop tracking.  This is for debugging
				public void onStopTrackingTouch(SeekBar seekBar) {
					if(CONSTANTS.DEBUG)
						updateUI();
				}
			});
		}
	}
	
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
	
	
//	/**
//	 * Sets up the spinner/drop down menu
//	 * The spinner is offset by 1.  0 is custom which internally we use -1 for custom.
//	 * This gives us some wierd math down the stack.
//	 */
//	public void setupSpinner(){
//		//Spinner - drop down selector
//		mSpinner = (Spinner) findViewById(R.id.spinner_preset);
//		//List for the array adapter
//		List<String> mList = new ArrayList<String>();
//		//Add Custom as the first in the list
//		mList.add("Custom");
//		//Fill up the list
//		for(short i = 0; i < mService.getNumPresets(); i++){
//			mList.add(mService.getPresetName(i));
//		}
//		//Set up the array adapter
//		ArrayAdapter<String> mArrayAdapter = 
//				new ArrayAdapter<String>(mContext,17367052,mList);
//		//Set the adapter
//		mSpinner.setAdapter(mArrayAdapter);
//		//Have the Current Preset Selected, if preset is -1 then its custom
//		if(mService.getPreset() > 0)
//			mSpinner.setSelection(mService.getSharedPreset()+1);
//		else{
//			mSpinner.setSelection(0);
//		}
//		
//		/*
//		 * On item selected for the spinner.  I need to subtract by 1 to make the presets accurate.  Custom
//		 * is at 0, so to get the 0 preset to send I need to subtract it by 1 to make it accurate.
//		 */
//		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//			@Override
//			public void onItemSelected(AdapterView<?> arg0, View arg1,
//					int arg2, long arg3) {
//				sendPreset(arg2 - 1);
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> arg0) {
//
//			}
//		});
//	}
//	
//	/**
//	 * Sets the spinner to the current preset in the Service
//	 * If the preset is -1 then it is the custom setting which is 0
//	 * I need to add + 1 to everything since -1 is custom and that is 0 in the spinner
//	 */
//	public void setSpinner(){
//		//Have the Current Preset Selected, if preset is -1 then its custom
//		if(mService.getPreset() >= 0)
//			mSpinner.setSelection(mService.getSharedPreset()+1);
//		else{
//			mSpinner.setSelection(0);
//		}
//	}
	
	/**
	 * Sends the preset intent to the service.  
	 * @param preset The preset to send to the Service
	 */
	public void sendPreset(int preset){
		//Create a new intent
		Intent mPresetIntent = new Intent();
		//set the action to be preset action
		mPresetIntent.setAction(CONSTANTS.PRESET_ACTION);
		//add the extra to the action, which is the preset
		mPresetIntent.putExtra(CONSTANTS.PRESET_EXTRA,preset);
		//Broadcast so the service can receive and set the EQ
		sendBroadcast(mPresetIntent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preset_activity);

		//Ill need context I think
		mContext = this;

		//seekbar
		mSeekBarArray[0] = (SeekBar) findViewById(R.id.seekBar1);
		mSeekBarArray[1] = (SeekBar) findViewById(R.id.seekBar2);
		mSeekBarArray[2] = (SeekBar) findViewById(R.id.seekBar3);
		mSeekBarArray[3] = (SeekBar) findViewById(R.id.seekBar4);
		mSeekBarArray[4] = (SeekBar) findViewById(R.id.seekBar5);
		mSeekBarArray[5] = (SeekBar) findViewById(R.id.seekBar6);
		//SeekBar position tv
		mSeekPos[0] = (TextView) findViewById(R.id.tv_AETune);
		mSeekPos[1] = (TextView) findViewById(R.id.tv_AEHarm);
		mSeekPos[2] = (TextView) findViewById(R.id.tv_AEMix);
		mSeekPos[3] = (TextView) findViewById(R.id.tv_BBTune);
		mSeekPos[4] = (TextView) findViewById(R.id.tv_BBDrive);
		mSeekPos[5] = (TextView) findViewById(R.id.tv_BBMix);

		//Linear layout for adding debugging texts
		mLinearLayout = (LinearLayout) findViewById(R.id.ll_debug_view);
		
		//Nav button to main screen
		mHomeButton = (Button) findViewById(R.id.btn_nav_toggle);
		mCreditsButton = (Button) findViewById(R.id.btn_credits);
		//Sends intent to start the main screen
		mHomeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent mIntent = new Intent(mContext,ActivityAphexDSP.class);
				startActivity(mIntent);
			}
		});
		mCreditsButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent mIntent = new Intent(mContext,ActivityCredits.class);
				startActivity(mIntent);
			}
		});
		
        //Toggle button
        mToggleButton = (ToggleButton)(findViewById(R.id.tb_DSP_toggle_button_settings));
        
        //Toggle onClickListener
        mToggleButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(mService.getEnabled()){
					mService.setEnabled(!mService.getEnabled());
					if(CONSTANTS.DEBUG)
						Toast.makeText(getBaseContext(), "DSP OFF: " + mService.getEnabled(), Toast.LENGTH_SHORT).show();
					mToggleButton.setChecked(false);
				}
				else if(!mService.getEnabled()){
					mService.setEnabled(!mService.getEnabled());
					if(CONSTANTS.DEBUG)
						Toast.makeText(getBaseContext(), "DSP ON: " + mService.getEnabled(), Toast.LENGTH_SHORT).show();
					mToggleButton.setChecked(true);
				}
			} 	
        });
        
        mRadioGroup = (RadioGroup) findViewById(R.id.preset);
        
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				setRadioButtons(checkedId);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Bind to LocalService
		Intent intent = new Intent(mContext, ServiceAphexDSP.class);
		//http://developer.android.com/guide/components/bound-services.html
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		CONSTANTS.DEBUG_LOG("Preset ONSTART SERVICE IS", "" +mService);

	}
	@Override
	protected void onResume() { 
		super.onResume();
		/**
		 * Broadcast receiver for when it gets a preset
		 */
		IntentFilter mPresetIntent = new IntentFilter();
		mPresetIntent.addAction(CONSTANTS.ACTION_PRESET_COMPLETED);
        registerReceiver(SetPresetReceiver, mPresetIntent); 
        
        /**
         * Broadcast receiver for the toggle button.
         */
        IntentFilter mToggleIntent = new IntentFilter();
        mToggleIntent.addAction(CONSTANTS.ACTIVITY_TOGGLE_TRUE);
        mToggleIntent.addAction(CONSTANTS.ACTIVITY_TOGGLE_FALSE);
        registerReceiver(ActivityToggleReceiver, mToggleIntent); 
        
        //Make it tight looking for when it stops and starts again
        if(mService != null){
        	mToggleButton.setChecked(mService.getEnabled());
        	setRadioButtons();
        }
	}
	
	@Override 
	protected void onStop(){
		super.onStop();
		//Unreg receiver
		unregisterReceiver(ActivityToggleReceiver);
		unregisterReceiver(SetPresetReceiver);
	}
}
