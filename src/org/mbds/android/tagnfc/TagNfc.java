package org.mbds.android.tagnfc;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.webkit.URLUtil;

/**
 * Permet d'extraire facilement les données utiles d'un tag NFC
 */
public class TagNfc {
	byte[] id;
	String[] technologies;
	int content;
	Ndef ndef;
	boolean isWritable;
	boolean canMakeReadOnly;
	Parcelable[] rawMsgs;
	NdefMessage[] msgs;

	public TagNfc(Intent intent) {

		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		this.id = tag.getId();
		this.technologies = tag.getTechList();
		this.content = tag.describeContents();
		this.ndef = Ndef.get(tag);
		this.isWritable = ndef.isWritable();
		this.canMakeReadOnly = ndef.canMakeReadOnly();
		this.rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

	}

	public String getMessage() {
		String message = null;
		if (rawMsgs != null) {
			msgs = new NdefMessage[rawMsgs.length];
			for (int i = 0; i < rawMsgs.length; i++) {
				msgs[i] = (NdefMessage) rawMsgs[i];
				NdefRecord record = msgs[i].getRecords()[i];
				byte[] idRec = record.getId();
				short tnf = record.getTnf();
				byte[] type = record.getType();
				message = new String(record.getPayload());
			}
			return message;
		}
		return "No message found ! ";
	}

	public String getId() {

		String idtxt = "";
		idtxt = bytesToHex(id);
		return idtxt;
	}

	public static final Pattern VALID_PHONE_REGEX = Pattern.compile("[0-9]{9}",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern EMAIL_PATTERN = Pattern
			.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}",
					Pattern.CASE_INSENSITIVE);

	public static boolean validate(String emailStr, int type) {
		if (type == 1) {
			Matcher matcher = VALID_PHONE_REGEX.matcher(emailStr);
			return matcher.find();
		} else if (type == 2) {
			Matcher matcher = EMAIL_PATTERN.matcher(emailStr);
			return matcher.find();
		}
		return false;
	}

	public String getTypeMessage(String message) {

		Log.d("TAGNFC", message);
		if (validate(message, 2))
			return "email";

		if (validate(message, 1))
			return "phone";

		if (message.contains("http://")) {
			if (URLUtil.isValidUrl(message))
				return "url";
		}
		return "unknow";

	}

	public String getTechnologies() {

		String techList = "";
		for (int k = 0; k < technologies.length; k++) {
			techList += technologies[k];
			if (k < (technologies.length - 1)) {
				techList += " / ";
			}
		}

		return techList;
	}

	public int getContent() {
		return content;
	}

	public Ndef getNdef() {
		return ndef;
	}

	public String isWritable() {
		if (isWritable)
			return "Oui";
		else
			return "Non";
	}

	public String isCanMakeReadOnly() {
		if (canMakeReadOnly)
			return "Oui";
		else
			return "Non";
	}

	public Parcelable[] getRawMsgs() {
		return rawMsgs;
	}

	public NdefMessage[] getMsgs() {
		return msgs;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}