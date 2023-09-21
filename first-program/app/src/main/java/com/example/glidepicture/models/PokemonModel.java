package com.example.glidepicture.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PokemonModel {
    private int id;
    private String name;
    private float height;
    private float weight;
    private List<Stat> stats = null;

    public List<Stat> getStats() {
        return stats;
    }

    public class Stat {

        @SerializedName("base_stat")
        private Integer baseStat;

        public Integer getBaseStat() {
            return baseStat;
        }
    }


    public float getWeight() {
        return weight;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getHeight() {
        return height;
    }


}


