package com.example.financialcompanion;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    // Method to set the listener (this can be called by the parent fragment/activity)
    public void setItemSelectedListener(OnItemSelectedListener listener) {
        this.itemSelectedListener = listener;
    }

    // Interface for the listener
    public interface OnItemSelectedListener {
        void onItemSelected(int imageResourceId);
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
    }

}

