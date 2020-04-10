package com.example.weather;

public class Place {
    private String text;
    private String place_name;
    private float longitude;
    private float latitude;

    public Place(String text, String place_name, float longitude, float latitude) {
        this.text = text;
        this.place_name = place_name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public String getText() {
        return text;
    }

    public String getPlace_name() {
        return place_name;
    }

    @Override
    public String toString(){
        return this.place_name;
    }
}
