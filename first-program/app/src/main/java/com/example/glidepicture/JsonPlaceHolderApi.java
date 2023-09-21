package com.example.glidepicture;

import com.example.glidepicture.models.PokemonModel;
import com.example.glidepicture.models.PokemonsModel;
import com.example.glidepicture.models.PokemonsResultsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface JsonPlaceHolderApi {

    String BASE_URL = "https://pokeapi.co/api/v2/";

    @GET("pokemon/?limit=1000")
    Call<PokemonsModel> getPokemons();

    @GET("pokemon/{number}/")
    Call<PokemonModel> getPokemon(@Path("number") String number);

}
