package com.example.maps;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {
    @SerializedName("main")
    private MainWeatherInfo main;

    @SerializedName("weather")
    private WeatherCondition[] weatherConditions;

    public MainWeatherInfo getMain() {
        return main;
    }

    public WeatherCondition[] getWeatherConditions() {
        return weatherConditions;
    }
}
