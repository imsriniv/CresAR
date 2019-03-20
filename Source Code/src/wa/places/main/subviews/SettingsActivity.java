package wa.places.main.subviews;

import wa.places.main.R;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener{

	private Preference listPref;
	private Preference listPref1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		listPref = (Preference) findPreference("listPref");
		listPref.setOnPreferenceChangeListener(this);
		listPref1 = (Preference) findPreference("listPref1");
		listPref1.setOnPreferenceChangeListener(this);
		
	}

	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		
		
		return true;
	}

	public boolean onPreferenceClick(Preference preference) {
		
		
		
		return true;
	}

	
	
}
