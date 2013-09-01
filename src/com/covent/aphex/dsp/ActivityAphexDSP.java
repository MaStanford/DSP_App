/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.covent.aphex.dsp;

import com.covent.aphex.dsp.ServiceAphexDSP.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityAphexDSP extends Activity {
	//Log tag to be passed into DEBUG_LOG
    private static final String TAG = "ActivityAphexDSP";

    //EQ class
    //private Equalizer mEqualizer;

    //Views
    private LinearLayout mLinearLayout;
    private TextView mStatusTextView;
    
    //Toggle button
    private Button mToggleButton;
    
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


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        //Status view
        mStatusTextView = new TextView(this);
        
        //Toggle button
        mToggleButton = new Button(this);
        mToggleButton.setText("Toggle");
        
        //Toggle onClickListener
        mToggleButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(mService.getEnabled()){
					mService.setEnabled(!mService.getEnabled());
					Toast.makeText(getBaseContext(), "DSP OFF: " + mService.getEnabled(), Toast.LENGTH_SHORT).show();
					mStatusTextView.setText("Effect: " + mService.getEnabled());
				}
				else if(!mService.getEnabled()){
					mService.setEnabled(!mService.getEnabled());
					Toast.makeText(getBaseContext(), "DSP ON: " + mService.getEnabled(), Toast.LENGTH_SHORT).show();
					mStatusTextView.setText("Effect: " + mService.getEnabled());
				}
			}
        	
        });

        //inflate views
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.addView(mStatusTextView);
        mLinearLayout.addView(mToggleButton);
        setContentView(mLinearLayout);
        
        mContext = this;
        
    }
    
    @Override
	protected void onStart(){
		super.onStart();
		/****************************************
		 * SERVICES
		 ***************************************/
		if(mService == null){
			// Bind to LocalService
			Intent intent = new Intent(mContext, ServiceAphexDSP.class);
			startService(intent);
			//http://developer.android.com/guide/components/bound-services.html
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			CONSTANTS.DEBUG_LOG("ONSTART SERVICE IS", "" +mService);
		}
	}
}
