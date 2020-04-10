package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private String mapBoxToken = "";
    private String mapboxFormatStringQuery = "";
    private ArrayList<String> currentPlaces = new ArrayList<>();
    private ProgressBar progressBar;

    Handler handler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.activity_main_progress_bar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        mapBoxToken = getResources().getString(R.string.mapbox_token);
        mapboxFormatStringQuery = getResources().getString(R.string.mapbox_format_string_query);
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(!isNetworkAvailable()){
            Intent intent = new Intent(this, WeatherActivity.class);
            intent.putExtra("network_available", false);
            startActivity(intent);
        }
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        String userInput = editText.getText().toString();
        String query = String.format(mapboxFormatStringQuery, userInput, mapBoxToken);
        RequestQueue queue = Volley.newRequestQueue(this);
        final MainActivity ctx = this;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, query,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        final ArrayList<Place> places = getPlaces(response);
                        String[] placeStrings = new String[places.size()];
                        for (int i = 0; i < places.size(); i++)
                            placeStrings[i] = places.get(i).toString();
                        ListView listView = findViewById(R.id.string_list_view);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, R.layout.simple_text_item, placeStrings);
                        if(listView != null) {
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    float[] coordinates = {places.get(position).getLongitude(), places.get(position).getLatitude()};
                                    Intent intent = new Intent(ctx, WeatherActivity.class);
                                    intent.putExtra("coordinates", coordinates);
                                    intent.putExtra("network_available", true);
                                    startActivity(intent);
                                }
                            });
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(ctx, getResources().getString(R.string.coordination_error), duration);
                        toast.show();
                    }
        });
        queue.add(stringRequest);
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }
        });
    }

    public static ArrayList<Place> getPlaces(String mapboxResponse){
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(mapboxResponse);
        if(root.isJsonObject()){
            JsonObject rootObject = root.getAsJsonObject();
            JsonElement features = rootObject.get("features");
            if(features != null){
                ArrayList<Place> places = new ArrayList<>();
                JsonArray items = features.getAsJsonArray();
                for(JsonElement item : items){
                    JsonObject itemAsJsonObject = item.getAsJsonObject();
                    String text = "";
                    String place_name = "";
                    float longitude = 0;
                    float latitude = 0;
                    JsonElement element = itemAsJsonObject.get("text");
                    if(element != null)
                        text = element.getAsString();
                    element = itemAsJsonObject.get("place_name");
                    if(element != null)
                        place_name = element.getAsString();
                    element = itemAsJsonObject.get("center");
                    if(element != null){
                        JsonArray longitudeLatitude = element.getAsJsonArray();
                        if(longitudeLatitude != null && longitudeLatitude.size() >= 2){
                            longitude = longitudeLatitude.get(0).getAsFloat();
                            latitude = longitudeLatitude.get(1).getAsFloat();
                        }
                    }
                    Place place = new Place(text, place_name, longitude, latitude);
                    places.add(place);
                }
                return places;
            }
        }
        return new ArrayList<>();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
