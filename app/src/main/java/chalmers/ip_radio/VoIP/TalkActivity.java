package chalmers.ip_radio.VoIP;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.ParseException;

import chalmers.ip_radio.R;

/**
 * Created by Vivi on 2015-01-30.
 */
public class TalkActivity extends Activity {
    public SipManager manager = null;
    public SipProfile profile = null;
    public String sipAddress = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);

        //to keep the screen constantly on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.close();
        }
        closeLocalProfile();
        if (callReceiver != null) {
            this.unregisterReceiver(callReceiver);
        }
    }

    /**
     * To create a new instance of SipManager
     */
    public void initManager(){
        if(manager == null)
            manager = SipManager.newInstance(this);
        initProfile();
    }

    /**
     * Logs you into your SIP provider, registering this device as the location
     * to send SIP calls to for your SIP address.
     */
    public void initProfile(){
        if (manager == null) {
            return;
        }
        Log.v("profile", profile + " get velus");
        if (profile != null) {
            Log.v("call closeLocalProfile", "call closeLocalProfile");
            closeLocalProfile();
        }

        //Store private primitive data in key-value pairs.
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        String username = prefs.getString("namePref", "");
        String domain = prefs.getString("domainPref", "");
        String password = prefs.getString("passPref", "");
        Log.v("username", username);
        Log.v("domain", domain);
        Log.v("password", password);

        if (username.length() == 0 || domain.length() == 0
                || password.length() == 0) {
            //popup to show a dialog? "UPDATE_SETTINGS_DIALOG = 3 in demo"
            //hardcode the name, pw and domain
            return;
        }
        try {
            // Log.v("SipProfile.Builder", "SipProfile.Builder");
            //Creating SipProfile
            SipProfile.Builder builder = new SipProfile.Builder(username,
                    domain);
            builder.setPassword(password);
            profile = builder.build();
            Log.v("profile", profile + " get velus");

            //The following code excerpt opens the local profile for making calls and/or receiving generic SIP calls.
            Intent i = new Intent();
            i.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i,
                    Intent.FILL_IN_DATA);
            manager.open(profile, pi, null);
            Log.v("intent call ", "intent call");
            // This listener must be added AFTER manager.open is called,
            // Otherwise the methods aren't guaranteed to fire.

            //This tracks whether the SipProfile was successfully registered with your SIP service provider
            manager.setRegistrationListener(profile.getUriString(),
                    new SipRegistrationListener() {
                        public void onRegistering(String localProfileUri) {
                            updateStatus("Registering with SIP Server...");
                        }
                        public void onRegistrationDone(String localProfileUri,
                                                       long expiryTime) {
                            updateStatus("Ready");
                        }
                        public void onRegistrationFailed(
                                String localProfileUri, int errorCode,
                                String errorMessage) {
                            updateStatus("Registration failed. Please check settings.");
                        }
                    });
        } catch (ParseException pe) {
            updateStatus("Connection Error.");
        } catch (SipException se) {
            updateStatus("Connection error.");
        }

    }

    /**
     * Closes out your local profile, freeing associated objects into memory and
     * unregistering your device from the server.
     */
    public void closeLocalProfile() {
        Log.v("closeLocalProfile manager", manager + " closeLocalProfile");
        if (manager == null) {
            return;
        }
        try {
            Log.v("closeLocalProfile profile", profile + " get velus");
            if (profile != null) {
                Log.v("close profile URI ", profile.getUriString() + " get velus");
                manager.close(profile.getUriString());
            }
        } catch (Exception ee) {
            Log.d("TalkActivity/onDestroy",
                    "Failed to close local profile.", ee);
        }
    }

    /**
     * Make an outgoing call.
     */
    public void initCall(View view) {
        updateStatus(sipAddress);
        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners. Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is
                // established.
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(true);
                    // if (call.isMuted()) {
                    // call.toggleMute();
                    // }
                    // call.toggleMute();
                    updateStatus(call, 2);
                }
                @Override
                public void onCallEnded(SipAudioCall call) {
                    updateStatus("Call End"); //can be replaced
                    //
                }
            };
            call = manager.makeAudioCall(profile.getUriString(), sipAddress,
                    listener, 30);
        } catch (Exception e) {
            Log.i("TalkActivity/InitCall",
                    "Error when trying to close manager.", e);
            if (profile != null) {
                try {
                    manager.close(profile.getUriString());
                } catch (Exception ee) {
                    Log.i("Talk/InitCall",
                            "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close(); //change to invite another person ?
            }
        }
    }


    /**
     * Updates the status box at the top of the UI with a messege of your
     * choice.
     *
     * @param status
     * The String to display in the status box.
     */
    public void updateStatus(final String status) {
// Be a good citizen. Make sure UI changes fire on the UI thread.
        this.runOnUiThread(new Runnable() {
            public void run() {
                // TextView labelView = (TextView) findViewById(R.id.sipLabel);
                // labelView.setText(status);
            }
        });
    }

    /**
     * Updates the status box with the SIP address of the current call.
     *
     * @param call
     * The current, active call.
     */
    public void updateStatus(SipAudioCall call, int val) {
        String useName = call.getPeerProfile().getDisplayName();
        if (useName == null) {
            useName = call.getPeerProfile().getUserName();
        }
        if (val == 1) {
            updateStatus("Recieve call :- " + useName + "@"
                    + call.getPeerProfile().getSipDomain());
        } else {
            updateStatus("Dial call :- " + useName + "@"
                    + call.getPeerProfile().getSipDomain());
        }
    }
}
