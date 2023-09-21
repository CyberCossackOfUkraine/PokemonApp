package com.example.glidepicture.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.glidepicture.Constants;
import com.example.glidepicture.JsonPlaceHolderApi;
import com.example.glidepicture.R;
import com.example.glidepicture.models.PokemonModel;
import com.github.florent37.glidepalette.BitmapPalette;
import com.github.florent37.glidepalette.GlidePalette;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skydoves.progressview.ProgressView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class OpenedPokemonFragment extends Fragment implements View.OnClickListener {

    private ImageView ivPokemonImage, ivFavorite;
    private Button bRetryConnection;
    private Drawable ivPokemonBackground;
    private TextView tvPokemonName, tvPokemonHeight, tvPokemonWeight;
    private ImageButton ibArrowBack;
    private ProgressView progressViewHp, progressViewAtk, progressViewDef, progressViewSpd;
    private ProgressBar pbLoading;
    private DatabaseReference dataBase;
    private FirebaseUser user;
    private PokemonModel pokemonModel;


    private int pokemonNumber;

    public OpenedPokemonFragment() {
        super(R.layout.fragment_opened_pokemon);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the number of the Pokémon that has been opened.
        if (getArguments() != null) {
            pokemonNumber = getArguments().getInt(Constants.keyNumber);
        }

        user = FirebaseAuth.getInstance().getCurrentUser();

        // Path to check Favorite button
        dataBase = FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid())
                .child("Favorites")
                .child(String.valueOf(pokemonNumber));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_opened_pokemon, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        handleOnBackPressed();

        hideSearchIcon();

        getPokemon();

        ibArrowBack.setOnClickListener(this);
        ivFavorite.setOnClickListener(this);
        bRetryConnection.setOnClickListener(this);
    }

    public void findViews(View view) {
        ivPokemonImage = view.findViewById(R.id.iv_pokemon_image);
        ivPokemonBackground = view.findViewById(R.id.iv_pokemon_background).getBackground();
        ivFavorite = view.findViewById(R.id.iv_favorite);
        tvPokemonName = view.findViewById(R.id.tv_pokemon_name);
        tvPokemonWeight = view.findViewById(R.id.tv_pokemon_weight);
        tvPokemonHeight = view.findViewById(R.id.tv_pokemon_height);
        ibArrowBack = view.findViewById(R.id.ib_arrow_back);
        bRetryConnection = view.findViewById(R.id.b_retry_connection);
        progressViewHp = view.findViewById(R.id.progress_hp);
        progressViewAtk = view.findViewById(R.id.progress_atk);
        progressViewDef = view.findViewById(R.id.progress_def);
        progressViewSpd = view.findViewById(R.id.progress_spd);
        pbLoading = view.findViewById(R.id.pb_loading);

    }

    public void hideSearchIcon() {
        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        Menu menu = toolbar.getMenu();
        MenuItem searchIcon = menu.findItem(R.id.action_search);
        searchIcon.setVisible(false);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }

    public void getPokemon() {
        if (!isNetworkConnected()) {
            showConnectionError();
        } else {
            pbLoading.setVisibility(View.GONE);
            bRetryConnection.setVisibility(View.GONE);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(JsonPlaceHolderApi.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

            Call<PokemonModel> call = jsonPlaceHolderApi.getPokemon(String.valueOf(pokemonNumber));

            call.enqueue(new Callback<PokemonModel>() {
                @Override
                public void onResponse(Call<PokemonModel> call, Response<PokemonModel> response) {
                    if (!response.isSuccessful()) {
                        Log.e(Constants.TAG, " onResponse: " + response.errorBody());
                        return;
                    }
                    pokemonModel = response.body();

                    showPokemon();
                }

                @Override
                public void onFailure(Call<PokemonModel> call, Throwable t) {
                    Log.e(Constants.TAG, " onFailure: " + t.getMessage());
                }
            });
        }
    }

    public void showPokemon() {
        String imagePokemon = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/" + pokemonNumber + ".png";

        Glide.with(requireActivity())
                .load(imagePokemon)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(
                        GlidePalette.with(imagePokemon).use(BitmapPalette.Profile.MUTED_LIGHT)
                                .intoCallBack(new BitmapPalette.CallBack() {
                                    @Override
                                    public void onPaletteLoaded(@Nullable Palette palette) {
                                        if (palette != null && palette.getDominantSwatch() != null) {
                                            int rgbHexCode = palette.getDominantSwatch().getRgb();
                                            ivPokemonBackground.setColorFilter(rgbHexCode, PorterDuff.Mode.MULTIPLY);

                                        }
                                    }
                                }).crossfade(true))
                .into(ivPokemonImage);

        tvPokemonName.setText(pokemonModel.getName());
        tvPokemonHeight.setText(pokemonModel.getHeight() / 10 + " M " + System.getProperty("line.separator") + "Height");
        tvPokemonWeight.setText(pokemonModel.getWeight() / 10 + " KG " + System.getProperty("line.separator") + "Weight");

        progressViewHp.setLabelText(getString(R.string.HP, pokemonModel.getStats().get(0).getBaseStat()));
        progressViewHp.setProgress(pokemonModel.getStats().get(0).getBaseStat());

        progressViewAtk.setLabelText(getString(R.string.ATK, pokemonModel.getStats().get(1).getBaseStat()));
        progressViewAtk.setProgress(pokemonModel.getStats().get(1).getBaseStat());

        progressViewDef.setLabelText(getString(R.string.DEF, pokemonModel.getStats().get(2).getBaseStat()));
        progressViewDef.setProgress(pokemonModel.getStats().get(2).getBaseStat());

        progressViewSpd.setLabelText(getString(R.string.SPD, pokemonModel.getStats().get(5).getBaseStat()));
        progressViewSpd.setProgress(pokemonModel.getStats().get(5).getBaseStat());

        // Favorite button check
        dataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ivFavorite.setImageResource(R.drawable.ic_heart_full);
                } else {
                    ivFavorite.setImageResource(R.drawable.ic_heart_empty);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void showConnectionError() {
        Toast.makeText(getContext(), "No Internet connection.", Toast.LENGTH_SHORT).show();
        pbLoading.setVisibility(View.GONE);
        bRetryConnection.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_arrow_back:
                getParentFragmentManager().popBackStack();
                break;
            case R.id.iv_favorite:
                favoritePressed();
                break;
            case R.id.b_retry_connection:
                pbLoading.setVisibility(View.VISIBLE);
                getPokemon();
                break;
        }
    }

    public void favoritePressed() {
        dataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Remove favorite pokemon
                    ivFavorite.setImageResource(R.drawable.ic_heart_empty);
                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(user.getUid())
                            .child("Favorites")
                            .child(String.valueOf(pokemonNumber))
                            .removeValue();
                } else {
                    // Add favorite pokemon
                    ivFavorite.setImageResource(R.drawable.ic_heart_full);
                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(user.getUid())
                            .child("Favorites")
                            .child(String.valueOf(pokemonNumber))
                            .setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void handleOnBackPressed() {
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getParentFragmentManager().popBackStack();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);
    }

}