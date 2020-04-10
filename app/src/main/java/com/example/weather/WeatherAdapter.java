package com.example.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder>{
    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView iconImageView;
        public TextView weatherTextView;

        public ViewHolder(View itemView){
            super(itemView);

            iconImageView = (ImageView) itemView.findViewById(R.id.icon_image_view);
            weatherTextView = (TextView) itemView.findViewById(R.id.weather_text_view);
        }

    }

    private List<Weather> weatherList;

    public WeatherAdapter(List<Weather> weatherList){
        this.weatherList = weatherList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_weather, parent, false);
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Weather weather = this.weatherList.get(position);
        ImageView imageView = holder.iconImageView;
        TextView textView = holder.weatherTextView;
        imageView.setImageBitmap(weather.getBitmap());
        textView.setText(weather.toString());
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }
}
