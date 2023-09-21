package com.example.glidepicture.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.glidepicture.R;
import com.example.glidepicture.models.PokemonsResultsModel;
import com.github.florent37.glidepalette.BitmapPalette;
import com.github.florent37.glidepalette.GlidePalette;

import java.util.List;

public class FavoritePokemonsAdapter extends RecyclerView.Adapter<FavoritePokemonsAdapter.ViewHolder>  {
    public interface OnPokemonClickListener {
        void onPokemonClick(PokemonsResultsModel pokemonResultsModel, int position);
    }

    private final FavoritePokemonsAdapter.OnPokemonClickListener onClickListener;
    private final LayoutInflater inflater;
    private final List<PokemonsResultsModel> pokemons;


    public FavoritePokemonsAdapter(Context context, List<PokemonsResultsModel> pokemons, FavoritePokemonsAdapter.OnPokemonClickListener onClickListener) { // Тут мы получаем данные из МейнАктивити.
        this.inflater = LayoutInflater.from(context);
        this.pokemons = pokemons;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public FavoritePokemonsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.pokemons_recycle_item, parent, false);
        return new FavoritePokemonsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritePokemonsAdapter.ViewHolder holder, int position) {
        PokemonsResultsModel pokemon = pokemons.get(position);
        String imagePokemon = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/" + pokemon.getNumber() + ".png";

        Glide.with(holder.itemView.getContext())
                .load(imagePokemon)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(
                        GlidePalette.with(imagePokemon).use(BitmapPalette.Profile.MUTED_LIGHT)
                                .intoCallBack(new BitmapPalette.CallBack() {
                                    @Override
                                    public void onPaletteLoaded(@Nullable Palette palette) {
                                        if (palette != null && palette.getDominantSwatch() != null) {
                                            int rgbHexCode = palette.getDominantSwatch().getRgb();
                                            holder.background.setColorFilter(rgbHexCode, PorterDuff.Mode.MULTIPLY);
                                        }
                                    }
                                }).crossfade(true))
                .into(holder.ivPokemonImage);

        holder.tvPokemonName.setText(pokemon.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onPokemonClick(pokemon, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return pokemons.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPokemonImage, ivPokemonBackground;
        final TextView tvPokemonName;
        Drawable background;

        ViewHolder(View view) {
            super(view);
            ivPokemonImage = view.findViewById(R.id.iv_pokemon_image);
            ivPokemonBackground = view.findViewById(R.id.iv_pokemon_background);
            tvPokemonName = view.findViewById(R.id.tv_pokemon_name);
            background = ivPokemonBackground.getBackground();
        }
    }
}
