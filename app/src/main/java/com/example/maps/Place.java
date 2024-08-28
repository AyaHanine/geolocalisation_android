package com.example.maps;

import com.google.gson.annotations.SerializedName;

public class Place {
    @SerializedName("name")
    private String name;

    @SerializedName("vicinity")
    private String vicinity;

    @SerializedName("geometry")
    private Geometry geometry;

    public String getName() {
        return name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public Geometry getGeometry() {
        return geometry;
    }
}
