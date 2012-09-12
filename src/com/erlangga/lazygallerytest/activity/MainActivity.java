package com.erlangga.lazygallerytest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * @author Erlangga
 *
 */

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button btnLaunchImageSelector = (Button) findViewById(R.id.btnLaunchImageSelector);
		btnLaunchImageSelector.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ImageSelector.class));
			}
		});
	}
}
