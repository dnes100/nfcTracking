package com.aalto.nfctracking;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.aalto.nfctracking.utils.RestApiCall;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * { SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    private FragmentInteractionListener mListener;
    private EditText nfcReaderNameField;
    private EditText nfcReaderIdField;
    private EditText nfcReaderLocationField;
    private Button registerBtn;
    private Button clearPrefBtn;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           //
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final LinearLayout mLinearLayout = (LinearLayout) inflater.inflate(R.layout.settings_fragment, container, false);
        SharedPreferences preferences = getActivity().getPreferences(0);
        String nfcReaderName = preferences.getString("nfcReaderName", "");
        String nfcReaderId = preferences.getString("nfcReaderId", "");
        String nfcReaderLocation = preferences.getString("nfcReaderLocation", "");

        nfcReaderNameField = (EditText) mLinearLayout.findViewById(R.id.nfcReaderName);
        nfcReaderIdField = (EditText) mLinearLayout.findViewById(R.id.nfcReaderId);
        nfcReaderLocationField = (EditText) mLinearLayout.findViewById(R.id.nfcReaderLocation);
        registerBtn = (Button) mLinearLayout.findViewById(R.id.registerBtn);
        clearPrefBtn = (Button) mLinearLayout.findViewById(R.id.clearPrefBtn);

        nfcReaderNameField.setText(nfcReaderName, TextView.BufferType.NORMAL);
        nfcReaderIdField.setText(nfcReaderId, TextView.BufferType.NORMAL);
        nfcReaderLocationField.setText(nfcReaderLocation, TextView.BufferType.NORMAL);
        if(!nfcReaderId.isEmpty()){
            nfcReaderNameField.setEnabled(false);
            nfcReaderLocationField.setEnabled(false);
            /*nfcReaderNameField.setFocusable(false);
            nfcReaderNameField.setClickable(true);*/
            registerBtn.setVisibility(View.GONE);
        } else {
            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    registerNfcReaderId();
                }
            });
        }

        clearPrefBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPreferences();
            }
        });

        return mLinearLayout;
    }

    public void registerNfcReaderId(){
        Log.i("DNES", " Settings registerNfcReaderId");
        String readerName = nfcReaderNameField.getText().toString();
        String readerLocation = nfcReaderLocationField.getText().toString();
        if(readerName.isEmpty()){
            Toast.makeText(getActivity(), "Enter reader name!", Toast.LENGTH_SHORT).show();
            return;
        } else if (readerLocation.isEmpty()){
            Toast.makeText(getActivity(), "Enter reader Location!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject req = new JSONObject();
            req.put("interface", "handleNfcMethods");
            req.put("method", "registerNfcReader");

            JSONObject param = new JSONObject();
            param.put("readerName", readerName);
            param.put("readerLocation", readerLocation);
            req.put("parameters", param);

            Response.Listener successListener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("DNES", "Response: " + response.toString());
                    try {
                        JSONObject result = response.getJSONObject("response").getJSONObject("result");
                        String readerId = result.getString("readerId");
                        String readerName = result.getString("readerName");
                        String readerLocation = result.getString("readerLocation");
                        saveNfcReaderDetails(readerId, readerName, readerLocation);
                        nfcReaderIdField.setText(readerId, TextView.BufferType.NORMAL);
                        nfcReaderNameField.setEnabled(false);
                        registerBtn.setVisibility(View.GONE);
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

    public void saveNfcReaderDetails(String nfcReaderId, String nfcReaderName, String nfcReaderLocation){
        Log.i("DNES", "saveNfcReaderDetails");
        SharedPreferences.Editor preferencesEditor = getActivity().getPreferences(0).edit();
        preferencesEditor.putString("nfcReaderName", nfcReaderName);
        preferencesEditor.putString("nfcReaderId", nfcReaderId);
        preferencesEditor.putString("nfcReaderLocation", nfcReaderLocation);
        preferencesEditor.commit();
        nfcReaderNameField.setEnabled(false);
        nfcReaderLocationField.setEnabled(false);
        Toast.makeText(getActivity(), "Details Saved!", Toast.LENGTH_SHORT).show();
    }

    private void clearPreferences(){
        Log.i("DNES", "clearPreferences");
        getActivity().getPreferences(0).edit().clear().commit();
        nfcReaderIdField.setText("", TextView.BufferType.NORMAL);
        nfcReaderNameField.setText("", TextView.BufferType.NORMAL);
        nfcReaderNameField.setEnabled(true);
        nfcReaderLocationField.setText("", TextView.BufferType.NORMAL);
        nfcReaderNameField.setEnabled(true);
        Toast.makeText(getActivity(), "Preference Cleared!", Toast.LENGTH_SHORT).show();
    }

  /*  // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     *
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    } */
}
