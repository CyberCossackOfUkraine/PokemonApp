package com.example.glidepicture.models;

import android.os.Parcel;
import android.os.Parcelable;

public class PokemonsResultsModel{

    private String name;
    private String url;
    private int number;

    public int getNumber() {
        String[] urlParts = url.split("/");
        return Integer.parseInt(urlParts[urlParts.length - 1]);
    }

    public String getName() {
        return name;
    }


    public String getUrl() {
        return url;
    }

}
