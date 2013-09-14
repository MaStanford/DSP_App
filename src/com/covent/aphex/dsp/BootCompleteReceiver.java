package com.covent.aphex.dsp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * http://stackoverflow.com/questions/2784441/trying-to-start-a-service-on-boot-on-android
 * http://stackoverflow.com/questions/16671619/android-intentservice-wont-start-on-boot
 * @author mStanford
 *
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		CONSTANTS.DEBUG_LOG("RECIEVBER", "REVCRER HIT");
        Intent mIntent = new Intent(context,ServiceAphexDSP.class);
        mIntent.setClass(context, ServiceAphexDSP.class);
        context.startService(mIntent);
	}
}
