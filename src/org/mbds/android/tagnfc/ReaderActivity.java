package org.mbds.android.tagnfc;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by shepard on 29/10/13.
 */
public class ReaderActivity extends Activity {

    private IntentFilter ndefDetected;

    String message = "";
    String id= "";
    String technologies = "";
    String isWritable = "";
    String isLockable = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_layout);

        Bundle bundle = getIntent().getExtras();
        message = bundle.getString("message");
        id = bundle.getString("id");
        technologies = bundle.getString(technologies);
        isWritable = bundle.getString("isWritable");
        isLockable = bundle.getString("canMakeReadOnly");


        getActionBar().setDisplayHomeAsUpEnabled(true);

        if(!message.isEmpty()){

            // Affichage du message dans le textView
            TextView tvMessage = (TextView) findViewById(R.id.tvMessage);
            tvMessage.setText(message);

            TextView tvId = (TextView) findViewById(R.id.tvId);
            tvId.setText(id);

            TextView tvTech = (TextView) findViewById(R.id.tvTech);
            tvTech.setText(technologies);

            TextView tvEcriture = (TextView) findViewById(R.id.tvEcriture);
            tvEcriture.setText(isWritable);

            TextView tvLockable = (TextView) findViewById(R.id.tvLockable);
            tvLockable.setText(isLockable);
        }

    }



}

