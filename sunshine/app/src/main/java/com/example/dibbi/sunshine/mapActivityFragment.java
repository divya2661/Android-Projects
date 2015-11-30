package com.example.dibbi.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class mapActivityFragment extends Fragment {

    public mapActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String lat_log = "26.2078818,72.99017789999999";
        View rootview = inflater.inflate(R.layout.fragment_map, container, false);
        showMap(lat_log);
        return rootview;
    }

    public void showMap(String geoLocation) {
        String final_query = "geo:0,0"  +"?q="+  geoLocation;
        Uri uri = Uri.parse(final_query);
        Intent addressIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(addressIntent);

    }
}
