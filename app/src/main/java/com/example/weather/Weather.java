package com.example.weather;

import android.graphics.Bitmap;
public class Weather {
    private String date = "";
    private float avgTemp = 0;
    private float windSpeed = 0;
    private float totalPrecipitation = 0;
    private float totalHumidity = 0;
    private String iconLink = "";
    private transient Bitmap bitmap;

    public static class Builder {
        private String date;
        private float avgTemp;
        private float windSpeed;
        private float totalPrecipitation;
        private float totalHumidity;
        private String iconLink;

        public Builder(String date) {
            this.date = date;
        }

        public Builder withAvgTemp(float avgTemp) {
            this.avgTemp = avgTemp;
            return this;
        }

        public Builder withWindSpeed(float windSpeed) {
            this.windSpeed = windSpeed;
            return this;
        }

        public Builder withTotalPrecipitation(float totalPrecipitation) {
            this.totalPrecipitation = totalPrecipitation;
            return this;
        }

        public Builder withTotalHumidity(float totalHumidity) {
            this.totalHumidity = totalHumidity;
            return this;
        }

        public Builder withIconLink(String iconLink) {
            this.iconLink = iconLink;
            return this;
        }

        public Weather build() {
            Weather weather = new Weather();
            weather.date = this.date;
            weather.avgTemp = this.avgTemp;
            weather.windSpeed = this.windSpeed;
            weather.totalPrecipitation = this.totalPrecipitation;
            weather.totalHumidity = this.totalHumidity;
            weather.iconLink = this.iconLink;
            return weather;
        }
    }

    @Override
    public String toString() {
        return String.format("Date: %s\nTemp: %.1f\nWind Speed: %.1f\nTotal Precipitation: %.1f\nTotal Humidity: %.1f", this.date, this.avgTemp, this.windSpeed, this.totalPrecipitation, this.totalHumidity);
    }

    public String getDate() {
        return date;
    }

    public float getAvgTemp() {
        return avgTemp;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public float getTotalPrecipitation() {
        return totalPrecipitation;
    }

    public float getTotalHumidity() {
        return totalHumidity;
    }

    public String getIconLink() {
//        iconLink = iconLink.replace("64x64", "128x128");
        return "https:" + iconLink;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getSaveFileName() {
        String[] temp = this.iconLink.split("/");
        String childPath = temp[temp.length - 1];
        return childPath;
    }


}