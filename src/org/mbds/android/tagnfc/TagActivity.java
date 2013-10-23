package org.mbds.android.tagnfc;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
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
                Share();
            }
        });
		
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
