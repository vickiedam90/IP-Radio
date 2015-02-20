package chalmers.ip_radio.VoIP;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.util.Log;

/**
 * Created by Vivi on 2015-01-30.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    /**
     * Processes the incoming call, answers it, and hands it over to the
     * TalkActivity.
     *
     * @param context The context under which the receiver is running.
     * @param intent  The intent being received.
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        SipAudioCall incomingCall = null;
        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    try {
                        call.answerCall(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            TalkActivity talkActivity = (TalkActivity) context;
            incomingCall = talkActivity.manager.takeAudioCall(intent, listener);
            incomingCall.answerCall(3000);
            incomingCall.startAudio();
            incomingCall.setSpeakerMode(true);

            talkActivity.call = incomingCall;
            talkActivity.updateStatus(incomingCall, 1);
        } catch (SipException e) {
            if (incomingCall != null) {
                incomingCall.close();
                Log.d("end call", "incoming call");
            }
        }

    }
}