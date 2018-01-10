package com.easyweather.bryant.easyweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.ConnectionResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private RequestQueue requestQueue;
    private SharedPreferences prefs;
    private MenuItem refreshItem;
    private TextView locationTextView;
    private TextView currentTempTextView;
    //private TextView highLowTextView;
    private TextView refreshTimeTextView;
    private TextView forecast1TextView;
    private TextView forecast2TextView;
    private TextView forecast3TextView;
    private TextView forecast4TextView;
    private TextView forecast5TextView;
    private TextView weatherUnitTextView;
    private TextView humidityTextView;
    private TextView windTextView;
    private TextView sunriseTextView;
    private TextView sunsetTextView;
    private ImageView currentWeatherImageView;
    private TextView currentDescTextView;
    private String refreshDateTime;
    private SimpleDateFormat refreshDateTimeFormat = new SimpleDateFormat("EEE, MMMM h:mm a");
    private SimpleDateFormat forecaseDateFormat = new SimpleDateFormat("EEE");
    private ImageView f1ImageView;
    private ImageView f2ImageView;
    private ImageView f3ImageView;
    private ImageView f4ImageView;
    private ImageView f5ImageView;

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    protected String mLatitudeLabel;
    protected String mLongitudeLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);


        init();
        buildGoogleApiClient();

        mGoogleApiClient.connect();
        refreshData();

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void init() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        locationTextView = (TextView) findViewById(R.id.locationTextView);
        currentTempTextView = (TextView) findViewById(R.id.currentTempTextView);
        currentWeatherImageView = (ImageView) findViewById(R.id.currentWeatherImageView);

        weatherUnitTextView = (TextView) findViewById(R.id.unitTextView);
        currentDescTextView = (TextView) findViewById(R.id.currentDescriptionTextView);

        refreshTimeTextView = (TextView) findViewById(R.id.refreshTimeTextView);
        forecast1TextView = (TextView) findViewById(R.id.f1TextView);
        forecast2TextView = (TextView) findViewById(R.id.f2TextView);
        forecast3TextView = (TextView) findViewById(R.id.f3TextView);
        forecast4TextView = (TextView) findViewById(R.id.f4TextView);
        forecast5TextView = (TextView) findViewById(R.id.f5TextView);

        humidityTextView = (TextView) findViewById(R.id.humidityTextView);
        windTextView = (TextView) findViewById(R.id.windTextView);

        sunsetTextView = (TextView) findViewById(R.id.sunsetTextView);
        sunriseTextView = (TextView) findViewById(R.id.sunriseTextView);

        f1ImageView = (ImageView) findViewById(R.id.f1ImageView);
        f2ImageView = (ImageView) findViewById(R.id.f2ImageView);
        f3ImageView = (ImageView) findViewById(R.id.f3ImageView);
        f4ImageView = (ImageView) findViewById(R.id.f4ImageView);
        f5ImageView = (ImageView) findViewById(R.id.f5ImageView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    private void refreshData() {

        if (prefs == null)
            return ;

        String woeid = prefs.getString("woeid", null);

        if (woeid != null) {
            String unit = prefs.getString("temp_unit", null).toUpperCase();

            YahooClient.getWeather(woeid, unit, requestQueue, new YahooClient.WeatherClientListener() {
                @Override
                public void onWeatherResponse(Weather weather) {

                    refreshDateTime = refreshDateTimeFormat.format(new Date());

                    int code = weather.condition.code;
                    locationTextView.setText(weather.location.name);
                    currentTempTextView.setText("" + weather.condition.temp);
                    refreshTimeTextView.setText(refreshDateTime);
                    //highLowTextView.setText(weather.forecast.tempMax + "H\n" + weather.forecast.tempMin + "L");
                    weatherUnitTextView.setText("°" +  prefs.getString("temp_unit", null).toUpperCase());
                    currentDescTextView.setText(weather.condition.description);

                    windTextView.setText("Wind: " + getWindDirection(weather.wind.direction) + weather.wind.speed + " " + weather.units.speed );
                    humidityTextView.setText("Humidity: " + weather.atmosphere.humidity + "%");

                    sunriseTextView.setText("Sunrise: " + weather.astronomy.sunRise);
                    sunsetTextView.setText("Sunset: " + weather.astronomy.sunSet);

                    currentWeatherImageView.setImageResource(getConditionImage(code));


                    // Start update each view item that needs
                    int i = 1;
                    String unit = prefs.getString("temp_unit", null).toUpperCase();
                    forecast1TextView.setText(forecaseDateFormat.format(weather.forecast.get(i).date) + "\n" +
                            "H: " + weather.forecast.get(i).tempMax + " °" + unit + "\nL: " + weather.forecast.get(i).tempMin + " °" + unit);
                    f1ImageView.setImageResource(getConditionImage(weather.forecast.get(i).code));
                    i++;

                    forecast2TextView.setText(forecaseDateFormat.format(weather.forecast.get(i).date) + "\n" +
                            "H: " + weather.forecast.get(i).tempMax + " °" + unit + "\nL: " +  weather.forecast.get(i).tempMin + " °" + unit);
                    f2ImageView.setImageResource(getConditionImage(weather.forecast.get(i).code));
                    i++;

                    forecast3TextView.setText(forecaseDateFormat.format(weather.forecast.get(i).date) + "\n" +
                            "H: " + weather.forecast.get(i).tempMax + " °" + unit + "\nL: " + weather.forecast.get(i).tempMin + " °" + unit);
                    f3ImageView.setImageResource(getConditionImage(weather.forecast.get(i).code));
                    i++;

                    forecast4TextView.setText(forecaseDateFormat.format(weather.forecast.get(i).date) + "\n" +
                            "H: " + weather.forecast.get(i).tempMax + " °" + unit + "\nL: " + weather.forecast.get(i).tempMin + " °" + unit);
                    f4ImageView.setImageResource(getConditionImage(weather.forecast.get(i).code));
                    i++;

                    forecast5TextView.setText(forecaseDateFormat.format(weather.forecast.get(i).date) + "\n" +
                            "H: " + weather.forecast.get(i).tempMax + " °" + unit + "\nL: " + weather.forecast.get(i).tempMin + " °" + unit);
                    f5ImageView.setImageResource(getConditionImage(weather.forecast.get(i).code));
                    i++;

                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, WeatherPreferencesActivity.class);
            startActivity(i);
            //return true;
        }
        else if (id == R.id.action_refresh) {
            refreshItem = item;
            refreshData();
        } else if (id == R.id.action_change_location) {
            Intent i = new Intent(this, CityFinderActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    public int getConditionImage(int code) {
        switch (code) {
            case 0:  //	tornado
                return R.drawable.windy;
            case 1:  //	tropical storm
                return R.drawable.rain_heavy;
            case 2:  //	hurricane
                return R.drawable.rain_heavy;
            case 3:  //	severe thunderstorms
                return R.drawable.thunderstorms;
            case 4:  //	thunderstorms
                return R.drawable.thunderstorms;
            case 5:  //	mixed rain and snow
                return R.drawable.rain_s_snow;
            case 6:  //	mixed rain and sleet
                return R.drawable.rain_s_snow;
            case 7:  //	mixed snow and sleet
                return R.drawable.snow_s_rain;
            case 8:  //	freezing drizzle
                return R.drawable.rain_light;
            case 9:  //	drizzle
                return R.drawable.rain_light;
            case 10:  //	freezing rain
                return R.drawable.rain;
            case 11:  //	showers
                return R.drawable.rain;
            case 12:  //	showers
                return R.drawable.rain;
            case 13:  //	snow flurries
                return R.drawable.snow_heavy;
            case 14:  //	light snow showers
                return R.drawable.snow_light;
            case 15:  //	blowing snow
                return R.drawable.snow_heavy;
            case 16:  //	snow
                return R.drawable.snow;
            case 17:  //	hail
                return R.drawable.rain;
            case 18:  //	sleet
                return R.drawable.rain_light;
            case 19:  //	dust
                return R.drawable.windy;
            case 20:  //	foggy
                return R.drawable.fog;
            case 21:  //	haze
                return R.drawable.fog;
            case 22:  //	smoky
                return R.drawable.fog;
            case 23:  //	blustery
                return R.drawable.fog;
            case 24:  //	windy
                return R.drawable.windy;
            case 25:  //	cold
                return R.drawable.cloudy;
            case 26:  //	cloudy
                return R.drawable.cloudy;
            case 27:  //	mostly cloudy (night)
                return R.drawable.partly_cloudy;
            case 28:  //	mostly cloudy (day)
                return R.drawable.partly_cloudy;
            case 29:  //	partly cloudy (night)
                return R.drawable.partly_cloudy;
            case 30:  //	partly cloudy (day)
                return R.drawable.partly_cloudy;
            case 31:  //	clear (night)
                return R.drawable.cloudy;
            case 32:  //	sunny
                return R.drawable.sunny;
            case 33:  //	fair (night)
                return R.drawable.cloudy;
            case 34:  //	fair (day)
                return R.drawable.sunny;
            case 35:  //	mixed rain and hail
                return R.drawable.rain_s_snow;
            case 36:  //	hot
                return R.drawable.sunny;
            case 37:  //	isolated thunderstorms
                return R.drawable.rain_light;
            case 38:  //	scattered thunderstorms
                return R.drawable.thunderstorms;
            case 39:  //	scattered thunderstorms
                return R.drawable.rain_light;
            case 40:  //	scattered showers
                return R.drawable.rain;
            case 41:  //	heavy snow
                return R.drawable.snow_heavy;
            case 42:  //	scattered snow showers
                return R.drawable.snow_light;
            case 43:  //	heavy snow
                return R.drawable.snow_heavy;
            case 44:  //	partly cloudy
                return R.drawable.partly_cloudy;
            case 45:  //	thundershowers
                return R.drawable.thunderstorms;
            case 46:  //	snow showers
                return R.drawable.snow;
            case 47:  //	isolated thundershowers
                return R.drawable.thunderstorms;
            case 3200:  //	not available

        }
        return 0;
    }

    public String getWindDirection(int dirValue) {
        if (dirValue >= 338 || dirValue <= 22){
            // N
            return "N ";
        } else if (dirValue >= 23 && dirValue <= 67) {
            // NE
            return "NE ";
        } else if (dirValue >= 68 && dirValue <= 112) {
            // E
            return "E ";
        } else if (dirValue >= 113 && dirValue <= 157) {
            // SE
            return "SE ";
        } else if (dirValue >= 158 && dirValue <= 202) {
            // S
            return "S ";
        } else if (dirValue >= 203 && dirValue <= 247) {
            // SW
            return "SW ";
        } else if (dirValue >= 258 && dirValue <= 292) {
            // W
            return "W ";
        } else if (dirValue >= 293 && dirValue <= 337) {
            // NW
            return "NW ";
        } else {
            return "";
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            Log.d("DEBUG", String.format("%s: %f", mLatitudeLabel, mLastLocation.getLatitude()));
            Log.d("DEBUG", String.format("%s: %f", mLongitudeLabel, mLastLocation.getLongitude()));
            //mLatitudeText.setText(String.format("%s: %f", mLatitudeLabel, mLastLocation.getLatitude()));
            //mLongitudeText.setText(String.format("%s: %f", mLongitudeLabel, mLastLocation.getLongitude()));
            //Toast.makeText(this, String.format("%s: %f", mLatitudeLabel, mLastLocation.getLatitude()) + " : " + String.format("%s: %f", mLongitudeLabel, mLastLocation.getLongitude()), Toast.LENGTH_LONG).show();
        } else {
            //Toast.makeText(this, "NO LOCATION DETECTED", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("APP ERROR", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("APP ERROR", "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }
}
