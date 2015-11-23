package com.example.dibbi.sunshine;

import android.content.ClipData;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

        if(id==R.id.action_refresh) {

            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("94043");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        String[] forecastArray = new String[]{
                "Mon - Clear - 1/-3",
                "Mon - Clear - 1/-3",
                "Mon - Clear - 1/-3",
                "Mon - Clear - 1/-3",
                "Mon - Clear - 1/-3",
                "Mon - Clear - 1/-3",
                "Mon - Clear - 1/-3"

        };

//        String[] array = {"a","b","c","d","e","f","g"};
//        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(array));
//        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                                                                       android.R.layout.
//
//                                                                            simple_list_item_1, lst);

        ArrayList<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        MforcastAdapter = new ArrayAdapter<String>(getActivity(),
                                                   R.layout.list_item_forcast,
                                                   R.id.list_item_forecast_textview,
                                                   weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);

        listView.setAdapter(MforcastAdapter);

        return rootView;

    }



    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG;

        {
            LOG_TAG = FetchWeatherTask.class.getSimpleName();
        }


        /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
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

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            Log.v(LOG_TAG, "Dibbiiiiiiiiiii: ");
            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            //Time dayTime = new Time();
            //dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            // int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            // dayTime = new Time();
            Calendar calendar = Calendar.getInstance();
            int day_num = calendar.get(Calendar.DAY_OF_WEEK)-1;
            Log.e(LOG_TAG,"day_num_initial: "+day_num);
            String[] resultStrs = new String[numDays];

            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day = null;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                // long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                //dateTime = dayTime.setJulianDay(julianStartDay+i);
                //day = getReadableDateString(dateTime);


//                Log.e(LOG_TAG,"day_num: "+day_num);
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

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;

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
                Log.e(LOG_TAG, "okay clear is working: ");
                for(String weekforecast : result){
                    Log.e("for loop: ", weekforecast );
                    MforcastAdapter.add(weekforecast);
                }
            }
        }
    }

}
