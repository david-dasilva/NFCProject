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
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.os.Parcelable;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class TagActivity extends Activity implements SensorListener {

	public static final String TAG = "TAGNFC";
	public static final String MESSAGE = "I'm a message!";
	public static final String PREFIX = "http://www.mbds-fr.org/";
	private static boolean writeMode = false;

	// Gestion du shaker

	private static final int SHAKE_THRESHOLD = 2000;
	private SensorManager sensorMgr = null;
	
	long lastUpdate = 0;
	float last_x = 0;
	float last_y = 0;
	float last_z = 0;
	
	

	NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;
	public static NdefMessage message = null;
	private IntentFilter ndefDetected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER,
				SensorManager.SENSOR_DELAY_GAME);

		// Ajout du listener sur le bouton share
		final Button btnShare = (Button) findViewById(R.id.share);
		btnShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				share();

			}
		});

		// if(message == null)
		// {
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		// pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
		// getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// }

	}

	private void share() {
		writeMode = true;

		EditText messageField = (EditText) findViewById(R.id.message);
		final String texte = messageField.getText().toString();

		if (!texte.isEmpty()) {
			message = createNdefMessage(texte);

			nfcAdapter.setNdefPushMessage(message, this);
		}

		Log.d(TAG, "Click btn Share, message = " + message.toString());
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
			// Si un périphérique NFC est en proximité, le message sera
			// envoyé
			// en mode passif
			// (ne fonctionne pas pour un tag passif)
			// nfcAdapter.setNdefPushMessage(message, this);
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
			e.printStackTrace();
		}

	}

	public void onPause() {
		super.onPause();
		/* nfcAdapter.disableForegroundDispatch(this); */
	}

	public void onNewIntent(Intent intent) {
		// Méthode qui va traiter le contenu
		/*
		 * String action = intent.getAction(); if
		 * (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
		 * NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
		 * NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
		 * resolveIntent(intent); }
		 */
	}

	public void resolveIntent(Intent intent) {

		Log.d(TAG, "Detection de tag!");
		// Infos sur le tag
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		if (writeMode) {
			// Mode écriture
			Log.d(TAG, "mode ecriture");

<<<<<<< HEAD
			try {
				// ////
				writeTag(message, tag);
				writeMode = false;
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			// Mode lecture

			Log.d(TAG, "mode lecture");

			if (tag != null) {

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
						byte[] idRec = record.getId();
						short tnf = record.getTnf();
						byte[] type = record.getType();
=======
        if(writeMode){
            // Mode écriture
            Log.d(TAG, "mode ecriture");

            try{
                //////
                writeTag(message, tag);
                writeMode = false;
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
                        byte[] idRec = record.getId();
                        short tnf = record.getTnf();
                        byte[] type = record.getType();

                        // récupération du message sous forme de string
                        String message = "";//new String(record.getPayload());
                        Uri uri = record.toUri();
                        message += uri.toString().replace(PREFIX,"")+" ";
                        //message = message.substring(13); // <---- sale

                        Log.d(TAG, "message = " + message);

                        Intent readIntent = new Intent(getBaseContext(), ReaderActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("message", message);
                        bundle.putString("id", tagnfc.getId());
                        bundle.putString("technologies", tagnfc.getTechnologies());
                        bundle.putString("isWritable", tagnfc.isWritable());
                        bundle.putString("canMakeReadOnly", tagnfc.isCanMakeReadOnly());

                        readIntent.putExtras(bundle);
                        readIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        Log.d(TAG,"starting activity ReaderActivity");
                        startActivityForResult(readIntent,0);
                        finish();

                    }
                }
            }
        }
	}
>>>>>>> 2e9dc8098af85fe8e3f4c16778551722e964bb7d

						// récupération du message sous forme de string
						String message = "";// new String(record.getPayload());
						Uri uri = record.toUri();
						message += uri.toString().replace(PREFIX, "") + " ";
						// message = message.substring(13); // <---- sale

						Log.d(TAG, "message = " + message);

						Intent readIntent = new Intent(getBaseContext(),
								ReaderActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("message", message);
						bundle.putString("id", tagnfc.getId());
						bundle.putString("technologies",
								tagnfc.getTechnologies());
						bundle.putString("isWritable", tagnfc.isWritable());
						bundle.putString("canMakeReadOnly",
								tagnfc.isCanMakeReadOnly());

						readIntent.putExtras(bundle);
						readIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						Log.d(TAG, "starting activity ReaderActivity");
						startActivity(readIntent);
						finish();

					}
				}
			}
		}
	}

	public NdefMessage createNdefMessage(String text) {

		// Message de type URI
		NdefMessage msg = new NdefMessage(NdefRecord.createUri(PREFIX + text));
		return msg;
	}

	public static boolean writeTag(NdefMessage message, Tag tag) {
		try {

			Log.d(TAG, "debut writeTag");
			if (message == null) {
				Log.e(TAG, "message null");
			}
			int size = message.toByteArray().length;

			Log.d(TAG, "writeTag message = " + message.toString());

			Ndef ndef = Ndef.get(tag);

			Log.d(TAG, "writeTag tag =" + tag.getId());

			if (ndef != null) {
				ndef.connect();
				Log.d(TAG, "connected");
				if (!ndef.isWritable()) {
					return false;
				}
				if (ndef.getMaxSize() < size) {
					return false;
				}
				Log.d(TAG, "pre write");
				ndef.writeNdefMessage(message);
				Log.d(TAG, "post write");
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tag, menu);
		return true;
	}

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "retour a l'activité principale");
        clear(null);
    }

	public void clear(View view) {
        Log.d(TAG, "clear field");
		EditText mEdit = (EditText) findViewById(R.id.message);
		mEdit.setText("");
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

	@SuppressWarnings("deprecation")
	public void onSensorChanged(int sensor, float[] values) {
		if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			// only allow one update every 100ms.
			if (lastUpdate == 0)
				lastUpdate = curTime;
			if ((curTime - lastUpdate) > 100) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				float x = values[SensorManager.DATA_X];
				float y = values[SensorManager.DATA_Y];
				float z = values[SensorManager.DATA_Z];
				if (last_x == 0 && last_y == 0 && last_z == 0) {
					last_x = x;
					last_y = y;
					last_z = z;
				}
				float speed = Math.abs(x + y + z - last_x - last_y - last_z)
						/ diffTime * 10000;
				Log.d("TAGNFC", "shake:"+speed);
				if (speed > SHAKE_THRESHOLD) {
					EditText mEdit = (EditText) findViewById(R.id.message);
					mEdit.setText("");
				}
				last_x = x;
				last_y = y;
				last_z = z;
			}
		}
	}

	@Override
	public void onAccuracyChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

}
