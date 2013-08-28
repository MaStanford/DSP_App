package com.covent.aphex.aphexstreaming;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Bundle;
import android.os.Looper;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class ActivityMP3Streaming extends Activity {

	private static final String LOG_TAG = "STREAM";
	private String address = "http://ia600200.us.archive.org/1/items/testmp3testfile/mpthreetest.mp3";
	private String fileAddress = "/Stream/";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_mp3_streaming);
		/*
		 * Making thread so I can run network operations
		 * Also, need to call looper.prepare() to be able
		 * to call toast, which uses a handler.
		 */
		new Thread(){
			@Override
			public void run() {
				Looper.prepare();
				URL url = resolveURL(address);
				InputStream inputStream = resolveInputStream(url);
				DSPWrapper(inputStream);
				File outputSource = new File(fileAddress);
				FileOutputStream fileOutputStream = createFileOutputStream(outputSource);
				writeToFile(inputStream,fileOutputStream);
				closeStream(fileOutputStream);
			}
		}.start();

	}



	/*
	 * Grab the URL
	 */
	public URL resolveURL(String address){
		URL url = null;

		try {
			url = new URL(address);
			Toast.makeText(getApplicationContext(), "URL: " + url.getPath(), 
					Toast.LENGTH_SHORT).show();
		} catch (MalformedURLException e) {
			Toast.makeText(getApplicationContext(), "URL bad", 
					Toast.LENGTH_SHORT).show();
		}

		return url;
	}

	/*
	 * Grab the input stream from the URL
	 */
	public InputStream resolveInputStream(URL url){

		InputStream inputStream = null;

		try {
			inputStream = url.openStream();
			Toast.makeText(getApplicationContext(), "Input Stream not opened", 
					Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "Input Stream opened", 
					Toast.LENGTH_SHORT).show();
		}

		Log.d(LOG_TAG, "url.openStream()");

		return inputStream;
	}

	/*
	 * Open an output stream
	 */
	public FileOutputStream createFileOutputStream(File file){

		FileOutputStream fileOutputStream = null;

		try {
			fileOutputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(LOG_TAG, "FileOutputStream: " + file);
		return fileOutputStream;
	}

	/*
	 * Writes to the file
	 */
	public void writeToFile(InputStream inputStream, FileOutputStream fileOutputStream){
		/*
		 * Writes the input byte stream to the file
		 */
		int c;
		try {
			while ((c = inputStream.read()) != -1) {
				int bytesRead = 0;
				Log.d(LOG_TAG, "bytesRead=" + bytesRead);
				fileOutputStream.write(c);
				bytesRead++;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Here ill send the bytes to the native code which will then breka it down into samples
	 * and send it to the aphex DSP.
	 */
	private void DSPWrapper(InputStream inputStream){
		int c;
		try {
			while ((c = inputStream.read()) != -1) {
				int bytesRead = 0;
				Log.d(LOG_TAG, "bytesRead=" + bytesRead);
				nativeParseBytesToSamples(c);
				bytesRead++;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void nativeParseBytesToSamples(int c) {
		// TODO Auto-generated method stub
	}



	/*
	 * Closes the output stream
	 */
	public void closeStream(FileOutputStream fileOutputStream){
		try {
			fileOutputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_mp3_streaming, menu);
		return true;
	}
}