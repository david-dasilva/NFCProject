package org.mbds.android.tagnfc;

import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
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
import android.os.Parcelable;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;


public class TagActivity extends Activity {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    NdefMessage message = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


        // Ajout du listener sur le bouton share
        final Button btnShare = (Button)findViewById(R.id.share);
        btnShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

	}


    public void onResume() {
        super.onResume();
        /*nfcAdapter.enableForegroundDispatch(this,
                pendingIntent, null, null); // TODO verifier ça*/
    }


    public void onPause() {
        super.onPause();
        /*nfcAdapter.disableForegroundDispatch(this);*/
    }


    public void onNewIntent(Intent intent) {
        //Méthode qui va traiter le contenu
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            resolveIntent(intent) ;
        }
    }

    public void resolveIntent (Intent intent) {
        //Infos sur le tag
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        byte[] id =tag.getId();
        String[] technologies = tag.getTechList();
        int content = tag.describeContents();
        Ndef ndef = Ndef.get(tag);
        boolean isWritable = ndef.isWritable();
        boolean canMakeReadOnly = ndef.canMakeReadOnly();

        //Récupération des messages
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra( NfcAdapter.EXTRA_NDEF_MESSAGES);
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
                //Utiliser ?
                message = message.substring(13); // <---- sale


                Log.d("TAGNFC", "message = "+message);
                EditText txt = (EditText)findViewById(R.id.message);
                txt.setText(message);

                Log.d("TAGNFC", "Tag trouvé!");



                //Lancer le navigateur si type URI ?
                /*
                if (Arrays.equals(type, NdefRecord.RTD_URI)) {
                    Uri uri = record.toUri();
                    Intent in = new Intent(Intent.ACTION_VIEW);
                    in.setData(uri);
                    startActivity(in);
                }*/
            }
        } else {
            //Tag de type inonnu
            byte[] empty = new byte[] {};
            NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                            empty, empty, empty);
            NdefMessage msg = new NdefMessage(
                    new NdefRecord[] {record});
            msgs = new NdefMessage[] {msg};
        }
        //TODO : Afficher les informations...

        if(message != null){
            Log.d("TAGNFC", "il y a un message stocké");
            try{
                writeTag(message, tag);
            } catch (Exception e){
                Log.e("TAGNFC","erreur dans le writetag" );
            }
        } else {
            Log.d("TAGNFC", "pas de message stocké");
        }

    }



    public NdefMessage createNdefMessage(String text, String mimeType)
    {

        //Message de type URI
        NdefMessage msg = new NdefMessage(NdefRecord.createUri(Uri.encode(
                "http://www.mbds-fr.org/"+text))
        );
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
                //Tags qui nécessitent un formatage :
                NdefFormatable format =
                        NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        //Formatage et écriture du message:
                        format.format(message);
                        //ou en verrouillant le tag en écriture :
                        //formatable.formatReadOnly(message);
                        format.close() ;
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
		
		EditText mEdit   = (EditText)findViewById(R.id.message);
		mEdit.setText("");
	 }
	
	public void share() {

        // récuperation du texte entré par l'utilisateur
        EditText messageField = (EditText)findViewById(R.id.message);
        final String texte = messageField.getText().toString();
        message = createNdefMessage(texte, null);
        Log.d("TAGNFC","message stocké : "+texte);
	 }
	

}
