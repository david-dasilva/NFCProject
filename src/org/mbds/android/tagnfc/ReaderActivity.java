package org.mbds.android.tagnfc;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activité affichant les messages stocké dans le tag NFC Les informations
 * utiles sont transmises par l'activité mère via le Bundle Il est possible
 * revenir en arrière en secouant le téléphone
 */
@SuppressWarnings("deprecation")
public class ReaderActivity extends Activity implements SensorListener {

	// Gestion du shaker
	private static final int SHAKE_THRESHOLD = 5000;
	private SensorManager sensorMgr = null;

	long lastUpdate = 0;
	float last_x = 0;
	float last_y = 0;
	float last_z = 0;

	String message = "";
	String id = "";
	String technologies = "";
	String isWritable = "";
	String isLockable = "";
	public static String typeMessage = "";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reader_layout);

		TagActivity.FromReader = true;

		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER,
				SensorManager.SENSOR_DELAY_GAME);

		Bundle bundle = getIntent().getExtras();
		message = bundle.getString("message");
		id = bundle.getString("id");
		technologies = bundle.getString(technologies);
		isWritable = bundle.getString("isWritable");
		isLockable = bundle.getString("canMakeReadOnly");
		typeMessage = bundle.getString("typeMessage");

		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (!message.isEmpty()) {

			// Affichage du message dans le textView
			TextView tvMessage = (TextView) findViewById(R.id.tvMessage);
			tvMessage.setText(message);

			TextView typeMessage = (TextView) findViewById(R.id.textmessage);
			typeMessage.setText(this.typeMessage);

			Log.d("TAGNFC",this.typeMessage);
			Button bt = (Button) findViewById(R.id.btsearch);
			if (this.typeMessage.contains("url"))
				bt.setText("Go to website !");
			else if (this.typeMessage.contains("phone"))
				bt.setText("Call number ! ");
			else if (this.typeMessage.contains("email")) {
				bt.setText("Send email now !");
			}
			else
				bt.setText("Search message on web !");
			
			bt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					if (ReaderActivity.typeMessage.contains("url")){
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(message));
						startActivity(browserIntent);
					}
					else if (ReaderActivity.typeMessage.contains("phone")){
						Intent callIntent = new Intent(Intent.ACTION_CALL);
						callIntent.setData(Uri.parse("tel:"+message));
						startActivity(callIntent);
						
					}
					else if (ReaderActivity.typeMessage.contains("email")) {
						Intent i = new Intent(Intent.ACTION_SEND);  
						//i.setType("text/plain");
						i.setType("message/rfc822") ; // use from live device
						i.putExtra(Intent.EXTRA_EMAIL, new String[]{message});  
						startActivity(Intent.createChooser(i, "Select email application."));
					}
					else
					{
						String url = "http://www.google.com/#q="+message;
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
						
					}
				}
			});
			

			TextView tvId = (TextView) findViewById(R.id.tvId);
			tvId.setText(id);

			TextView tvEcriture = (TextView) findViewById(R.id.tvEcriture);
			tvEcriture.setText(isWritable);

			TextView tvLockable = (TextView) findViewById(R.id.tvLockable);
			tvLockable.setText(isLockable);
		}

	}

	/**
	 * Ferme l'activité et reviens à l'activité principale lorsque l'on
	 * secoue le téléphone
	 * 
	 * @param sensor
	 * @param values
	 */
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

				if (speed > SHAKE_THRESHOLD) {
					finish();
				}
				last_x = x;
				last_y = y;
				last_z = z;
			}
		}
	}

	@Override
	public void onAccuracyChanged(int sensor, int accuracy) {
		// TODO Auto-generated method stub

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
