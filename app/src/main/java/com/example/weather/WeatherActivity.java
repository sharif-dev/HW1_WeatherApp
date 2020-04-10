package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class WeatherActivity extends AppCompatActivity {
    private String weatherApiToken = "";
    private String weatherApiFormatStringQuery = "";
    private float[] coordinates = null;
    private int numberOfDays = 0;
    private ProgressBar progressBar;
    private Context context;
    private boolean networkAvailable;
    private final String saveFilePath = "saved.txt";
    private final String imageDirectory = "imageDir";

    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        this.context = this;
        weatherApiFormatStringQuery = getResources().getString(R.string.weather_api_format_string_query);
        weatherApiToken = getResources().getString(R.string.weather_api_token);
        numberOfDays = getResources().getInteger(R.integer.number_of_days);
        progressBar = findViewById(R.id.get_weather_progress_bar);
        Intent intent = getIntent();
        networkAvailable = intent.getBooleanExtra("network_available", false);
        if (networkAvailable) {
            coordinates = intent.getFloatArrayExtra("coordinates");
            int duration = Toast.LENGTH_SHORT;
            if (coordinates != null && coordinates.length == 2) {
                String message = String.format(getResources().getString(R.string.coordinates_format_string), coordinates[0], coordinates[1]);
                Toast toast = Toast.makeText(this, message, duration);
                toast.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!this.networkAvailable) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            String json = readFromFile(this);
            ArrayList<Weather> weathers = getWeathers(json);
            for(Weather weather : weathers){
                loadImageFromStorage(weather);
            }
            RecyclerView rvWeathers = findViewById(R.id.weather_recycler_view);
            WeatherAdapter weatherAdapter = new WeatherAdapter(weathers);
            rvWeathers.setAdapter(weatherAdapter);
            rvWeathers.setLayoutManager(new LinearLayoutManager(context));
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this, getResources().getString(R.string.no_connection_error), duration);
            toast.show();
            return;
        }
        final RequestQueue queue = Volley.newRequestQueue(this);
        float longitude = coordinates[0];
        float latitude = coordinates[1];
        final String query = String.format(weatherApiFormatStringQuery, latitude, longitude, weatherApiToken, numberOfDays);
        final WeatherActivity ctx = this;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, query,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        writeToFile(response, context);
                        final ArrayList<Weather> weathers = getWeathers(response);
                        for (int i = 0; i < weathers.size(); i++) {
                            final Weather weather = weathers.get(i);
//                            final boolean makeVisible = (i == 0);
                            final boolean makeInvisible = (i == weathers.size() - 1);
                            ImageRequest imageRequest = new ImageRequest(weather.getIconLink(),
                                    new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            saveWeatherBitmap(response, weather.getSaveFileName());
                                            weather.setBitmap(response);
                                            if (makeInvisible) {
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                        RecyclerView rvWeathers = findViewById(R.id.weather_recycler_view);
                                                        WeatherAdapter weatherAdapter = new WeatherAdapter(weathers);
                                                        rvWeathers.setAdapter(weatherAdapter);
                                                        rvWeathers.setLayoutManager(new LinearLayoutManager(context));
                                                    }
                                                });
                                            }
                                        }
                                    }, 0, 0, null,
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            int duration = Toast.LENGTH_LONG;
                                            Toast toast = Toast.makeText(context, getResources().getString(R.string.weather_partial_data_error), duration);
                                            toast.show();
                                        }
                                    });
                            queue.add(imageRequest);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(ctx, getResources().getString(R.string.weather_total_data_error), Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }
        });
    }

    public Weather getWeather(String weatherJson, boolean current) {
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(weatherJson);
        String date = "";
        float avgTemp = 0, windSpeed = 0, totalPrecipitation = 0, totalHumidity = 0;
        String iconLink = "";
        if (root != null && root.isJsonObject()) {
            if (current) {
                JsonObject jsonObject = root.getAsJsonObject();
                date = jsonObject.get("last_updated").getAsString().split(" ")[0];
                avgTemp = jsonObject.get("temp_c").getAsFloat();
                windSpeed = jsonObject.get("wind_kph").getAsFloat();
                totalPrecipitation = jsonObject.get("precip_mm").getAsFloat();
                totalHumidity = jsonObject.get("humidity").getAsFloat();
                JsonElement condition = jsonObject.getAsJsonObject("condition");
                if (condition != null && condition.isJsonObject()) {
                    iconLink = condition.getAsJsonObject().get("icon").getAsString();
                }
            } else {
                JsonObject jsonObject = root.getAsJsonObject();
                date = jsonObject.get("date").getAsString();
                JsonElement day = jsonObject.get("day");
                if (day != null && day.isJsonObject()) {
                    jsonObject = day.getAsJsonObject();
                    avgTemp = jsonObject.get("avgtemp_c").getAsFloat();
                    windSpeed = jsonObject.get("maxwind_kph").getAsFloat();
                    totalPrecipitation = jsonObject.get("totalprecip_mm").getAsFloat();
                    totalHumidity = jsonObject.get("avghumidity").getAsFloat();
                    JsonElement condition = jsonObject.get("condition");
                    if (condition != null && condition.isJsonObject()) {
                        iconLink = condition.getAsJsonObject().get("icon").getAsString();
                    }
                }
            }
        }
        Weather weather = new Weather.Builder(date)
                .withAvgTemp(avgTemp)
                .withWindSpeed(windSpeed)
                .withTotalPrecipitation(totalPrecipitation)
                .withTotalHumidity(totalHumidity)
                .withIconLink(iconLink)
                .build();
        return weather;
    }

    private static String imageDownloadTAG = "ImageDownload: ";

    public ImageRequest getImageDownloadReq(String url, final Weather weather) {
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        weather.setBitmap(response);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(imageDownloadTAG, error.toString());
                    }
                });
        return imageRequest;
    }

    public ArrayList<Weather> getWeathers(String jsonResponse) {
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(jsonResponse);
        ArrayList<Weather> weathers = new ArrayList<>();
        if (root.isJsonObject()) {
            String current = root.getAsJsonObject().get("current").toString();
            Weather current_weather = getWeather(current, true);
            weathers.add(current_weather);
            JsonElement forecast = root.getAsJsonObject().get("forecast");
            if (forecast != null && forecast.isJsonObject()) {
                JsonArray forecastDay = forecast.getAsJsonObject().getAsJsonArray("forecastday");
                for (int i = 1; i < forecastDay.size(); i++) {
                    String dayWeatherString = forecastDay.get(i).toString();
                    Weather weather = getWeather(dayWeatherString, false);
                    weathers.add(weather);
                }
            }
        }
        return weathers;
    }

    public void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(saveFilePath, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception: ", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {
        String ret = "";
        try {
            InputStream inputStream = context.openFileInput(saveFilePath);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private String saveWeatherBitmap(Bitmap bitmap, String path) {
        if (bitmap == null)
            return "";
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(imageDirectory, Context.MODE_PRIVATE);
        File mypath = new File(directory, path);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(Weather weather) {
        try {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir(imageDirectory, Context.MODE_PRIVATE);
            File f = new File(directory, weather.getSaveFileName());
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            weather.setBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
