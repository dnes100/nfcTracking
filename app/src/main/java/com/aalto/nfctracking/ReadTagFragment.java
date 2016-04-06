package com.aalto.nfctracking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aalto.nfctracking.utils.RestApiCall;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class ReadTagFragment extends Fragment {

    private FragmentInteractionListener mListener;
    private EditText nfcTagIdField;
    private LinearLayout mLinearLayout;
    private ToggleButton sendToggleBtn;
    private boolean isSendChecked = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.i("DNES", "ReadTagFragment onCreate!!");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("DNES", "ReadTagFragment onCreateView!!");
        mLinearLayout = (LinearLayout) inflater.inflate(R.layout.read_tag_fragment, container, false);
        nfcTagIdField = (EditText) mLinearLayout.findViewById(R.id.nfcTagId);
        sendToggleBtn = (ToggleButton) mLinearLayout.findViewById(R.id.sendToggleBtn);
        sendToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isSendChecked = true;
                } else {
                    isSendChecked = false;
                }
            }
        });
        return mLinearLayout;
    }

    @Override
    public void onAttach(Context context) {
        mListener = (FragmentInteractionListener) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i("DNES", "ReadTagFragment onDetach!!");
        mListener = null;
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i("DNES", "ReadTagFragment onPause!!");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i("DNES", "ReadTagFragment onResume!!");

        Intent intent = getActivity().getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return;
            }
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        String nfcTagId = readText(ndefRecord);
                        nfcTagIdField.setText(nfcTagId, TextView.BufferType.NORMAL);
                        if(isSendChecked){
                            sendtagToServer(nfcTagId);
                        } else {
                            Toast.makeText(getActivity(), "Tag read successfully!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            /*Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }*/
        }
    }

    private void sendtagToServer(String nfcTagId) {

        try {
            SharedPreferences preferences = getActivity().getPreferences(0);
            JSONObject req = new JSONObject();
            req.put("interface", "handleNfcMethods");
            req.put("method", "saveNfcData");

            JSONObject param = new JSONObject();
            param.put("nfcTagId", nfcTagId);
            param.put("readerId", preferences.getString("nfcReaderId", ""));
            param.put("readerName", preferences.getString("nfcReaderName", ""));
            param.put("readerLocation", preferences.getString("nfcReaderLocation", ""));
            req.put("parameters", param);

            Response.Listener successListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("DNES", "Response: " + response.toString());
                    try {
                        JSONObject result = response.getJSONObject("response").getJSONObject("result");
                        String readerId = result.getString("readerId");
                        String nfcTagId = result.getString("nfcTagId");
                        Toast.makeText(getActivity(), "Info Sent!", Toast.LENGTH_SHORT).show();
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

    private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding;
        if((payload[0] & 128) == 0)
        {
            textEncoding = "UTF-8";
        } else {
            textEncoding = "UTF-16";
        }

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        String s = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        return  s;
    }

}
