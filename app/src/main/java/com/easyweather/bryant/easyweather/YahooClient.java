package com.easyweather.bryant.easyweather;

import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.io.StringReader;

/**
 * Created by Bryant on 2017-04-04.
 */

public class YahooClient {
    public static String YAHOO_GEO_URL =  "https://query.yahooapis.com/v1";
    public static String YAHOO_WEATHER_URL = "https://query.yahooapis.com/v1";
    public static int MAX_FORECAST_LENGTH = 9; // 10 in total because first one is the current day forecast

    private static String APPID = "8f405d4c0bc2079d39666490dac85a32fc2ebd17";

    public static List<CityResult> getCityList(String cityName) {
        List<CityResult> result = new ArrayList<CityResult>();
        HttpURLConnection yahooHttpConn = null;
        try {
            String query =makeQueryCityURL(cityName);
            yahooHttpConn = (HttpURLConnection) (new URL(query)).openConnection();
            yahooHttpConn.connect();
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(yahooHttpConn.getInputStream()));
            int event = parser.getEventType();

            CityResult cty = null;
            String tagName = null;
            String currentTag = null;

            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("place")) {
                        cty = new CityResult();
                    }
                    currentTag = tagName;

                }
                else if (event == XmlPullParser.TEXT) {
                    Log.d("DEBUG", parser.getText());
                    if ("woeid".equals(currentTag))
                        cty.setWoeid(parser.getText());
                    else if ("name".equals(currentTag))
                        cty.setCityName(parser.getText());
                    else if ("country".equals(currentTag))
                        cty.setCountry(parser.getText());
                    else if ("admin1".equals(currentTag))
                        cty.setRegion(parser.getText());

                }
                else if (event == XmlPullParser.END_TAG) {
                    if ("place".equals(tagName))
                        result.add(cty);
                }

                event = parser.next();
            }
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try {
                yahooHttpConn.disconnect();
            }
            catch(Throwable ignore) {}

        }
        return result;
    }

    public static void getWeather(String woeid, String unit, RequestQueue rq, final WeatherClientListener listener) {
        String url2Call = makeWeatherURL(woeid, unit);
        final Weather result = new Weather();
        StringRequest req = new StringRequest(Request.Method.GET, url2Call, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                parseResponse(s, result);
                listener.onWeatherResponse(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        rq.add(req);
    }

    private static Weather parseResponse (String resp, Weather result) {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new StringReader(resp));

            String tagName = null;
            String currentTag = null;

            int event = parser.getEventType();
            boolean isFirstDayForecast = true;
            int i = 0;
            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();

                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("yweather:wind")) {
                        result.wind.chill = Integer.parseInt(parser.getAttributeValue(null, "chill"));
                        result.wind.direction = Integer.parseInt(parser.getAttributeValue(null, "direction"));
                        result.wind.speed  = (int) Float.parseFloat(parser.getAttributeValue(null, "speed"));
                    }
                    else if (tagName.equals("yweather:atmosphere")) {
                        result.atmosphere.humidity = Integer.parseInt(parser.getAttributeValue(null, "humidity"));
                        result.atmosphere.visibility = Float.parseFloat(parser.getAttributeValue(null, "visibility"));
                        result.atmosphere.pressure = Float.parseFloat(parser.getAttributeValue(null, "pressure"));
                        result.atmosphere.rising = Integer.parseInt(parser.getAttributeValue(null, "rising"));
                    }
                    else if (tagName.equals("yweather:forecast")) {
                        Forecast temp = new Forecast();
                        if (i != 0) {
                            temp.date = new SimpleDateFormat("dd MMM yyyy").parse(parser.getAttributeValue(null, "date"));
                            //result.forecast.get(i).date = new SimpleDateFormat("dd MMM yyyy").parse(parser.getAttributeValue(null, "date"));
                        }
                        //result.forecast.get(i).code = Integer.parseInt(parser.getAttributeValue(null, "code"));
                        temp.code = Integer.parseInt(parser.getAttributeValue(null, "code"));
                        //result.forecast.get(i).tempMin = Integer.parseInt(parser.getAttributeValue(null, "low"));
                        temp.tempMin = Integer.parseInt(parser.getAttributeValue(null, "low"));
                        //result.forecast.get(i).description = parser.getAttributeValue(null, "text");
                        temp.description = parser.getAttributeValue(null, "text");
                        //result.forecast.get(i).tempMax = Integer.parseInt(parser.getAttributeValue(null, "high"));
                        temp.tempMax = Integer.parseInt(parser.getAttributeValue(null, "high"));
                        result.forecast.add(temp);
                        i++;

                    }
                    else if (tagName.equals("yweather:condition")) {
                        result.condition.code = Integer.parseInt(parser.getAttributeValue(null, "code"));
                        result.condition.description = parser.getAttributeValue(null, "text");
                        result.condition.temp = Integer.parseInt(parser.getAttributeValue(null, "temp"));
                        result.condition.date = parser.getAttributeValue(null, "date");
                    }
                    else if (tagName.equals("yweather:units")) {
                        result.units.temperature = "°" + parser.getAttributeValue(null, "temperature");
                        result.units.pressure = parser.getAttributeValue(null, "pressure");
                        result.units.distance = parser.getAttributeValue(null, "distance");
                        result.units.speed = parser.getAttributeValue(null, "speed");
                    }
                    else if (tagName.equals("yweather:location")) {
                        result.location.name = parser.getAttributeValue(null, "city");
                        result.location.region = parser.getAttributeValue(null, "region");
                        result.location.country = parser.getAttributeValue(null, "country");
                    }
                    else if (tagName.equals("image"))
                        currentTag = "image";
                    else if (tagName.equals("url")) {
                        if (currentTag == null) {
                            result.imageUrl = parser.getAttributeValue(null, "src");
                        }
                    }
                    else if (tagName.equals("lastBuildDate")) {
                        currentTag="update";
                    }
                    else if (tagName.equals("yweather:astronomy")) {
                        result.astronomy.sunRise = parser.getAttributeValue(null, "sunrise");
                        result.astronomy.sunSet = parser.getAttributeValue(null, "sunset");
                    }

                }
                else if (event == XmlPullParser.END_TAG) {
                    if ("image".equals(currentTag)) {
                        currentTag = null;
                    }
                }
                else if (event == XmlPullParser.TEXT) {
                    if ("update".equals(currentTag))
                        result.lastUpdate = parser.getText();
                }
                event = parser.next();
            }

        }
        catch(Throwable t) {
            t.printStackTrace();
        }

        return result;
    }

    private static String makeQueryCityURL(String cityName) {
        cityName = cityName.replaceAll(" ", "%20");
        return YAHOO_GEO_URL + "/public/yql?q=select%20*%20from%20geo.places%20where%20text%3D%22"+ cityName + "%22";
    }

    private static String makeWeatherURL(String woeid, String unit) {
        return YAHOO_WEATHER_URL + "/public/yql?q=select%20*%20from%20weather.forecast%0Awhere%20u%3D'" + unit.toUpperCase() + "'%0Aand%20woeid%20in%20(" + woeid + ")";
    }


    public static interface WeatherClientListener {
        public void onWeatherResponse(Weather weather);
    }
}


