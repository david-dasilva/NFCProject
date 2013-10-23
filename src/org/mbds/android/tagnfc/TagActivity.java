package org.mbds.android.tagnfc;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TagActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Ajout du listener sur le bouton share
        final Button btnShare = (Button)findViewById(R.id.share);
        btnShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
            	// Construct the data to write to the tag
            	// Should be of the form [relay/group]-[rid/gid]-[cmd]
            	String nfcMessage = NFCReader.MESSAGE+"/toto";
            	 
            	// When an NFC tag comes into range, call the main activity which handles writing the data to the tag
            	NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getBaseContext());
            	 
            	Intent nfcIntent = new Intent(getBaseContext(), TagActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            	nfcIntent.putExtra("nfcMessage", nfcMessage);
            	PendingIntent pi = PendingIntent.getActivity(getBaseContext(), 0, nfcIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            	IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);  
            	 
            	nfcAdapter.enableForegroundDispatch((Activity)getBaseContext(), pi, new IntentFilter[] {tagDetected}, null);
           
            }
        });
	}
	
	public void onNewIntent(Intent intent) {
	    // When an NFC tag is being written, call the write tag function when an intent is
	    // received that says the tag is within range of the device and ready to be written to
	    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    String nfcMessage = intent.getStringExtra("nfcMessage");
	 
	    if(nfcMessage != null) {
	        NFCReader.writeTag(this, tag, nfcMessage);
	    }
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tag, menu);
		return true;
	}
	
	public void Clear(View view) {
		
		EditText mEdit   = (EditText)findViewById(R.id.message);
		mEdit.setText("");
	 }
	
	public void Share() {

        // récuperation du texte entré par l'utilisateur
        EditText messageField = (EditText)findViewById(R.id.message);
        final String message = messageField.getText().toString();

        Bundle bundle = new Bundle();
        bundle.putString(NFCReader.MESSAGE, message);
        Intent nfcReader = new Intent(getBaseContext(), NFCReader.class);
        nfcReader.putExtras(bundle);
        startActivity(nfcReader);
	 }
	

}
