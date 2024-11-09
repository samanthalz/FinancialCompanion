package com.example.financialcompanion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryDialogFragment extends DialogFragment {

    private List<Item> purchasedItems;
    private OnItemSelectedListener itemSelectedListener;
    private TextView noItemsTextView;

    // Method to set the listener (this can be called by the parent fragment/activity)
    public void setItemSelectedListener(OnItemSelectedListener listener) {
        this.itemSelectedListener = listener;
    }


    // Interface for the listener
    public interface OnItemSelectedListener {
        void onItemSelected(int imageResourceId, String itemName);  // Added itemName
    }

    public static InventoryDialogFragment newInstance(List<Item> items) {
        InventoryDialogFragment fragment = new InventoryDialogFragment();
        fragment.purchasedItems = items;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory_dialog, container, false);

        RecyclerView inventoryRecyclerView = view.findViewById(R.id.inventoryRecyclerView);
        inventoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        InventoryAdapter adapter = new InventoryAdapter(purchasedItems, itemSelectedListener);
        inventoryRecyclerView.setAdapter(adapter);

        noItemsTextView = view.findViewById(R.id.noItemsTextView);

        // Check if the purchasedItems list is empty
        if (purchasedItems != null && purchasedItems.isEmpty()) {
            // Show "No items purchased" message and hide the RecyclerView
            noItemsTextView.setVisibility(View.VISIBLE);
            inventoryRecyclerView.setVisibility(View.GONE);
        } else {
            // Set up the RecyclerView with the adapter
            adapter = new InventoryAdapter(purchasedItems, itemSelectedListener);
            inventoryRecyclerView.setAdapter(adapter);
            noItemsTextView.setVisibility(View.GONE); // Hide the message if there are items
            inventoryRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Adjust dialog size here
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT); // Adjust height as needed
        }

        // Find the close button by its ID
        ImageView closeButton = view.findViewById(R.id.close_btn);

        // Set a click listener to dismiss the dialog
        closeButton.setOnClickListener(v -> {
            // Dismiss the dialog when the close button is clicked
            dismiss();
        });
    }
}

