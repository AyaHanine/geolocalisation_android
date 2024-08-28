package com.example.maps;



import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlacesResponse {
    @SerializedName("results")
    private List<com.example.maps.Place> places;

    public List<com.example.maps.Place> getPlaces() {
        return places;
    }
}
