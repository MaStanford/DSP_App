package com.covent.aphex.dsp;

import android.util.Log;

public class CONSTANTS {
	
	//Debug, set true to have log output and debug output
	static boolean DEBUG = false;

	public static int bands = 6; 
	
	//Notification parameters
	static final int SERVICE_ID = 2000;
	static final int REQUEST_CODE = 1;
	
	//EQ priority
	static final int EQ_PRIORITY = 100000000;
	
	//Keys for broadcast receivers.
	static final String ACTION_TOGGLE = "covent.action.toggle";
	static final String ACTIVITY_TOGGLE = "covent.activity.toggle";
	static final String ACTIVITY_TOGGLE_TRUE = "covent.activity.toggle.true";
	static final String ACTIVITY_TOGGLE_FALSE = "covent.activity.toggle.false";
	static final String PRESET_ACTION = "covent.preset.action";
	static final String PRESET_EXTRA = "covent.preset.extra";
	static final String TOGGLE_KEY = "toggle_key";
	static final String ACTION_PRESET_COMPLETED = "covent.action.preset.complete";
	
	//Keys for shared preferences
	static final String SHARED_PREF_KEY = "covent.shared.key";
	static final String SHARED_PREF_CUSTOM = "covent.shared.custom";
	static final String SHARED_PREF_PRESET_NUM = "covent.shared.preset.num";
	
	//Shared prefs defaults
	public static final boolean DEFAULT_IS_CUSTOM = false;
	public static final int DEFAULT_GET_PRESET = 0;
	
	//Array of setting headers
	static final String[] bandName = {"High Tune", "Harmonics", "High Mix",
						"Low Tune", "Drive", "Low Mix"};
	
	//Log output using debug logic
	static void DEBUG_LOG(String tag,String msg){
		if(DEBUG)
			Log.d(tag,msg);
	}

}
