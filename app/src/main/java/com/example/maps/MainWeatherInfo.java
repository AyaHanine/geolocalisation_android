package com.example.maps;

import com.google.gson.annotations.SerializedName;

public class MainWeatherInfo {
    @SerializedName("temp")
    private double temperature;

    // Ajoutez un getter pour accéder à la température

    public double getTemperature() {
        return temperature;
    }
}
