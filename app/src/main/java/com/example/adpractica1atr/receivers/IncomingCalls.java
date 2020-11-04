package com.example.adpractica1atr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.adpractica1atr.Call;
import com.example.adpractica1atr.CallSaver;
import com.example.adpractica1atr.MainActivity;

import java.util.GregorianCalendar;

public class IncomingCalls extends BroadcastReceiver {

    private final String TAG=this.getClass().toString();
    private boolean notRegistered=true;
    //I have tested this class, and I have noticed that several times onReceive runs more than once,
    //even with conditionals filtering the state RINGING. With this boolean notRegistered, I make sure
    //that the call is registered only once.

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING) && notRegistered) {

            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    super.onCallStateChanged(state, incomingNumber);
                    if (incomingNumber == null || incomingNumber.equals("")) return; //incomingNumber could be null if readCallLog permissions are not granted
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        if(notRegistered) {
                            notRegistered=false;
                            Log.v("xyzyx ", " incoming: " + incomingNumber);
                            Call call = new Call(incomingNumber, new GregorianCalendar());
                            CallSaver saver = new CallSaver(context, call);
                            saver.start();
                        }
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
    //String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
    //I could have got the incoming number with that line, and not registering a PhoneStateListener
    //but I prefer not to use deprecated methods or constants (EXTRA_INCOMING_NUMBER)
}