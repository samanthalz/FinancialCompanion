package com.example.financialcompanion;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ExpenseFragment extends Fragment {

    private RecyclerView recyclerView;
    private SharedViewModel viewModel;
    private String userId;

    // Define the listener interface
    public interface OnCreateIconClickListener {
        void onCreateIconClicked();
    }

    private IncomeFragment.OnCreateIconClickListener createIconClickListener;

    // Set the listener from the adapter
    public void setOnCreateIconClickListener(IncomeFragment.OnCreateIconClickListener listener) {
        this.createIconClickListener = listener;
    }

    public void navigateToAddIncomeFragment() {
        // Navigate to AddIncomeFragment to cover the entire screen
        AddIncomeFragment addIncomeFragment = new AddIncomeFragment();

        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();

        // Replace the nav_host_fragment to display AddIncomeFragment full-screen
        transaction.replace(R.id.nav_host_fragment, addIncomeFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewExpense);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        // Set up the adapter with the drawable resources and category names
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));

       userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        // Check if categories are already set
        if (viewModel.getExpenseCategories().getValue() == null) {
            fetchCategories(userId); // Call this method to fetch categories
            setCategoryAdapter(viewModel.getExpenseCategories().getValue());
        } else {
            setCategoryAdapter(viewModel.getExpenseCategories().getValue());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the FragmentResultListener to listen for the refresh signal
        getParentFragmentManager().setFragmentResultListener("refreshRequest", getViewLifecycleOwner(), (requestKey, result) -> {
            // Check if the refresh signal is received
            if (result.containsKey("refresh")) {
                // Refresh the RecyclerView here
                fetchCategories(userId);
            }
        });
    }

    private void fetchCategories(String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("categories").child("expense");

        Log.d("ExpenseFragment", "Attempting to fetch expense categories from Firebase...");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                Category vectorCategory = null;
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    assert category != null;
                    if (category.getVectorResource() == R.drawable.baseline_add_36_grey) { // Check if it's the vector category
                        vectorCategory = category; // Store it to add later
                    } else {
                        categories.add(category); // Add other categories normally
                        Log.d("ExpenseFragment", "Added category: " + category);
                    }
                }
                Log.d("ExpenseFragment", "Fetched " + categories.size() + " expense categories.");

                // If the vector category was found, add it to the end of the list
                if (vectorCategory != null) {
                    categories.add(vectorCategory); // Add the vector category at the end
                    Log.d("ExpenseFragment", "Moved vector category to the end: " + vectorCategory);
                }

                // Set categories in ViewModel
                viewModel.setExpenseCategories(categories);

                // Set the adapter only if categories are not null or empty
                if (!categories.isEmpty()) {
                    setCategoryAdapter(categories);
                } else {
                    Log.d("ExpenseFragment", "No expense categories found.");
                    // Optionally, handle the case where there are no categories (e.g., show a placeholder)
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching categories: " + databaseError.getMessage());
            }
        });
    }

    private void setCategoryAdapter(List<Category> categories) {
        // Check if categories is null or empty
        if (categories == null || categories.isEmpty()) {
            // Handle the case where there are no categories, maybe show a placeholder or empty state
            return;
        }

        // Create arrays to hold category names and drawable resource IDs
        String[] categoryNames = new String[categories.size()];
        int[] vectorDrawableResources = new int[categories.size()];

        // Fill the arrays with data from the categories list
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getName(); // Get the category name
            vectorDrawableResources[i] = categories.get(i).getVectorResource(); // Get the vector drawable resource ID
            Log.d("ExpenseFragment", "Vector Resource ID: " + vectorDrawableResources[i]);

        }

        // Create and set the CategoryAdapter
        CategoryAdapter categoryAdapter = new CategoryAdapter(vectorDrawableResources, categoryNames, viewModel);
        recyclerView.setAdapter(categoryAdapter);
        // Set the listener to the adapter
        categoryAdapter.setOnCreateIconClickListener(new CategoryAdapter.OnCreateIconClickListener() {
            @Override
            public void onCreateIconClicked() {
                // When the listener is triggered, call the method in the fragment
                navigateToAddIncomeFragment();
            }
        });
    }

}
