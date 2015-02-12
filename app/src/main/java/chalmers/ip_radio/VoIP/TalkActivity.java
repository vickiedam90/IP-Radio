package chalmers.ip_radio.VoIP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.os.Bundle;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.ParseException;

import chalmers.ip_radio.R;

/**
 * Created by Vivi on 2015-01-30.
 */
public class TalkActivity extends FragmentActivity implements View.OnTouchListener {
    public SipManager manager = null;
    public SipProfile profile = null;
    public String sipAddress = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver;

    private static final int CALL_ADDRESS = 1;
    private static final int SET_AUTH_INFO = 2;
    private static final int UPDATE_SETTINGS_DIALOG = 3;
    private static final int HANG_UP = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        ToggleButton pushToTalkButton = (ToggleButton) findViewById(R.id.pushToTalk);
        pushToTalkButton.setOnTouchListener(this);

        // Set up the intent filter. This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);

        //to keep the screen constantly on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initManager();
    }

    @Override
    public void onStart() {
        super.onStart();
    // When we get back from the preference setting Activity, assume
    // settings have changed, and re-login with new auth info.
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
            showDialog(UPDATE_SETTINGS_DIALOG);
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
    public void initCall() {
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
                    call.toggleMute();
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
                 TextView labelView = (TextView) findViewById(R.id.sipLabel);
                 labelView.setText(status);
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

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, CALL_ADDRESS, 0, "Call someone");
        menu.add(0, SET_AUTH_INFO, 0, "Edit your SIP Info.");
        menu.add(0, HANG_UP, 0, "End Current Call.");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CALL_ADDRESS:
                showDialog(CALL_ADDRESS);
                break;
            case SET_AUTH_INFO:
                updatePreferences();
                break;
            case HANG_UP:
                if(call != null) {
                    try {
                        call.endCall();
                    } catch (SipException se) {
                        Log.d("WalkieTalkieActivity/onOptionsItemSelected",
                                "Error ending call.", se);
                    }
                    call.close();
                }
                break;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CALL_ADDRESS:
                LayoutInflater factory = LayoutInflater.from(this);
                final View textBoxView = factory.inflate(R.layout.call_address_dialog, null);
                return new AlertDialog.Builder(this)
                        .setTitle("Call Someone.")
                        .setView(textBoxView)
                        .setPositiveButton(
                                android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        EditText textField = (EditText)
                                                (textBoxView.findViewById(R.id.calladdress_edit));
                                        sipAddress = textField.getText().toString();
                                        initCall();
                                    }
                                })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
// Noop.
                                    }
                                })
                        .create();
            case UPDATE_SETTINGS_DIALOG:
                return new AlertDialog.Builder(this)
                        .setMessage("Please update your SIP Account Settings.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                updatePreferences();
                            }
                        })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
// Noop.
                                    }
                                })
                        .create();
        }
        return null;
    }

    public void updatePreferences() {
        Intent settingsActivity = new Intent(getBaseContext(),
                SipSettings.class);
        startActivity(settingsActivity);
    }

    /**
     * Updates whether or not the user's voice is muted, depending on whether the button is pressed.
     * @param v The View where the touch event is being fired.
     * @param event The motion to act on.
     * @return boolean Returns false to indicate that the parent view should handle the touch event
     * as it normally would.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (call == null) {
            return false;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN && call != null && call.isMuted()) {
            call.toggleMute();
        } else if (event.getAction() == MotionEvent.ACTION_UP && !call.isMuted()) {
            call.toggleMute();
        }
        return false;
    }
}
