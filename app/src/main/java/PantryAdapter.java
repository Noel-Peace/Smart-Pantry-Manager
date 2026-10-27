package com.smartpantry.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartpantry.app.R;
import com.smartpantry.app.logic.ExpiryUtils;
import com.smartpantry.app.logic.QuantityFormatter;
import com.smartpantry.app.model.PantryItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Custom RecyclerView adapter that turns the list of {@link PantryItem}s (read from SQLite)
 * into one card per ingredient.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** The adapter reports clicks to the Activity through this interface. */
    public interface Listener {
        void onItemClick(PantryItem item);

        void onDeleteClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean alertsEnabled = true;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces the displayed data and redraws the list. */
    public void setItems(List<PantryItem> newItems, boolean alertsEnabled) {
        items.clear();
        items.addAll(newItems);
        this.alertsEnabled = alertsEnabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PantryItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(item.getName());
        holder.tvQty.setText(QuantityFormatter.withUnit(item.getQuantity(), item.getUnit()));
        bindExpiry(holder.tvExpiry, context, item);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    private void bindExpiry(TextView view, Context context, PantryItem item) {
        String expiry = item.getExpiryDate();
        Integer days = ExpiryUtils.daysUntil(expiry, Calendar.getInstance());

        if (days == null) {
            view.setText(R.string.no_expiry);
            view.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        } else if (alertsEnabled && days < 0) {
            view.setText(context.getString(R.string.expired_ago, -days));
            view.setTextColor(ContextCompat.getColor(context, R.color.warn_red));
        } else if (alertsEnabled && days == 0) {
            view.setText(R.string.expires_today);
            view.setTextColor(ContextCompat.getColor(context, R.color.warn_red));
        } else if (alertsEnabled && days <= ExpiryUtils.EXPIRING_SOON_DAYS) {
            view.setText(context.getString(R.string.expires_in, days));
            view.setTextColor(ContextCompat.getColor(context, R.color.warn_orange));
        } else {
            view.setText(context.getString(R.string.expires_on, expiry.trim()));
            view.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvQty;
        final TextView tvExpiry;
        final MaterialButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvQty = itemView.findViewById(R.id.tv_item_qty);
            tvExpiry = itemView.findViewById(R.id.tv_item_expiry);
            btnDelete = itemView.findViewById(R.id.btn_item_delete);
        }
    }
}
