package com.aalto.nfctracking;


import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    private FragmentInteractionListener mListener;
    private EditText usernameField;
    private EditText passwordField;
    private Button loginBtn;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i("DNES", " LoginFragment | onCreateView");
        final LinearLayout mLinearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_login, container, false);

        usernameField = (EditText) mLinearLayout.findViewById(R.id.userName);
        passwordField = (EditText) mLinearLayout.findViewById(R.id.password);
        loginBtn = (Button) mLinearLayout.findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate();
            }
        });

        return mLinearLayout;
    }

    private void authenticate(){
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        if(username.equals("admin") && password.equals("admin")){
            Log.i("DNES", " LoginFragment | authenticated");
            SharedPreferences.Editor preferencesEditor = getActivity().getPreferences(0).edit();
            preferencesEditor.putString("loggedUserName", username);
            preferencesEditor.commit();
            Intent intent = getActivity().getIntent();
            getActivity().finish();
            startActivity(intent);
        } else{
            Log.i("DNES", " LoginFragment | wrong credentials!");
            Toast.makeText(getActivity(), "Wrong credentials!!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onAttach(Context context) {
        mListener = (FragmentInteractionListener) context;
        super.onAttach(context);
    }

}
