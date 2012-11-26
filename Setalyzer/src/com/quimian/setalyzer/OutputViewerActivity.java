package com.quimian.setalyzer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.Menu;
import android.widget.ImageView;

public class OutputViewerActivity extends Activity {

	BitmapDrawable image;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_output_viewer);
		
		Intent intent = getIntent();
		Bitmap bmp = BitmapFactory.decodeFile(intent.getStringExtra("bmpTemporaryFile"));
		this.setImage(bmp);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_output_viewer, menu);
		return true;
	}
	
	public void setImage(Bitmap bmp) {
		ImageView iv = (ImageView) findViewById(R.id.outputImageView);
		BitmapDrawable drawable = new BitmapDrawable(getApplicationContext().getResources(), bmp);
		this.image = drawable;
		iv.setImageDrawable(this.image);
	}
	
	

}
