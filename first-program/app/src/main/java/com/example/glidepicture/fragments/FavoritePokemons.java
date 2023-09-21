package com.example.glidepicture.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.glidepicture.Constants;
import com.example.glidepicture.JsonPlaceHolderApi;
import com.example.glidepicture.R;
import com.example.glidepicture.adapters.FavoritePokemonsAdapter;
import com.example.glidepicture.models.PokemonsModel;
import com.example.glidepicture.models.PokemonsResultsModel;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FavoritePokemons extends Fragment implements View.OnClickListener {

    // Fragments
    private OpenedPokemonFragment openedPokemonFragment;
    // Other
    private List<Integer> pokemonsFromDatabase;
    private RecyclerView recyclerView;
    private TextView tvNoPokemons;
    private Button bRetryConnection;
    private ProgressBar pbLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Animation
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setReenterTransition((new MaterialSharedAxis(MaterialSharedAxis.X, false)));

        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        setReturnTransition((new MaterialSharedAxis(MaterialSharedAxis.X, true)));
        // Animation

        handleOnBackButton();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_pokemons, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        bRetryConnection.setOnClickListener(this);

        getFavoritePokemons(view);
    }

    private void findViews(View view) {
        View rootView = getActivity().findViewById(R.id.root);
        recyclerView = view.findViewById(R.id.rv_pokemon_list);
        tvNoPokemons = view.findViewById(R.id.tv_no_pokemons);
        bRetryConnection = view.findViewById(R.id.b_retry_connection);
        pbLoading = view.findViewById(R.id.pb_loading);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }

    public void getFavoritePokemons(View view) {
        if (!isNetworkConnected()) {
            showConnectionError();
        } else {
            pbLoading.setVisibility(View.GONE);
            bRetryConnection.setVisibility(View.GONE);

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Favorites");

            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    pokemonsFromDatabase = new ArrayList<>();

                    for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {

                        pokemonsFromDatabase.add(Integer.parseInt(Objects.requireNonNull(favoriteSnapshot.getKey())));

                    }
                    showFavoritePokemons(view);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };

            rootRef.addListenerForSingleValueEvent(eventListener);
        }
    }

    public void showFavoritePokemons(View view) {
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
                ArrayList<PokemonsResultsModel> pokemonsList = pokemonModel.getResults();

                ArrayList<PokemonsResultsModel> favoritePokemons = new ArrayList<>();

                for (int i = 0; i < pokemonsList.size(); i++) {
                    for(int x = 0; x < pokemonsFromDatabase.size(); x++) {
                        if (pokemonsList.get(i).getNumber() == pokemonsFromDatabase.get(x)) {
                            favoritePokemons.add(pokemonsList.get(i));
                        }
                    }
                }
                if (favoritePokemons.size() > 0) {
                    startRecycler(view, favoritePokemons);
                } else {
                    tvNoPokemons.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<PokemonsModel> call, Throwable t) {
                Log.e(Constants.TAG, " onFailure: " + t.getMessage());
            }
        });

    }

    public void startRecycler(View view, ArrayList<PokemonsResultsModel> pokemonsList) {
        FavoritePokemonsAdapter.OnPokemonClickListener pokemonClickListener = new FavoritePokemonsAdapter.OnPokemonClickListener() {
            @Override
            public void onPokemonClick(PokemonsResultsModel pokemonResultsModel, int position) {
                openedPokemonFragment = new OpenedPokemonFragment();

                Bundle bundle = new Bundle();
                bundle.putInt(Constants.keyNumber, pokemonResultsModel.getNumber());
                openedPokemonFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container_view, openedPokemonFragment);
                fragmentTransaction.addToBackStack(openedPokemonFragment.getClass().getSimpleName());
                fragmentTransaction.commit();
            }
        };

        FavoritePokemonsAdapter adapter = new FavoritePokemonsAdapter(getContext(), pokemonsList, pokemonClickListener);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }

    public void showConnectionError() {
        Toast.makeText(getContext(), "No Internet connection.", Toast.LENGTH_SHORT).show();
        pbLoading.setVisibility(View.GONE);
        bRetryConnection.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_retry_connection:
                pbLoading.setVisibility(View.VISIBLE);
                getFavoritePokemons(view);
                break;
        }
    }

    public void handleOnBackButton() {
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getParentFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        Menu menu = toolbar.getMenu();
        MenuItem searchIcon = menu.findItem(R.id.action_search);
        searchIcon.setVisible(false);
    }
}