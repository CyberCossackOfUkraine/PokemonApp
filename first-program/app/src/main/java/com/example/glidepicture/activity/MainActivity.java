package com.example.glidepicture.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.glidepicture.R;
import com.example.glidepicture.fragments.FavoritePokemons;
import com.example.glidepicture.fragments.PokemonFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

public class MainActivity extends AppCompatActivity {

    // Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar materialToolbar;
    private MaterialSearchView searchView;
    // Fragments
    private PokemonFragment pokemonFragment;
    private FavoritePokemons favoritePokemons;
    // Others
    private FirebaseUser user;
    private TextView tvUsername;
    private Toolbar toolbar;
    private Menu menuList;
    private MenuItem searchIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVars();

        findViews();

        navigationView.getMenu().getItem(0).setChecked(true); // Highlight Pokemon item in drawer

        handleOnClickListener();

        enableSearch();

        setUsername();

        openFragment(pokemonFragment);
    }

    public void initVars() {
        pokemonFragment = new PokemonFragment();
        favoritePokemons = new FavoritePokemons();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void findViews() {
        materialToolbar = findViewById(R.id.navigation_menu_button);
        drawerLayout = findViewById(R.id.root);
        navigationView = findViewById(R.id.navigation_view);

        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);

        View headerView = navigationView.getHeaderView(0);
        tvUsername = headerView.findViewById(R.id.tv_nickname_header);
    }

    public void handleOnClickListener() {

        // Click open drawer.
        materialToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //  Click drawer items.
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            item.setChecked(true);
            drawerLayout.closeDrawer(GravityCompat.START);

            switch (id) {
                case R.id.nav_pokemons:
                    searchIcon.setVisible(true);
                    openFragment(pokemonFragment);
                    break;
                case R.id.nav_favorites:
                    searchIcon.setVisible(false);
                    openFragment(favoritePokemons);
                    break;
                case R.id.nav_logout:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class)); //Go back to home page
                    finish();
            }
            return true;
        });
    }

    private void enableSearch() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_item, menu);

                menuList = menu;
                searchIcon = menuList.findItem(R.id.action_search);

                MenuItem item = menu.findItem(R.id.action_search);
                searchView.setMenuItem(item);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;

                // Handle option Menu Here

            }
        }, this, Lifecycle.State.RESUMED);
    }

    public void setUsername() {
        // Get username.
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid()).child("nickname");

        // Set username.
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nickname = snapshot.getValue(String.class);
                    tvUsername.setText(nickname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, fragment);
        fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

}