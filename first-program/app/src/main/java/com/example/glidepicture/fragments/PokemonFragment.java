package com.example.glidepicture.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.glidepicture.Constants;
import com.example.glidepicture.PokemonsViewModel;
import com.example.glidepicture.R;
import com.example.glidepicture.adapters.PokemonsAdapter;
import com.example.glidepicture.models.PokemonsResultsModel;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.transition.MaterialSharedAxis;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

public class PokemonFragment extends Fragment implements PokemonsAdapter.OnPokemonClickListener {

    // Fragments
    private OpenedPokemonFragment openedPokemonFragment;
    // Others
    private ImageView ivBackground;
    private ImageButton ibArrowUp;
    private MaterialSearchView searchView;
    private int lastPosition; // Last RecyclerView position
    private RecyclerView recyclerView;
    private PokemonsViewModel pokemonsViewModel;
    private PokemonsAdapter adapter;
    private NavigationView navigationView;

    public PokemonFragment() {
        super(R.layout.fragment_pokemon);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setReenterTransition((new MaterialSharedAxis(MaterialSharedAxis.X, false)));

        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        setReturnTransition((new MaterialSharedAxis(MaterialSharedAxis.X, true)));

        handleOnBackPressed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pokemon, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        initVars();

        // Load a background
        Glide.with(this)
                .load("https://mfiles.alphacoders.com/892/thumb-1920-892085.png")
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivBackground);

        showPokemons();

        manageSearchBar(view);

        handleOnClickListener();
    }

    private void initVars() {
        adapter = new PokemonsAdapter(getContext(), this);
        startRecycler();
    }

    public void findViews(View v) {
        View rootView = getActivity().findViewById(R.id.root);
        searchView = rootView.findViewById(R.id.searchView);
        ivBackground = v.findViewById(R.id.iv_background);
        recyclerView = v.findViewById(R.id.rv_pokemon_list);
        ibArrowUp = v.findViewById(R.id.ib_arrow_up);
        navigationView = rootView.findViewById(R.id.navigation_view);
    }

    public void startRecycler() {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                lastPosition = layoutManager.findFirstVisibleItemPosition();
                if (lastPosition >= 10) {
                    viewVisibleAnimator(ibArrowUp);
                } else {
                    viewGoneAnimator(ibArrowUp);
                }
            }
        });
    }

    public void showPokemons() {
        pokemonsViewModel = new ViewModelProvider(getActivity()).get(PokemonsViewModel.class);
        pokemonsViewModel.getPokemonsList().observe(getViewLifecycleOwner(), new Observer<ArrayList<PokemonsResultsModel>>() {
            @Override
            public void onChanged(ArrayList<PokemonsResultsModel> pokemonsResultsModels) {
                adapter.setPokemons(pokemonsResultsModels);
            }
        });
    }

    public void manageSearchBar(View v) {
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pokemonsViewModel.getSearchedPokemons(newText).observe(getViewLifecycleOwner(), new Observer<ArrayList<PokemonsResultsModel>>() {
                    @Override
                    public void onChanged(ArrayList<PokemonsResultsModel> pokemonsResultsModels) {
                        adapter.setPokemons(pokemonsResultsModels);
                    }
                });
                return true;
            }
        });

    }

    private void viewGoneAnimator(final View view) {
        view.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

    private void viewVisibleAnimator(final View view) {
        view.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                    }
                });
    }

    public void handleOnBackPressed() {
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                searchView.closeSearch();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    public void handleOnClickListener() {
        ibArrowUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.scrollToPosition(0);
                viewGoneAnimator(ibArrowUp);
                lastPosition = 0;
            }
        });
    }

    @Override
    public void onPokemonClick(PokemonsResultsModel pokemonResultsModel, int position) {
        openedPokemonFragment = new OpenedPokemonFragment();

        searchView.closeSearch();

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.keyNumber, pokemonResultsModel.getNumber());
        openedPokemonFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, openedPokemonFragment);
        fragmentTransaction.addToBackStack(openedPokemonFragment.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(0).setChecked(true);
    }
}