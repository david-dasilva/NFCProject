package org.mbds.android.tagnfc;


import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class NfcDialogFragment extends DialogFragment {

    public NfcDialogFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_fragment, container);
        return view;
    }

}