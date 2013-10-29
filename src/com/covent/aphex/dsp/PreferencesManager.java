package com.covent.aphex.dsp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages the Shared Preferences.  It needs to be passed a context when you instantiate an object.
 * 
 * @author mStanford
 *
 */
public class PreferencesManager {

	SharedPreferences mSharedPreferences;
	//Need a context for the shared preferences
	Context mContext;
	
	/**
	 * Pass a context for the shared preferences.
	 * @param context
	 */
	PreferencesManager(Context context){
		mContext = context;
		loadPreferences();
	}
	
	/**
	 * Grabs reference to the shared preferences in private mode
	 * @author mStanford
	 */
	private void loadPreferences(){
		mSharedPreferences = mContext.getSharedPreferences(CONSTANTS.SHARED_PREF_KEY, Context.MODE_PRIVATE);
	}
	
	/**
	 * Checks to see if the preset is custom or not. 
	 * @return True is custom, False if not custom
	 * @author mStanford
	 */
	public boolean isCustom(){
		return mSharedPreferences.getBoolean(CONSTANTS.SHARED_PREF_CUSTOM, CONSTANTS.DEFAULT_IS_CUSTOM);
	}
	
	/**
	 * Sets the shared preference for whether or not the preset is custom
	 * @param mIsCustom Boolean for if it custom or not, True - Custom
	 */
	public void setIsCustom(boolean mIsCustom){
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		mEditor.putBoolean(CONSTANTS.SHARED_PREF_CUSTOM, mIsCustom);
		mEditor.commit();
	}
	
	/**
	 * Returns the preset number.  Returns -1 if custom.  
	 * @return The preset, -1 if Custom
	 */
	public short getPreset(){
		if(isCustom())
			return -1;
		return (short) mSharedPreferences.getInt(CONSTANTS.SHARED_PREF_PRESET_NUM, CONSTANTS.DEFAULT_GET_PRESET);
	}
	
	/**
	 * Sets the preset number.  Sets custom to false.  You cannot have a preset number
	 * and a custom preset at the same time.
	 * @param preset Is the preset number
	 * @author mStanford
	 */
	public void setPreset(short preset){
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		//set the preset
		mEditor.putInt(CONSTANTS.SHARED_PREF_PRESET_NUM, (int)preset);
		//set custom to false
		mEditor.putBoolean(CONSTANTS.SHARED_PREF_CUSTOM, false);
		mEditor.commit();
	}
}
