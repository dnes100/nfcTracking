package com.aalto.nfctracking;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aalto.nfctracking.utils.RestApiCall;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * { WriteTagFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WriteTagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WriteTagFragment extends Fragment {

    private FragmentInteractionListener mListener;
    private LinearLayout mLinearLayout;
    private EditText nfcTagIdField;
    private TextView attachTagMsg;
    TextView successMessage;
    private Button writeBtn;

    public WriteTagFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WriteTagFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WriteTagFragment newInstance() {
        WriteTagFragment fragment = new WriteTagFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mLinearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_write_tag, container, false);
        nfcTagIdField = (EditText) mLinearLayout.findViewById(R.id.nfcTagId);
        attachTagMsg = (TextView) mLinearLayout.findViewById(R.id.attachTagMsg);
        //generateTagIdBtn = (Button) mLinearLayout.findViewById(R.id.generateTagIdBtn);
        writeBtn = (Button) mLinearLayout.findViewById(R.id.writeBtn);

        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateTagIdFromServer();
            }
        });
        return  mLinearLayout;
    }

    // TODO: Rename method, update argument and hook method into UI event
   /* public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i("DNES", "WriteTagFragment onPause!!");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i("DNES", "WriteTagFragment onResume!!");

        Intent intent = getActivity().getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return;
            }
            String nfcTagId = nfcTagIdField.getText().toString();
            if (!nfcTagId.isEmpty()){
                writeToTag(nfcTagId, tag);
            }

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void generateTagIdFromServer(){
        Log.i("DNES", "generateTagIdFromServer");
        try {
            JSONObject req = new JSONObject();
            req.put("interface", "handleNfcMethods");
            req.put("method", "registerNfcTag");

            JSONObject param = new JSONObject();
            req.put("parameters", param);

            Response.Listener successListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("DNES", "Response: " + response.toString());
                    try {
                        JSONObject result = response.getJSONObject("response").getJSONObject("result");
                        String nfcTagId = result.getString("nfcTagId");
                        nfcTagIdField.setText(nfcTagId, TextView.BufferType.NORMAL);
                        attachTagMsg.setVisibility(View.VISIBLE);
                        writeBtn.setVisibility(View.INVISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            RestApiCall.postJsonObjectReqest(getActivity(), req, successListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void writeToTag(String nfcTagId, Tag tag) {
        Log.i("DNES", "writeToTag");

        try {
            NdefRecord[] records = new NdefRecord[]{ createRecord(nfcTagId) };
            NdefMessage message = new NdefMessage(records);
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            ndef.writeNdefMessage(message);
            ndef.close();
            successMessage = (TextView) mLinearLayout.findViewById(R.id.successMessage);
            attachTagMsg.setVisibility(View.INVISIBLE);
            successMessage.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
    }

    private NdefRecord createRecord(String nfcId) throws UnsupportedEncodingException {

        //create the message in according with the standard
        String lang = "en";
        byte[] langBytes = new byte[0];
        byte[] textBytes = nfcId.getBytes("UTF-8");
        langBytes = lang.getBytes("UTF-8");

        int langLength = langBytes.length;
        int textLength = textBytes.length;

        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        return recordNFC;
    }

}
