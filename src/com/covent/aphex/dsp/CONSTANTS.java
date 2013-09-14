package com.covent.aphex.dsp;

import android.util.Log;

public class CONSTANTS {
	
	static boolean DEBUG = false; 
	static final String ACTION_TOGGLE = "covent.action.toggle";
	static final int SERVICE_ID = 2000;
	static final int REQUEST_CODE = 1;
	static final String ACTIVITY_TOGGLE = "covent.activity.toggle";
	static final String ACTIVITY_TOGGLE_TRUE = "covent.activity.toggle.true";
	static final String ACTIVITY_TOGGLE_FALSE = "covent.activity.toggle.false";
	static final int EQ_PRIORITY = 1000;
	static final String PRESET_ACTION = "covent.preset.action";
	static final String PRESET_EXTRA = "covent.preset.extra";
	static String TOGGLE_KEY = "toggle_key";
	//Array of setting headers
	static final String[] bandName = {"High Tune", "Harmonics", "High Mix",
						"Low Tune", "Drive", "Low Mix"};
	
	static void DEBUG_LOG(String tag,String msg){
		if(DEBUG)
			Log.d(tag,msg);
	}

}
