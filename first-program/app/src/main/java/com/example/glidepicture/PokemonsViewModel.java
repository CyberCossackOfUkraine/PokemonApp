package com.example.glidepicture;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.glidepicture.models.PokemonsModel;
import com.example.glidepicture.models.PokemonsResultsModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PokemonsViewModel extends ViewModel {

    private static final String TAG = "PokemonViewModel";
    private MutableLiveData<ArrayList<PokemonsResultsModel>> mutableLiveData;
    private ArrayList<PokemonsResultsModel> pokemonsList;

    public LiveData<ArrayList<PokemonsResultsModel>> getPokemonsList() {
        if (mutableLiveData == null) {
            mutableLiveData = new MutableLiveData<>();
        }
        getPokemons();
        return mutableLiveData;
    }

    private void getPokemons() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(JsonPlaceHolderApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<PokemonsModel> call = jsonPlaceHolderApi.getPokemons();

        call.enqueue(new Callback<PokemonsModel>() {
            @Override
            public void onResponse(Call<PokemonsModel> call, Response<PokemonsModel> response) {
                if (!response.isSuccessful()) {
                    Log.e(Constants.TAG, " onResponse: " + response.errorBody());
                    return;
                }

                PokemonsModel pokemonModel = response.body();
                pokemonsList = pokemonModel.getResults();

                mutableLiveData.setValue(pokemonsList);

            }

            @Override
            public void onFailure(Call<PokemonsModel> call, Throwable t) {
                Log.e(Constants.TAG, " onFailure: " + t.getMessage());
            }
        });
    }

    public LiveData<ArrayList<PokemonsResultsModel>> getSearchedPokemons(String searchText) {
        if (mutableLiveData == null) {
            mutableLiveData = new MutableLiveData<>();
        }
        searchPokemons(searchText);
        return mutableLiveData;
    }

    public void searchPokemons(String newText) {
        if (newText != null && !newText.isEmpty()) {
            ArrayList<PokemonsResultsModel> searchedPokemons = new ArrayList<>();

            for (PokemonsResultsModel item : pokemonsList) {
                if (item.getName().contains(newText)) {
                    searchedPokemons.add(item);
                }
            }

            mutableLiveData.setValue(searchedPokemons);
//            startRecycler(searchedPokemons);

        } else {
            mutableLiveData.setValue(pokemonsList);
        }
    };

}
