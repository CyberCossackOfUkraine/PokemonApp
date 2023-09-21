package com.example.glidepicture.models;

import java.util.ArrayList;

//@JsonIgnoreProperties({"base_experience","is_default","order","weight","abilities","forms",
//        "game_indices", "held_items", "location_area_encounters", "moves", "species", "sprites",
//        "stats", "types"})

public class PokemonsModel {

   private ArrayList<PokemonsResultsModel> results;

    public ArrayList<PokemonsResultsModel> getResults() {
        return results;
    }

    public void setResults(ArrayList<PokemonsResultsModel> results) {
        this.results = results;
    }
}
