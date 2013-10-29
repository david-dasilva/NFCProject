package org.mbds.android.tagnfc;


import android.net.Uri;
import android.nfc.*;
import android.nfc.tech.*;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import java.io.IOException;


/**
 * Activité principale de notre application,
 * Elle reçoit les evenements NFC et décide quoi en faire.
 */
public class TagActivity extends FragmentActivity {

    public static final String TAG = "TAGNFC";
	public static final String PREFIX = "http://www.mbds-fr.org/";
    private static boolean writeMode = false;
	public static NdefMessage message = null;
    private static NfcDialogFragment dialog;
    NfcAdapter nfcAdapter;
    IntentFilter ndefDetected;

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

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
	}


    /**
     * Récupère le texte entré par l'utilisateur et affiche un dialogue
     */
    private void share(){
        writeMode = true;

        EditText messageField = (EditText) findViewById(R.id.message);
        final String texte = messageField.getText().toString();

        if(!texte.isEmpty()){
            message = createNdefMessage(texte);
            nfcAdapter.setNdefPushMessage(message, this);

            // Affichage du dialogue
            dialog = new NfcDialogFragment();
            FragmentManager fm = getSupportFragmentManager();
            dialog.show(fm, "fragment_nfc_dialog");
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
			// On insére le domaine (URL) pour que le tag soit détecté
			// par cette appli en priorité (cf. manifeste)=> dans notre
			// exemple, nous n'utiliserons pas le type mime...
			// msg = createNdefMessage(PREFIX+message, "text/plain");
			// Passer le message Ndef, ainsi que l'activité en cours à
			// l'adaptateur :
			// Si un périphérique NFC est en proximité, le message sera envoyé
			// en mode passif
			// (ne fonctionne pas pour un tag passif)
			//nfcAdapter.setNdefPushMessage(message, this);
		}

		try {
			if (nfcAdapter == null)
				nfcAdapter = NfcAdapter.getDefaultAdapter(this);

			// Intent filters
			ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
			Intent intent = getIntent();
			// Lecture/Ecriture...
			resolveIntent(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void resolveIntent(Intent intent) {

        Log.d(TAG, "Detection de tag!");
		// Infos sur le tag
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if(writeMode){
            // Mode écriture
            Log.d(TAG, "mode ecriture");

            try{
                writeTag(message, tag);
                writeMode = false;
                Toast toast = Toast.makeText(getApplicationContext(), R.string.tag_success, Toast.LENGTH_LONG);
                toast.show();
            } catch(Exception e){
                e.printStackTrace();
            }

        } else {
            // Mode lecture
            Log.d(TAG, "mode lecture");

            if (tag != null){
                TagNfc tagnfc = new TagNfc(intent);
                // Récupération des messages
                Parcelable[] rawMsgs = tagnfc.getRawMsgs();
                NdefMessage[] msgs;

                if (rawMsgs != null) {
                    // Il y a des messages!

                    msgs = new NdefMessage[rawMsgs.length];
                    for (int i = 0; i < rawMsgs.length; i++) {
                        msgs[i] = (NdefMessage) rawMsgs[i];
                        NdefRecord record = msgs[i].getRecords()[i];

                        // récupération du message sous forme de string
                        String message = "";
                        Uri uri = record.toUri();
                        // Suppression du préfix
                        message += uri.toString().replace(PREFIX,"")+" ";

                        /*
                         * Création d'un intent pour l'activité de lecture et passage des parametres
                         */
                        Intent readIntent = new Intent(getBaseContext(), ReaderActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("message", message);
                        bundle.putString("id", tagnfc.getId());
                        bundle.putString("technologies", tagnfc.getTechnologies());
                        bundle.putString("isWritable", tagnfc.isWritable());
                        bundle.putString("canMakeReadOnly", tagnfc.isCanMakeReadOnly());

                        readIntent.putExtras(bundle);
                        readIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        // Lancement de l'activité
                        startActivityForResult(readIntent, 0);
                        finish();
                    }
                }
            }
        }
	}


    /**
     * Crée un message Ndef en utilisant le prefix
     * @param text
     * @return
     */
	public NdefMessage createNdefMessage(String text) {

		// Message de type URI
		NdefMessage msg = new NdefMessage(NdefRecord.createUri(PREFIX + text));
		return msg;
	}


    /**
     * Ecriture du tag
     * @param message
     * @param tag
     * @return
     */
	public static boolean writeTag(NdefMessage message, Tag tag) {

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

                // On cache le dialogue demandant a l'utilisateur d'approcher un Tag
                dialog.dismiss();
				ndef.close();

                writeMode = false;
				return true;
			} else {
                Log.d(TAG, "Tag non formaté");
				// Tags qui nécessitent un formatage :
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						// Formatage et écriture du message:
						format.format(message);
						// ou en verrouillant le tag en écriture :
						// formatable.formatReadOnly(message);
						format.close();
                        writeMode = false;
						return true;
					} catch (IOException e) {
						return false;
					}
				} else {
                    writeMode = false;
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        writeMode = false;
        return false;
	}


    /**
     * Efface le champ texte au retour de l'activité de lecture
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "retour a l'activité principale");
        clear();
    }


    /**
     * Efface le champ texte
     */
	public void clear() {
        Log.d(TAG, "clear field");
		EditText mEdit = (EditText) findViewById(R.id.message);
		mEdit.setText("");
	}


    /**
     * Efface les données du tag, mais insere le préfix
     */
	public void resettag(View view)
	{
        writeMode = true;
        message = createNdefMessage("");
        nfcAdapter.setNdefPushMessage(message, this);

        // Affichage du dialogue
        dialog = new NfcDialogFragment();
        FragmentManager fm = getSupportFragmentManager();
        dialog.show(fm, "fragment_nfc_dialog");
		
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
