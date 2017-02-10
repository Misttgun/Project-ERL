package com.maurelsagbo.project_erl.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maurelsagbo.project_erl.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateFlightPFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateFlightPFragment extends Fragment {

    protected static final String TAG = "CreateFlightPFragment";

    public CreateFlightPFragment() {
        // Required empty public constructor
    }

    public static CreateFlightPFragment newInstance() {
        CreateFlightPFragment fragment = new CreateFlightPFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_flight_p, container, false);
    }

}
