package com.example.maps;

import com.google.gson.annotations.SerializedName;

public class WeatherCondition {
    @SerializedName("main")
    private String main;

    // Ajoutez un getter pour accéder à la condition météorologique

    public String getMain() {
        return main;
    }
}
