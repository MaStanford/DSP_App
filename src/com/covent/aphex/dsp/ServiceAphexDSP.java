package com.covent.aphex.dsp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.audiofx.Equalizer;
import android.os.Binder;
import android.os.IBinder;

public class ServiceAphexDSP extends Service {
	
	//Needed for the start foreground, which makes the notification sticky in the foreground
	private static final int SERVICE_ID = 0;

	//The EQ
	Equalizer mEqualizer;
	
	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();
		
		mEqualizer = new Equalizer(100, 0);
        mEqualizer.setEnabled(true);
        
        setNotification();
	}
	
	

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		setNotification();
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//release EQ
		mEqualizer.release();
		//release the notification bar
		stopForeground(true);
	}
	
	public void setNotification(){
		
		Notification mNotification=new Notification(R.drawable.ic_launcher_aphex, "Aphex DSP", System.currentTimeMillis());
        Intent mIntenti=new Intent(this, ActivityAphexDSP.class);

        mIntenti.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi=PendingIntent.getActivity(this, 0, mIntenti, 0);

        mNotification.setLatestEventInfo(this, "Aphex DSP","DSP Enabled: " + getEnabled(), pi);
        mNotification.flags|=Notification.FLAG_NO_CLEAR;
 
        startForeground(SERVICE_ID, mNotification);
	}
	
	
	/*
	 * SETTERS AND GETTERS 
	 */
	
	public boolean setEnabled(boolean enabled){
		mEqualizer.setEnabled(enabled);
		setNotification();
		return mEqualizer.getEnabled();
	}
	
	public boolean getEnabled(){
		return mEqualizer.getEnabled();
	}
	
}
