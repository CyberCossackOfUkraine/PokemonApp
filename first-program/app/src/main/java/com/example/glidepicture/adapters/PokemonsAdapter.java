package com.example.glidepicture.adapters;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NotifyDataSetChanged")
public class PokemonsAdapter extends RecyclerView.Adapter<PokemonsAdapter.ViewHolder> {

    public interface OnPokemonClickListener {
        void onPokemonClick(PokemonsResultsModel pokemonResultsModel, int position);
    }

    private final PokemonsAdapter.OnPokemonClickListener onClickListener;
    private final LayoutInflater inflater;
    private final List<PokemonsResultsModel> pokemons = new ArrayList<>();


    public PokemonsAdapter(Context context, PokemonsAdapter.OnPokemonClickListener onClickListener) { // Тут мы получаем данные из МейнАктивити.
        this.inflater = LayoutInflater.from(context);
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public PokemonsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.pokemons_recycle_item, parent, false);
        return new PokemonsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonsAdapter.ViewHolder holder, int position) {
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

        holder.itemView.setOnClickListener(view -> {
            onClickListener.onPokemonClick(pokemon, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return pokemons.size();
    }

    public void setPokemons(ArrayList<PokemonsResultsModel> pokemons) {
        if (!this.pokemons.isEmpty()) this.pokemons.clear();

        this.pokemons.addAll(pokemons);
        notifyDataSetChanged();
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
