package com.example.financialcompanion;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoreItemAdapter extends RecyclerView.Adapter<StoreItemAdapter.ViewHolder> {

    private List<Item> items;
    private OnItemClickListener onItemClickListener;
    private List<Boolean> itemSelectionState; // Track item selection state
    private Set<String> purchasedItemIds;

    public StoreItemAdapter(List<Item> items, OnItemClickListener onItemClickListener, Set<String> purchasedItemIds) {
        this.items = items;
        this.onItemClickListener = onItemClickListener;
        this.itemSelectionState = new ArrayList<>(Collections.nCopies(items.size(), false)); // Initialize selection state
        this.purchasedItemIds = (purchasedItemIds != null) ? new HashSet<>(purchasedItemIds) : new HashSet<>(); // Avoid null
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.itemName.setText(item.getName());
        holder.coinsNeeded.setText("Coins: " + item.getCoinsNeeded());
        holder.itemImage.setImageResource(item.getImageResourceId());

        // Log the entire contents of purchasedItemIds
        Log.d("StoreItemAdapter", "Purchased Item IDs: " + purchasedItemIds.toString());

        // Check if item has already been purchased
        if (purchasedItemIds.contains(item.getId())) {

            holder.addToCartButton.setText("Sold Out");
            holder.addToCartButton.setBackgroundColor(Color.LTGRAY); // Indicate "Sold Out" state
            holder.addToCartButton.setEnabled(false); // Disable button
        } else {
            // Set button state based on selection if not purchased
            if (itemSelectionState.get(position)) {
                holder.addToCartButton.setText("Added");
                holder.addToCartButton.setBackgroundColor(Color.GRAY);
            } else {
                holder.addToCartButton.setText("Add Item");
                int greenColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.green);
                holder.addToCartButton.setBackgroundColor(greenColor);
            }

            // Button click to toggle selection state
            holder.addToCartButton.setOnClickListener(v -> {
                boolean isSelected = !itemSelectionState.get(position);
                itemSelectionState.set(position, isSelected);
                notifyItemChanged(position);

                // Notify dialog fragment of selection change
                onItemClickListener.onItemClick(item, isSelected);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Item item, boolean isSelected);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, coinsNeeded;
        ImageView itemImage;
        Button addToCartButton;

        public ViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            coinsNeeded = itemView.findViewById(R.id.item_coins_needed);
            itemImage = itemView.findViewById(R.id.item_image);
            addToCartButton = itemView.findViewById(R.id.add_to_cart_button);
        }
    }
}


