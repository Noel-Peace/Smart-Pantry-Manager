package com.smartpantry.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.app.R;
import com.smartpantry.app.logic.MatchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter for the Suggested Recipes screen.
 * The same adapter shows the strict list (no "missing" line) and the "Almost There" list
 * (with a "Missing: ..." line).
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClick(MatchResult match);
    }

    private final List<MatchResult> matches = new ArrayList<>();
    private final Listener listener;
    private boolean showMissing = false;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setMatches(List<MatchResult> newMatches, boolean showMissing) {
        matches.clear();
        matches.addAll(newMatches);
        this.showMissing = showMissing;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MatchResult match = matches.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(match.getRecipe().getName());
        holder.tvInfo.setText(context.getString(R.string.recipe_ingredient_count,
                match.getRecipe().getIngredients().size()));

        if (showMissing) {
            holder.tvMissing.setText(context.getString(R.string.recipe_missing, match.describeUnmet()));
            holder.tvMissing.setVisibility(View.VISIBLE);
        } else {
            holder.tvMissing.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(match));
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvInfo;
        final TextView tvMissing;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_recipe_name);
            tvInfo = itemView.findViewById(R.id.tv_recipe_info);
            tvMissing = itemView.findViewById(R.id.tv_recipe_missing);
        }
    }
}
