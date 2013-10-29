package org.mbds.android.tagnfc;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;

/**
 * Created by shepard on 29/10/13.
 */
public class ReaderActivity extends Activity {

    private IntentFilter ndefDetected;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_layout);
    }


    public void onResume(){

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        // Intent filters
        ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        Intent intent = getIntent();
        // Lecture/Ecriture...
        resolveIntent(intent);
    }



}

