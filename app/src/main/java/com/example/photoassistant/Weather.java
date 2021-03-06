package com.example.photoassistant;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The weather fragment handles the weather screens UI and manages the API
 * from the sg.gov.
 */

public class Weather extends Fragment {
    Spinner location;
    TextView nowTemp;
    ImageView weatherIcon;
    TextView Description;
    TextView hum;
    TextView psi;
    TextView Hour1;
    TextView Hour1Temp;
    TextView Hour2;
    TextView Hour2Temp;
    TextView Hour3;
    TextView Hour3Temp;
    ImageView H1Icon;
    ImageView H2Icon;
    ImageView H3Icon;
    LinkedList<String> options=new LinkedList<String>();

    Handler handler;
    int count =0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
    }

    /**
     * binds all the ui elements to the java code, also checks for internet
     * and returns response.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        handler = new Handler();

        location = (Spinner) view.findViewById(R.id.Location);
        Description = (TextView) view.findViewById(R.id.Description);
        weatherIcon = (ImageView) view.findViewById(R.id.WeatherIcon);
        nowTemp = (TextView) view.findViewById(R.id.currentTemp);
        psi = (TextView) view.findViewById(R.id.PSI);
        hum = (TextView) view.findViewById(R.id.Humidity);
        Hour1 = (TextView) view.findViewById(R.id.Hour1);
        Hour2 = (TextView) view.findViewById(R.id.Hour2);
        Hour3 = (TextView) view.findViewById(R.id.Hour3);
        Hour1Temp = (TextView) view.findViewById(R.id.H1Temp);
        Hour2Temp = (TextView) view.findViewById(R.id.H2Temp);
        Hour3Temp = (TextView) view.findViewById(R.id.H3Temp);
        H1Icon = (ImageView) view.findViewById(R.id.H1Icon);
        H2Icon = (ImageView) view.findViewById(R.id.H2Icon);
        H3Icon = (ImageView) view.findViewById(R.id.H3Icon);

        // Check Internet Connectivity
        if (isNetworkAvailable() == false) {
            Toast.makeText(getActivity(), "Please check your Internet connection and try again.", Toast.LENGTH_LONG).show();
            //getActivity().finish();
        }



        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        updateWeather();
        super.onViewCreated(view, savedInstanceState);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * interface with api using json to get the air temperature, as well as the 24 hr forecast.
     *
     */
    private void updateWeather(){
        new Thread(){
            public void run(){
                final JSONObject currentTemp =getWeatherAPI.getWeather("air-temperature"); //currentTemp
                final JSONObject OWForecast =getOpenWeather.getForecast(); //description, icon
                final JSONObject PSI =getWeatherAPI.getWeather("psi"); //PSI
                final JSONObject Humidity =getWeatherAPI.getWeather("24-hour-weather-forecast"); //humidity


                if (currentTemp == null || OWForecast == null || PSI == null || Humidity == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Log.e("weather", "updateWeather(): Error retrieving data.");
                            Toast.makeText(getActivity(), "Error in getting data from NEA.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeatherScreen(currentTemp, OWForecast, PSI, Humidity);
                        }
                    });
                }

            }

        }.start();
    }
    List<Location> locations = new ArrayList<Location>();

    private void renderWeatherScreen(final JSONObject currentTemp, final JSONObject OWForecast, final JSONObject PSI, final JSONObject Humidity){
        String locationString;
        Map<String, String> simpleLocationNames = new HashMap<String, String>();
        simpleLocationNames.put("Banyan Road", "Jurong Island");
        simpleLocationNames.put("Clementi Road", "Clementi");
        simpleLocationNames.put("East Coast Parkway", "East Coast");
        simpleLocationNames.put("Kim Chuan Road", "Bartley");
        simpleLocationNames.put("Nanyang Avenue", "Nanyang Avenue");
        simpleLocationNames.put("Old Choa Chu Kang Road", "Tengah");
        simpleLocationNames.put("Pulau Ubin", "Pulau Ubin");
        simpleLocationNames.put("Sembawang Road", "Yishun");
        simpleLocationNames.put("Sentosa", "Sentosa");
        simpleLocationNames.put("Tuas South Avenue 3", "Tuas");
        simpleLocationNames.put("West Coast Highway", "West Coast");
        simpleLocationNames.put("Woodlands Avenue 9", "Woodlands");
        simpleLocationNames.put("Woodlands Road", "Sungei Kadut");
        try {
            GPS gps = new GPS(getContext());
            Location current = new Location("");
            current.setLatitude(gps.getLatitude());
            current.setLongitude(gps.getLongitude());

            // Initialize data streams
            //JSONArray currentTempData = currentTemp.getJSONArray("items").getJSONObject(0).getJSONArray("readings");
            JSONArray metadata = currentTemp.getJSONObject("metadata").getJSONArray("stations");
            if(metadata.length()<=0)
            {

                Log.e("weather", "updateWeather(): Error retrieving data.");
                Toast.makeText(getActivity(), "Error in getting data from NEA.", Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i=0;i<metadata.length();i++) {
                double latitude = metadata.getJSONObject(i).getJSONObject("location").getDouble("latitude");
                double longitude = metadata.getJSONObject(i).getJSONObject("location").getDouble("longitude");
                if(simpleLocationNames.containsKey(metadata.getJSONObject(i).getString("name")))
                {
                    options.add(simpleLocationNames.get(metadata.getJSONObject(i).getString("name")));
                }
                else
                {
                    options.add(metadata.getJSONObject(i).getString("name"));
                }
                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                locations.add(location);
            }

            double shortestDistance = Double.MAX_VALUE; int targetIndex=0;
            for (int i=0;i<locations.size();i++) {
                int R = 6371; // km
                double x = (Math.toRadians(locations.get(i).getLongitude()) - Math.toRadians(current.getLongitude())) * Math.cos((Math.toRadians(current.getLatitude()) + Math.toRadians(locations.get(i).getLatitude())) / 2);
                double y = (Math.toRadians(locations.get(i).getLatitude()) - Math.toRadians(current.getLatitude()));
                double distance = Math.sqrt(x * x + y * y) * R;
                if(distance<shortestDistance)
                {
                    shortestDistance = distance;
                    targetIndex = i;

                }
            }
            locationString =  metadata.getJSONObject(targetIndex).getString("name");

            double currentTempData = currentTemp.getJSONArray("items").getJSONObject(0).getJSONArray("readings").getJSONObject(targetIndex).getDouble("value");
            JSONArray forecastData = OWForecast.getJSONArray("list");
            int PSIData = PSI.getJSONArray("items").getJSONObject(0).getJSONObject("readings").getJSONObject("psi_twenty_four_hourly").getInt("national");
            int humidityData = Humidity.getJSONArray("items").getJSONObject(0).getJSONObject("general").getJSONObject("relative_humidity").getInt("high");


            // Current Weather Description
            Description.setText(forecastData.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("description").trim());
            // Current Weather icon
            getWeatherIcon(forecastData.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("icon"));

            // Current Temperature, Humidity, and PSI
            nowTemp.setText(currentTempData + "℃");
            hum.setText("Humidity: " + humidityData + "%");
            psi.setText("PSI: " + PSIData);

            // 3h forecast temperatures + icons
            int hours[] = new int[3];
            int tempVals[] = new int[3];
            String icons[] = new String[3];
            int diff, data_d, k;
            int index = 0;

            // Not sure if this part is necessary. I think it'd be fine if I just parse
            // 1, 2, 3 for the next 9h forecast...
            Calendar c = Calendar.getInstance();
            int x = c.get(Calendar.HOUR_OF_DAY);
            int d = c.get(Calendar.DAY_OF_MONTH);

            for (int j = 1; j < 4; j++) {
                hours[j - 1] = x + (j * 3);

                if (hours[j - 1] > 24) {
                    hours[j - 1] = hours[j - 1] - 24;
                }
            }
            for (k = 0; k < 10; k++) {
                diff = Integer.parseInt(forecastData.getJSONObject(k).getString("dt_txt").substring(11, 13)) - x; //time difference (hours)
                data_d = Integer.parseInt(forecastData.getJSONObject(k).getString("dt_txt").substring(8, 10)); //day
                if (diff > 0 && data_d == d) { // to avoid problems when time is 00:00:00, but date = next day. (midnight)
                    break;
                }
            }
            for (int l = k; l < (k + 3); l++) {
                tempVals[index] = forecastData.getJSONObject(l).getJSONObject("main").getInt("temp");
                icons[index] = forecastData.getJSONObject(l).getJSONArray("weather").getJSONObject(0).getString("icon");
                index++;
            }
            // 3h forecast temperature
            Hour1Temp.setText("" + tempVals[0] + "℃");
            Hour2Temp.setText("" + tempVals[1] + "℃");
            Hour3Temp.setText("" + tempVals[2] + "℃");

            // 3h forecast icons
            getHourlyIcon(icons[0], H1Icon);
            getHourlyIcon(icons[1], H2Icon);
            getHourlyIcon(icons[2], H3Icon);

            // Time values:
            setTimeStrings();

            // Location


            location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    boolean userSelect = false;
                    if (!userSelect) {
                        try {
                            //duplicated code
                            Log.d("weatherDebug", "onItemSelected: " + count);
                            count++;
                            double currentTempData = currentTemp.getJSONArray("items").getJSONObject(0).getJSONArray("readings").getJSONObject(position).getDouble("value"); // index 1 for Clementi Road
                            JSONArray forecastData = OWForecast.getJSONArray("list");
                            int PSIData = PSI.getJSONArray("items").getJSONObject(0).getJSONObject("readings").getJSONObject("psi_twenty_four_hourly").getInt("national");
                            int humidityData = Humidity.getJSONArray("items").getJSONObject(0).getJSONObject("general").getJSONObject("relative_humidity").getInt("high");
                            // Current Weather Description
                            Description.setText(forecastData.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("description").trim());
                            // Current Weather icon
                            getWeatherIcon(forecastData.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("icon"));

                            // Current Temperature, Humidity, and PSI
                            nowTemp.setText(currentTempData + "℃");
                            userSelect = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            userSelect = false;
                        }
                    }
                }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item,options);

            location.setAdapter(adapter);
            location.setSelection(targetIndex);
            if(simpleLocationNames.containsKey(locationString))
            {

            }
            else
            {
                //location.setText(locationString);
            }

        } catch (Exception e) {
            Log.e("MainActivity", "renderWeatherScreen(): One or more fields not found in the JSON data.");
        }
    }



    private void setTimeStrings() {
//        Instant instant = Instant.now();
//        ZoneId z = ZoneId.of("Asia/Singapore");
//        ZonedDateTime zdt = instant.atZone(z);
//        Calendar c = Calendar.getInstance(zdt);
        TimeZone tz = TimeZone.getTimeZone("GMT+8:00");
        Calendar c = Calendar.getInstance(tz);
        int x = c.get(Calendar.HOUR_OF_DAY);
        int y; // 1 = PM, 0 = AM.
        String hours[] = new String[4];
        int hour;

        for (int j = 1; j < 4; j++) {
            hour = x + (j * 3);

            if (hour > 11 && hour < 24) {
                if (hour != 12) {
                    hour = hour % 12;
                }
                y = 1;
            } else {
                if (hour > 24) {
                    hour = hour - 24;
                } else if (hour == 24) {
                    hour = 12;
                }
                y = 0;
            }

            if (y == 0) {
                hours[j] = Integer.toString(hour) + "AM";
            } else {
                hours[j] = Integer.toString(hour) + "PM";
            }
            Hour1.setText(hours[1]);
            Hour2.setText(hours[2]);
            Hour3.setText(hours[3]);
        }
    }

    private void getWeatherIcon(String icon) {
        String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png"; //large icon for current
        Picasso.get().load(iconUrl).into(weatherIcon); //add picasso to build.gradle
    }

    private void getHourlyIcon(String icon, ImageView hourly) {
        Log.d("DebugTag", "getHourlyIcon: " + icon);
        String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png"; //small icons for forecast
        Picasso.get().load(iconUrl).into(hourly);

    }

}