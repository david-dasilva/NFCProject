package org.mbds.android.tagnfc;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class TagActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tag, menu);
		return true;
	}
	
	public void Clear(View view) {
		
		EditText mEdit   = (EditText)findViewById(R.id.message);
		mEdit.setText("vide");
	     // Kabloey
	 }
	
	public void Share(View view) {
	     // Kabloey
		 // Dans le onClick du bouton "SHARE"
        final String message = "xxxxxx";// Récupérer le texte saisi par l'utilisateur
        Bundle bundle = new Bundle();
        bundle.putString(NFCReader.MESSAGE, message);
        Intent nfcReader = new Intent(getBaseContext(), NFCReader.class);
        nfcReader.putExtras(bundle);
        startActivity(nfcReader);
	 }
	

}
