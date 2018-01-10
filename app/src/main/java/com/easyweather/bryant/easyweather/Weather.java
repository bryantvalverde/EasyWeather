package com.easyweather.bryant.easyweather;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Bryant on 2017-04-04.
 */

public class Weather {

    public String imageUrl;
    public Condition condition = new Condition();
    public Wind wind = new Wind();
    public Atmosphere atmosphere = new Atmosphere();
    public Location location = new Location();
    public Astronomy astronomy = new Astronomy();
    public Units units = new Units();
    //public Forecast forecast = new Forecast();
    public List<Forecast> forecast = new ArrayList<Forecast>();


    public String lastUpdate;

    public class Condition {
        public  String description;
        public  int code;
        public  String date;
        public  int temp;
    }



    public static class Atmosphere {
        public  int humidity;
        public  float visibility;
        public  float pressure;
        public  int rising;
    }

    public class Wind {
        public  int chill;
        public  int direction;
        public  int speed;
    }

    public class Units {
        public String speed;
        public String distance;
        public String pressure;
        public String temperature;
    }

    public class Location {
        public String name;
        public String region;
        public String country;
    }

    public class Astronomy {
        public String sunRise;
        public String sunSet;
    }

}

class Forecast {
    public  int tempMin;
    public  int tempMax;
    public  String description;
    public  int code;
    public  Date date;

    public Forecast() {
    }

    public Forecast(int tempMin, int tempMax, String description, int code, Date date) {
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.description = description;
        this.code = code;
        this.date = date;
    }

    public Forecast(int code, int tempMin, int tempMax, String description) {
        this.code = code;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.description = description;
    }
}