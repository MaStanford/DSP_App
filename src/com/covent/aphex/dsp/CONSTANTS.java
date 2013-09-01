package com.covent.aphex.dsp;

import android.util.Log;

public class CONSTANTS {
	
	public static boolean DEBUG = true; 
	
	public static void DEBUG_LOG(String tag,String msg){
		if(DEBUG)
			Log.d(tag,msg);
	}

}
