package chalmers.ip_radio.VoIP;


import android.os.Bundle;
import chalmers.ip_radio.R;
import android.preference.PreferenceActivity;
/**
 * Handles SIP authentication settings for the Walkie Talkie app.
 */
public class SipSettings extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
// Note that none of the preferences are actually defined here.
// They're all in the XML file res/xml/preferences.xml.
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}