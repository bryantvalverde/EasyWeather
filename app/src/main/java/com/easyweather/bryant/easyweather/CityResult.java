package com.easyweather.bryant.easyweather;

/**
 * Created by Bryant on 2017-04-04.
 */

public class CityResult {

    private String woeid;
    private String cityName;
    private String country;
    private String region;

    public CityResult() {}

    public CityResult(String woeid, String cityName, String country, String region) {
        this.woeid = woeid;
        this.cityName = cityName;
        this.country = country;
        this.region = region;
    }

    public String getWoeid() {
        return woeid;
    }

    public void setWoeid(String woeid) {
        this.woeid = woeid;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() { return region; }

    public void setRegion(String region) { this.region = region; }

    @Override
    public String toString() {
        return cityName + " " + region + ", " + country;
    }
}