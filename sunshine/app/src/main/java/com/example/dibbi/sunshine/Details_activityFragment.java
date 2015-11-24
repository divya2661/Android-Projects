package com.example.dibbi.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class Details_activityFragment extends Fragment {

    public Details_activityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        View rootview = inflater.inflate(R.layout.fragment_details_activity,container,false);
        if(intent!=null && intent.hasExtra(Intent.EXTRA_TEXT)){

            String detail_forecast = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView)rootview.findViewById(R.id.detail_text)).setText(detail_forecast);
        }

        return rootview;
    }
}
