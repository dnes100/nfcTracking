package com.aalto.nfctracking;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {link Home.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private FragmentInteractionListener mListener;
    private LinearLayout mLinearLayout;
    private Button readFragmentBtn;
    private Button writeFragmentBtn;
    private Button settingFragmentBtn;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mLinearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_home, container, false);
        readFragmentBtn = (Button) mLinearLayout.findViewById(R.id.readFragmentBtn);
        readFragmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.changeFragment(new ReadTagFragment());
            }
        });

        writeFragmentBtn = (Button) mLinearLayout.findViewById(R.id.writeFragmentBtn);
        writeFragmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.changeFragment(new WriteTagFragment());
            }
        });

        settingFragmentBtn = (Button) mLinearLayout.findViewById(R.id.settingFragmentBtn);
        settingFragmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.changeFragment(new SettingsFragment());
            }
        });
        return  mLinearLayout;
    }

    @Override
    public void onAttach(Context context) {
        mListener = (FragmentInteractionListener) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
