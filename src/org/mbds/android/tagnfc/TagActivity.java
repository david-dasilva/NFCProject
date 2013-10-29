package org.mbds.android.tagnfc;

import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.os.Parcelable;

import java.io.IOException;

public class TagActivity extends Activity {

	public static final String TAG = "NFCReader";

	public static final int REQUEST_CODE = 1000;

	public static final String MESSAGE = "I'm a message!";

	public static final String PREFIX = "http://www.mbds-fr.org";

	NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;
	NdefMessage message = null;
	private IntentFilter ndefDetected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Ajout du listener sur le bouton share
		final Button btnShare = (Button) findViewById(R.id.share);
		btnShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				share();
			}
		});

		if(message == null)
		{
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (message != null) {
			// Créer le message à écrire

			NfcAdapter nfcAdapter = NfcAdapter
					.getDefaultAdapter(getApplicationContext());
			// Utilisation de la méthode crée précédemment :
			// On insère le domaine (URL) pour que le tag soit détecté
			// par cette appli en priorité (cf. manifeste)=> dans notre
			// exemple, nous n'utiliserons pas le type mime...
			// msg = createNdefMessage(PREFIX+message, "text/plain");
			// Passer le message Ndef, ainsi que l’activité en cours à
			// l’adaptateur :
			// Si un périphérique NFC est en proximité, le message sera envoyé
			// en mode passif
			// (ne fonctionne pas pour un tag passif)
			nfcAdapter.setNdefPushMessage(message, this);
		}

		try {
			if (nfcAdapter == null)
				nfcAdapter = NfcAdapter.getDefaultAdapter(this);
			/*
			 * pendingIntent = PendingIntent.getActivity(this, 0, new
			 * Intent(this,
			 * getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			 */
			// Intent filters
			ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
			Intent intent = getIntent();
			// Lecture/Ecriture...
			resolveIntent(intent);
		} catch (Exception e) {
			Log.d("TAGNFC", e.getStackTrace().toString());
		}

	}

	public void onPause() {
		super.onPause();
		/* nfcAdapter.disableForegroundDispatch(this); */
	}

	public void onNewIntent(Intent intent) {
		// MÃ©thode qui va traiter le contenu
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			resolveIntent(intent);
		}
	}

	public void resolveIntent(Intent intent) {
		// Infos sur le tag
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		byte[] id = tag.getId();
		String[] technologies = tag.getTechList();
		int content = tag.describeContents();
		Ndef ndef = Ndef.get(tag);
		boolean isWritable = ndef.isWritable();
		boolean canMakeReadOnly = ndef.canMakeReadOnly();

		// RÃ©cupÃ©ration des messages
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		NdefMessage[] msgs;
		if (rawMsgs != null) {
			msgs = new NdefMessage[rawMsgs.length];
			for (int i = 0; i < rawMsgs.length; i++) {
				msgs[i] = (NdefMessage) rawMsgs[i];
				NdefRecord record = msgs[i].getRecords()[i];
				byte[] idRec = record.getId();
				short tnf = record.getTnf();
				byte[] type = record.getType();
				String message = new String(record.getPayload());
				// Utiliser ?
				message = message.substring(13); // <---- sale

				Log.d("TAGNFC", "message = " + message);
				EditText txt = (EditText) findViewById(R.id.message);
				txt.setText(message);

				Log.d("TAGNFC", "Tag trouvÃ©!");
				Log.d("TAGNFC", "Test2");

				// Lancer le navigateur si type URI ?
				/*
				 * if (Arrays.equals(type, NdefRecord.RTD_URI)) { Uri uri =
				 * record.toUri(); Intent in = new Intent(Intent.ACTION_VIEW);
				 * in.setData(uri); startActivity(in); }
				 */
			}
		} else {
			// Tag de type inonnu
			byte[] empty = new byte[] {};
			NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty,
					empty, empty);
			NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
			msgs = new NdefMessage[] { msg };
		}
		// TODO : Afficher les informations...

		if (message != null) {
			Log.d("TAGNFC", "il y a un message stockÃ©");
			try {
				writeTag(message, tag);
			} catch (Exception e) {
				Log.e("TAGNFC", "erreur dans le writetag");
			}
		} else {
			Log.d("TAGNFC", "pas de message stockÃ©");
		}

	}

	public NdefMessage createNdefMessage(String text, String mimeType) {

		// Message de type URI
		NdefMessage msg = new NdefMessage(NdefRecord.createUri(Uri
				.encode("http://www.mbds-fr.org/" + text)));
		return msg;
	}

	public static boolean writeTag(final NdefMessage message, final Tag tag) {
		try {
			int size = message.toByteArray().length;
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					return false;
				}
				if (ndef.getMaxSize() < size) {
					return false;
				}
				ndef.writeNdefMessage(message);
				ndef.close();
				return true;
			} else {
				// Tags qui nÃ©cessitent un formatage :
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						// Formatage et Ã©criture du message:
						format.format(message);
						// ou en verrouillant le tag en Ã©criture :
						// formatable.formatReadOnly(message);
						format.close();
						return true;
					} catch (IOException e) {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tag, menu);
		return true;
	}

	public void Clear(View view) {

		EditText mEdit = (EditText) findViewById(R.id.message);
		mEdit.setText("");
	}

	public void share() {

		// rÃ©cuperation du texte entrÃ© par l'utilisateur
		EditText messageField = (EditText) findViewById(R.id.message);
		final String texte = messageField.getText().toString();
		message = createNdefMessage(texte, null);
		Log.d("TAGNFC", "message stockÃ© : " + texte);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
