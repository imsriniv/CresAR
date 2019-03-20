package wa.places.main.subviews;

import wa.places.main.R;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends Activity {
	TextView helpText;
protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helpview);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		helpText = (TextView) findViewById(R.id.textView1);
		helpText.setText("CresAR is an Augmented Reality driven College guide developed exclusively for Crescent Engineering College.\n\nCresAr needs GPS to be enabled and CresAr does not use your data pack.\n\nThe first time you open the application it takes some time as the device has to communicate with GPS Satellites.\n\nHold the phone still pointing to the place you want to get information about for atleast 3 to 5 seconds.\n\nClick on the places displayed to get description about them.\n\nClick menu->settings and you can change Location Radius to get better results.\n\n				Thanks for using CresAR!!");
		helpText.setTextSize((float) 14);
		helpText.setTypeface(null,Typeface.BOLD);
		
}
public void onBackPressed() {
	
	super.onBackPressed();
}
protected void onDestroy() {
	
	super.onDestroy();
}

}
