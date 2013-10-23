package org.mbds.android.tagnfc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 */
public class NFCReader extends Activity {

	public static final String TAG = "NFCReader";

	public static final int REQUEST_CODE = 1000;

	public static final String MESSAGE = "I'm a message";

	public static final String PREFIX = "http://www.mbds-fr.org";
	
	
	private PendingIntent mPendingIntent;
	private IntentFilter ndefDetected;
	private ProgressDialog mDialog;
	private String message = "";

	private Long progressTime = Long.valueOf(500);
	/*
	 * AsyncTask while reading the tag. When finished, launches target intent
	 */
//	public class SyncTask extends AsyncTask<String, Integer, Boolean> {
//		SyncTask() {
//			LinearLayout l = (LinearLayout) findViewById(R.id.nfc_searching);
//			l.setVisibility(View.VISIBLE);
//			mDialog = new ProgressDialog(NFCReader.this);
//			mDialog.setMax(progressTime.intValue());
//			mDialog.setMessage("Reading NFC Tag...");
//			mDialog.setIndeterminate(false);
//			mDialog.setCancelable(true);
//			mDialog.show();
//		}
//		@Override
//		public synchronized Boolean doInBackground(String... args) {
//			for (int i = 0; i < progressTime.intValue(); i++) {
//				try {
//					Thread.sleep(Long.valueOf(10));
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				publishProgress((int) ((i / (float) progressTime.longValue())));
//			}
//			return true;
//		}
//		@Override
//		protected void onProgressUpdate(Integer... values) {
//			mDialog.setProgress(values[0]);
//		}
//		@Override
//		protected void onPostExecute(Boolean result) {
//			try {
//				LinearLayout l = (LinearLayout) findViewById(R.id.nfc_searching);
//				l.setVisibility(View.GONE);
//				setResult(RESULT_OK);
//				mDialog.dismiss();
//			} catch (Exception e) {
//				System.out.println(e.getClass().toString());
//			}
//		}
//	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setTitle(TAG);
		setContentView(R.layout.nfcreading);
		

