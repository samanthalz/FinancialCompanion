package com.example.financialcompanion;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<Item> items;
    private InventoryDialogFragment.OnItemSelectedListener listener;

    // Listener interface to notify the parent fragment/activity
    public interface OnItemSelectedListener {
        void onItemSelected(int imageResourceId);
    }

    public InventoryAdapter(List<Item> items, InventoryDialogFragment.OnItemSelectedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.itemIcon.setImageResource(item.getImageResourceId());
        holder.itemName.setText(item.getName());

        holder.wearButton.setOnClickListener(v -> {
            // Handle the "Wear" action here
            Toast.makeText(holder.itemView.getContext(), "Wearing " + item.getName(), Toast.LENGTH_SHORT).show();

            String petType = "Cat"; // The pet type
            String itemName = item.getName(); // Get the item name (e.g., "black_sunglasses")

            // Log the petType and itemName to see what values are being passed
            Log.d("PetItemImageMapper", "Pet Type: " + petType);
            Log.d("PetItemImageMapper", "Item Name: " + itemName);

            // Get the image resource based on the pet type and item name
            int imageResourceId = PetItemImageMapper.getImageResource(petType, itemName);

            // Check if image resource was found
            if (imageResourceId != -1) {
                // Pass the image resource to the Inventory Dialog Fragment
                listener.onItemSelected(imageResourceId);
                Toast.makeText(holder.itemView.getContext(), "Item image found!", Toast.LENGTH_SHORT).show();
            } else {
                // Handle error if image resource not found
                Toast.makeText(holder.itemView.getContext(), "Item image not found!", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIcon;
        TextView itemName;
        Button wearButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.itemIcon);
            itemName = itemView.findViewById(R.id.itemName);
            wearButton = itemView.findViewById(R.id.wearButton);
        }
    }
}

