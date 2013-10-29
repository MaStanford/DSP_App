package com.covent.aphex.dsp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ActivityCredits extends Activity {
	
	ImageView mImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.credits_activity);
		
		mImageView = (ImageView) findViewById(R.id.imageView1);
		
		/**
		 * I'm making the screen just be one big onclick listener.  This is just for now until
		 * I get the art resources.  I was give a full image that the page should look like
		 * but I wasnt given individual art resources so I just set the whole page as an imageview
		 */
		mImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