        // Dans le onCreate de l'activité
        try {
            Bundle bundle = this.getIntent().getExtras();
            message = bundle.getString(MESSAGE);
        } catch (Exception e) {
            // pas de message :)
        }
	}

	@SuppressLint("NewApi")
	boolean resolveIntent(Intent intent) {

			 //Infos sur le tag
			Tag  tag = intent.getParcelableExtra(
				    NfcAdapter.EXTRA_TAG);
			byte[] id =tag.getId();
			String[] technologies = tag.getTechList();
			int content = tag.describeContents();
			Ndef ndef = Ndef.get(tag);
			boolean isWritable = ndef.isWritable();
			boolean canMakeReadOnly = ndef.canMakeReadOnly();

			 //Récupération des messages
			Parcelable[] rawMsgs = 
		                            intent.getParcelableArrayExtra(
		                                         NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage[] msgs;
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];

				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
					NdefRecord record = msgs[i].getRecords()[i];
					//byte[] idRec = record.getId();
					//short tnf = record.getTnf();
					//byte[] type = record.getType();
					String message = new String(record.getPayload());

					//message = message.replace(PREFIX, "");
                    message = message.substring(13); // <---- sale


                    Log.d("TAGNFC", "message = "+message);
					TextView textV = (TextView)findViewById(R.id.txtTag);
					textV.setText(message);					
					
					Log.d("TAGNFC", "Tag trouvé!");
					 //Utiliser ?
					//...

					//Lancer le navigateur si type URI ?
		  			/*if (Arrays.equals(type, NdefRecord.RTD_URI)) {
		        				Uri uri = record.toUri();
		        				Intent intentNew = new Intent(Intent.ACTION_VIEW);
		        				intentNew.setData(uri);
		        				startActivity(intentNew);
					}*/
				}
			} else {
				//Tag de type inonnu
				byte[] empty = new byte[] {};
				NdefRecord record = 
					new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
				NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
				msgs = new NdefMessage[] {msg};
			}
			//Afficher les informations...
			progressTime = 0l;
			return true;
		}
	@Override
	public void onResume() {
		super.onResume();
		// Waiting for the reading
//		SyncTask progress = (SyncTask) new SyncTask().execute();
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// Intent filters 
		ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefDetected.addDataType("text/plain");
		} catch (MalformedMimeTypeException e) {
		}

		((TextView) findViewById(R.id.text_nfc_reading))
				.setText("Processing...");
		Intent intent = getIntent();
		if (!resolveIntent(intent)) {
//			progress.cancel(true);
			// No data has been read
			Toast.makeText(getApplicationContext(), "NFC Reading has failed...", Toast.LENGTH_SHORT).show();
		}
		//finish();
	}

	@Override
	public void onPause() {
		super.onPause();
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
	
	public static boolean writeTag(Context context, Tag tag, String data) {     
	    // Record to launch Play Store if app is not installed
	    NdefRecord appRecord = NdefRecord.createApplicationRecord(context.getPackageName());
	 
	    // Record with actual data we care about
	    NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
	                                            new String("application/" + context.getPackageName()).getBytes(Charset.forName("US-ASCII")),
	                                            null, data.getBytes());
	 
	    // Complete NDEF message with both records
	    NdefMessage message = new NdefMessage(new NdefRecord[] {relayRecord, appRecord});
	 
	    try {
	        // If the tag is already formatted, just write the message to it
	        Ndef ndef = Ndef.get(tag);
	        if(ndef != null) {
	            ndef.connect();
	 
	            // Make sure the tag is writable
	            if(!ndef.isWritable()) {
	                //DialogUtils.displayErrorDialog(context, R.string.nfcReadOnlyErrorTitle, R.string.nfcReadOnlyError);
	                return false;
	            }
	 
	            // Check if there's enough space on the tag for the message
	            int size = message.toByteArray().length;
	            if(ndef.getMaxSize() < size) {
	                //DialogUtils.displayErrorDialog(context, R.string.nfcBadSpaceErrorTitle, R.string.nfcBadSpaceError);
	                return false;
	            }
	 
	            try {
	                // Write the data to the tag
	                ndef.writeNdefMessage(message);
	 
	                //DialogUtils.displayInfoDialog(context, R.string.nfcWrittenTitle, R.string.nfcWritten);
	                return true;
	            } catch (TagLostException tle) {
	                //DialogUtils.displayErrorDialog(context, R.string.nfcTagLostErrorTitle, R.string.nfcTagLostError);
	                return false;
	            } catch (IOException ioe) {
	                //DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
	                return false;
	            } catch (FormatException fe) {
	                //DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
	                return false;
	            }
	        // If the tag is not formatted, format it with the message
	        } else {
	            NdefFormatable format = NdefFormatable.get(tag);
	            if(format != null) {
	                try {
	                    format.connect();
	                    format.format(message);
	 
	                    //DialogUtils.displayInfoDialog(context, R.string.nfcWrittenTitle, R.string.nfcWritten);
	                    return true;
	                } catch (TagLostException tle) {
	                    //DialogUtils.displayErrorDialog(context, R.string.nfcTagLostErrorTitle, R.string.nfcTagLostError);
	                    return false;
	                } catch (IOException ioe) {
	                    //DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
	                    return false;
	                } catch (FormatException fe) {
	                    //DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
	                    return false;
	                }
	            } else {
	                //DialogUtils.displayErrorDialog(context, R.string.nfcNoNdefErrorTitle, R.string.nfcNoNdefError);
	                return false;
	            }
	        }
	    } catch(Exception e) {
	        //DialogUtils.displayErrorDialog(context, R.string.nfcUnknownErrorTitle, R.string.nfcUnknownError);
	    	
	    }
	 
	    return false;
	}
}
