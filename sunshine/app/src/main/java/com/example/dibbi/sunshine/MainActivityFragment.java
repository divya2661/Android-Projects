package com.example.dibbi.sunshine;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public ArrayAdapter<String> MforcastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute("94043");

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.forecatfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();

        //On refreshing click
        if(id==R.id.action_refresh) {

            FetchWeatherTask weatherTask = new FetchWeatherTask();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            String temp_unit = prefs.getString(getString(R.string.pref_temp_key),"1");
            //Log.e("temp","temp unit" + temp_unit);
            weatherTask.execute(location);

            return true;
        }
        //on setting click front_screen
        else if(id==R.id.action_settings) {
            startActivity(new Intent(getActivity(),SettingsActivity.class));
            return true;
        }
        //on map click
        else if(id==R.id.action_map){
            startActivity(new Intent(getActivity(),mapActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        String[] forecastArray = new String[]{
                "loading....",

        };

        ArrayList<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        MforcastAdapter = new ArrayAdapter<String>(getActivity(),
                                                   R.layout.list_item_forcast,
                                                   R.id.list_item_forecast_textview,
                                                   weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);

        listView.setAdapter(MforcastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, android.view.View view,int position,long l){

                  String forecast = MforcastAdapter.getItem(position);
                Intent in = new Intent(getActivity(),Details_activity.class).putExtra(Intent.EXTRA_TEXT,forecast);
                startActivity(in);
            }
        });

        return rootView;
    }



    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG;

        {
            LOG_TAG = FetchWeatherTask.class.getSimpleName();
        }

         // Prepare the weather high/lows for presentation.

        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        String convertStreamToString(java.io.InputStream is) {
            try {
                return new java.util.Scanner(is).useDelimiter("\\A").next();
            } catch (java.util.NoSuchElementException e) {
                return "";
            }
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";
            String temp_unit_type = "C";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            Calendar calendar = Calendar.getInstance();
            int day_num = calendar.get(Calendar.DAY_OF_WEEK)-1;
           // Log.e(LOG_TAG,"day_num_initial: "+day_num);
            String[] resultStrs = new String[numDays];

            for(int i = 0; i < weatherArray.length(); i++) {

                String day = null;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                if(day_num==0)
                    day = "Sun";
                else if(day_num==1)
                    day = "Mon";
                else if(day_num==2)
                    day = "Tue";
                else if(day_num==3)
                    day = "Wed";
                else if(day_num==4)
                    day = "Thu";
                else if(day_num==5)
                    day = "Fri";
                else if(day_num==6)
                    day = "Sat";

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String temp_unit = prefs.getString(getString(R.string.pref_temp_key), "1");
                Log.e("tag","temp_unit_dibbi: " + temp_unit);
                if(temp_unit.equals("2")){

                    high = high*9/5 + 32;
                    low = low*9/5 + 32;
                    temp_unit_type = "F";
                    Log.e("tag","temp_unit_type: " + temp_unit_type + " high: " + high + " low " + low);
                }

                highAndLow = formatHighLows(high, low);


                resultStrs[i] = day + " - " + description + " - " + highAndLow + temp_unit_type;



                day_num = (day_num + 1)%7;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Dibbi: " + s);
            }
            return resultStrs;

        }




        @Override
        protected String[] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                String format = "json";
                String units = "metric";
                int numday = 16;
                String appid = "fd7578c3f6aed34d19b48e4f06f04d1b";

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNIT_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "appid";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                                .appendQueryParameter(QUERY_PARAM,params[0])
                                .appendQueryParameter(FORMAT_PARAM,format)
                                .appendQueryParameter(UNIT_PARAM,units)
                                .appendQueryParameter(DAYS_PARAM,Integer.toString(numday))
                                .appendQueryParameter(APPID_PARAM,appid)
                                .build();



                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG,"Built URI" + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();

                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    //Log.v(LOG_TAG,"Input stream is null");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                //Log.v(LOG_TAG,"reader dibbi: " + reader.readLine());
                String line;

                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    //Log.v(LOG_TAG,"inside while");
                    buffer.append(line + "\n");
                }
                //Log.v(LOG_TAG,"Buffer: " + buffer);
                if (buffer.length() == 0) {
                    //Log.v(LOG_TAG,"buffer length =0 ");
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG,"Forecast JSON String" + forecastJsonStr);

                try {
                   return getWeatherDataFromJson(forecastJsonStr,numday);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(String[] result) {

            if(result!=null) {
                MforcastAdapter.clear();

                for(String weekforecast : result){
                    MforcastAdapter.add(weekforecast);
                }
            }
        }
    }

}
