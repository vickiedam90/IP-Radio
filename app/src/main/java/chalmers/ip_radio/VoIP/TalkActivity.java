package chalmers.ip_radio.VoIP;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.os.Bundle;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.ParseException;

import chalmers.ip_radio.R;

/**
 * Created by Vivi on 2015-01-30.
 */
public class TalkActivity extends ActionBarActivity{ //implements View.OnTouchListener {
    public SipManager manager = null;
    public SipProfile profile = null;
    public String sipAddress = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver;
    private String username = "vivi";
  private String password = "FewJkWMpUGpBmnv4";
 //   private String username = "magkal";
 //   private String username = "vicki";
  //  private String password = "1234";
 //   private String password = "sRvduQbzKuxVJovL";
//    private String domain = "192.168.38.100";
    private String domain = "getonsip.com";
    private String outProxy = "sip.onsip.com";
//       private String username = "xxx123";
//       private String password = "mewmew";
//       private String domain = "sip2sip.info";
//       private String outProxy = "proxy.sipthor.net";

    private boolean waitingCall = false;

 /*   private static final int CALL_ADDRESS = 1;
    private static final int SET_AUTH_INFO = 2;
    private static final int UPDATE_SETTINGS_DIALOG = 3;
    private static final int HANG_UP = 4;
*/
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        //ToggleButton pushToTalkButton = (ToggleButton) findViewById(R.id.pushToTalk);
        //Button hangUp = (Button) findViewById(R.id.hangUp);

       // pushToTalkButton.setOnTouchListener(this);

        // Set up the intent filter. This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);

        //to keep the screen constantly on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
       // ActionBar actionBar = getActionBar();
        initManager();
        getExtra();
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
        Log.d("Initmanager","initmanager");
        initProfile();

    }

    /**
     * Logs you into your SIP provider, registering this device as the location
     * to send SIP calls to for your SIP address.
     */
    public void initProfile(){
        if (manager == null) {
            Log.d("Manager", "manager is null");
            return;
        }
        Log.d("profile", profile + " get velus");
        if (profile != null) {
            Log.d("call closeLocalProfile", "call closeLocalProfile");
            closeLocalProfile();
        }

        if(username.equals("") || domain.equals("") ||password.equals("") || outProxy.equals("")){
            Log.d("","username or domain or pass or proxy is empty");
            getPrompt();
            return;
        }
        try {
            Log.d("SipProfile.Builder", "SipProfile.Builder");

            //Creating SipProfile
            SipProfile.Builder builder = new SipProfile.Builder(username,
                    domain);
            builder.setPassword(password);
       //     builder.setOutboundProxy(outProxy);
            builder.setDisplayName(username);
            builder.setAuthUserName("getonsip_" + username);
            builder.setAutoRegistration(true);
            //builder.setSendKeepAlive(true);
            profile = builder.build();
            Log.v("profile", profile + " get velus");
            Log.d("profile", profile + " get velus");

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
                        public void onRegistrationDone(String localProfileUri, long expiryTime) {
                            updateStatus("Ready");
                            if(waitingCall)
                                setReceiver(toCall);
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
                   // call.toggleMute();
                    updateStatus(call, 2);
                }
                @Override
                public void onCallEnded(SipAudioCall call) {
             //       try {
             //           call.endCall();
                        updateStatus("Call End"); //can be replaced
             //       } catch (SipException e) {
             //           e.printStackTrace();
             //       }


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
                 labelView.setTextColor(Color.WHITE);
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
     //   MenuInflater inflater = getMenuInflater();
     //   inflater.inflate(R.menu.sip_activity_settings, menu);
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);

      /* menu.add(0, CALL_ADDRESS, 0, "Call someone");
        menu.add(0, SET_AUTH_INFO, 0, "Edit your SIP Info.");
        menu.add(0, HANG_UP, 0, "End Current Call.");*/
        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sipsetting_compose:
                Log.d("","onOptionsItemSelected");
                getPrompt();
                break;
            case R.id.call_compose:
                setReceiver("");
                break;

            /*case CALL_ADDRESS:
                setReceiver("");
                break;
          /*  case SET_AUTH_INFO:
                getPrompt();
                break;
            case HANG_UP:
                endCall();
                break; */
        }
        return true;
    }

    public void setReceiver(String receiverAddr){
        if(receiverAddr.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Call address");
            // Set up the input
            final EditText addr = new EditText(this);
            addr.setHint("Call address");
            addr.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
            builder.setView(addr);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sipAddress = addr.getText().toString();
                    initCall();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
        else{
            sipAddress = receiverAddr;
            initCall();
        }
    }

    private void getPrompt(){
        // get prompts.xml view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sip Settings");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Set up the input
        final EditText username_input = new EditText(this);
        final EditText domain_input = new EditText(this);
        final EditText password_input = new EditText(this);
        final EditText outBoundProxy_input = new EditText(this);
        layout.addView(username_input);
        layout.addView(password_input);
        layout.addView(domain_input);
        layout.addView(outBoundProxy_input);

        username_input.setHint("username");
        password_input.setHint("password");
        domain_input.setHint("domain");
        outBoundProxy_input.setHint("outBoundProxy");
        username_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        domain_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        password_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        outBoundProxy_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        builder.setView(layout);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username = username_input.getText().toString();
                domain = domain_input.getText().toString();
                password = password_input.getText().toString();
                outProxy = outBoundProxy_input.getText().toString();
                initManager();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

        }

    public void endCall(View view){
        if(call != null) {
            try {
                call.endCall();
            } catch (SipException se) {
                Log.d("WalkieTalkieActivity/onOptionsItemSelected",
                        "Error ending call.", se);
            }
            call.close();
            updateStatus("Call ended");
        }
        else
            updateStatus("No call ongoing");
    }

    /**
     * Updates whether or not the user's voice is muted, depending on whether the button is pressed.
     * @param v The View where the touch event is being fired.
     * @param event The motion to act on.
     * @return boolean Returns false to indicate that the parent view should handle the touch event
     * as it normally would.
     */
  /*  @Override
    public boolean onTouch(View v, MotionEvent event) {
       /* if (call == null) {
            return false;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN && call != null && call.isMuted()) {
            call.toggleMute();
        } else if (event.getAction() == MotionEvent.ACTION_UP && !call.isMuted()) {
            call.toggleMute();
        }
        return false;
    }*/

    private void getExtra(){

        Intent i = getIntent();
        Bundle b = i.getExtras();

        if(b != null){
            setReceiver((String) b.get("STRING_I_NEED"));
            waiting
        }

        /*
        String newString ="";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                newString = null;
            } else {
                newString = extras.getString("STRING_I_NEED");
                setReceiver(newString);
            }
        } else {
            newString = (String) savedInstanceState.getSerializable("STRING_I_NEED");
            setReceiver(newString);
        }
        */
    }
}
