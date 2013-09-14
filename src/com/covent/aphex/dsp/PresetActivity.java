package com.covent.aphex.dsp;

import com.covent.aphex.dsp.ServiceAphexDSP.LocalBinder;

import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PresetActivity extends Activity {
	
	//Place holder text view until I get spinner set up
	private TextView mPresetTextView;
	private TextView mPresetTextValues;
	//The linear layout for the sliders
	private LinearLayout mLinearLayout;
	//nav button
	private Button mButton;
	//Context
	private Context mContext;
	//Service
	ServiceAphexDSP mService;
	boolean mBound = false;
	boolean active = true;
	//Testing for presets
	EditText mEditText;
	//Ok Button for edit text
	Button mOkButton;
	
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
			updateUI(0);
		}

		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};
	
	/**
	 * updates the UI
	 * @param preset
	 */
	private void updateUI(int preset){
		if(CONSTANTS.DEBUG){
			writePresetList();
			writePresetValuesToScreen(preset);
		}
		setupEqualizerFxAndUI(preset);
	}
	
	/**
	 * for DEBUG writing bands to screen
	 * @param preset
	 */
	private void writePresetValuesToScreen(int preset){
		//Just grab the EQ values
		mPresetTextValues.setText("");
		Equalizer mEQ = mService.getEQ();
		int bands = mEQ.getNumberOfBands();
		for(int i = 0; i < bands; i++){
			mPresetTextValues.append("Band[" + i +"] = " + mEQ.getBandLevel((short)i) + "\n");
		}
	}
	
	private void writePresetList(){
        //Presets List
        mPresetTextView = (TextView) findViewById(R.id.tv_preset);
        mPresetTextView.setText("Presets: \n");
        /**
         * Preset logic
         */
        for(short k=0; k < mService.getEQ().getNumberOfPresets(); k++){ 
        	mPresetTextView.append("#" + k + mService.getEQ().getPresetName(k) + " \n");
        }
	}
	
	private void setupEqualizerFxAndUI(int preset) {

        short bands = mService.getEQ().getNumberOfBands();

        final int minEQLevel = 0;//mEqualizer.getBandLevelRange()[0];
        final int maxEQLevel = 100;//mEqualizer.getBandLevelRange()[1];

        //Sets up the sliders to be moved, not to receive 
        for (short i = 0; i < bands; i++) {
            final short band = i;
            //Sets the band name and adds view to linear layout
            TextView freqTextView = new TextView(this);
            freqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            freqTextView.setText(CONSTANTS.bandName[band]);
            mLinearLayout.addView(freqTextView);

            //Creates the row which will have the slider
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            //Prints the min value to the row
            TextView minDbTextView = new TextView(this);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            minDbTextView.setText("" + minEQLevel);
            //Prints the max value to the row
            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            maxDbTextView.setText("" + maxEQLevel);
            //Sets up the seek bar
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            SeekBar bar = new SeekBar(this);
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel);
            bar.setProgress(mService.getEQ().getBandLevel(band) / 100);
            //sets the listner for the bar, updates the EQ band level
            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    mService.getEQ().setBandLevel(band, (short) (progress * 100));
                }

                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            //adds views the row
            row.addView(minDbTextView);
            row.addView(bar);
            row.addView(maxDbTextView);
            //adds row to the linear layout
            mLinearLayout.addView(row);
        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preset_activity);
		
		//Ill need context I think
		mContext = this;
		
		//Slider linearLayout
		mLinearLayout = (LinearLayout) findViewById(R.id.ll_preset_sliders);
		
		//EditText for preset testing
		mEditText = (EditText) findViewById(R.id.et_preset_num);
		mPresetTextValues = (TextView) findViewById(R.id.tv_preset_values);
		
		//Nav button
		mButton = (Button) findViewById(R.id.btn_nav_toggle);
		//Sends intent to start the main screen
        mButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent mIntent = new Intent(mContext,ActivityAphexDSP.class);
				startActivity(mIntent);
			}
        });
		
		//ok button
		mOkButton = (Button) findViewById(R.id.btn_ok_preset);
		//Broadcasts intent that preset has been selected and then puts extra for the preset.
		//Also makes a toast and writes the preset to screen and the sliders
        mOkButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				int preset = Integer.parseInt(mEditText.getText().toString());
				Intent mPresetIntent = new Intent();
				mPresetIntent.setAction(CONSTANTS.PRESET_ACTION);
				mPresetIntent.putExtra(CONSTANTS.PRESET_EXTRA,preset);
				sendBroadcast(mPresetIntent);
				Toast.makeText(mContext, "Preset: " + preset, Toast.LENGTH_SHORT).show();
				updateUI(preset);
			}
        });
	}

	@Override
	protected void onStart() {
		super.onStart();
		if(mService == null){
			// Bind to LocalService
			Intent intent = new Intent(mContext, ServiceAphexDSP.class);
			//http://developer.android.com/guide/components/bound-services.html
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			CONSTANTS.DEBUG_LOG("Preset ONSTART SERVICE IS", "" +mService);
		}
	}
	

}
