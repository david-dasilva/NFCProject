package org.mbds.android.tagnfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;

public class TagNfc{
	byte[] id;
	String[] technologies;
	int content;
	Ndef ndef;
	boolean isWritable;
	boolean canMakeReadOnly;
	Parcelable[] rawMsgs;
	NdefMessage[] msgs;
	
	public TagNfc(Intent intent) {
		super();

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		this.id = tag.getId();
		this.technologies =  tag.getTechList();
		this.content = tag.describeContents();
		this.ndef = Ndef.get(tag);
		this.isWritable = ndef.isWritable();
		this.canMakeReadOnly = ndef.canMakeReadOnly();
		this.rawMsgs =  intent.getParcelableArrayExtra( NfcAdapter.EXTRA_NDEF_MESSAGES);
		
	}
	
	
	public String getMessage()
	{
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


	public byte[] getId() {
		return id;
	}


	public String[] getTechnologies() {
		return technologies;
	}


	public int getContent() {
		return content;
	}


	public Ndef getNdef() {
		return ndef;
	}


	public boolean isWritable() {
		return isWritable;
	}


	public boolean isCanMakeReadOnly() {
		return canMakeReadOnly;
	}


	public Parcelable[] getRawMsgs() {
		return rawMsgs;
	}


	public NdefMessage[] getMsgs() {
		return msgs;
	}
	
	
}