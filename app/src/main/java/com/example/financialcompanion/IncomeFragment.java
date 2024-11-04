package com.example.financialcompanion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class IncomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private SharedViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewIncome);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        Log.d("IncomeFragment", "User ID: " + userId); // Log the user ID

        // Check if categories are already set
        if (viewModel.getIncomeCategories().getValue() == null) {
            Log.d("IncomeFragment", "Income categories not set, fetching categories..."); // Log the attempt to fetch categories
            fetchCategories(userId); // Call this method to fetch categories
        } else {
            Log.d("IncomeFragment", "Income categories already set, setting adapter..."); // Log that categories are already set
            setCategoryAdapter(viewModel.getIncomeCategories().getValue());
        }
        return view;
    }

    private void fetchCategories(String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("categories").child("income");

        Log.d("IncomeFragment", "Attempting to fetch income categories from Firebase...");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                Category vectorCategory = null;
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null) {
                        if (category.getVectorResource() == 2131165310) { // Check if it's the vector category
                            vectorCategory = category; // Store it to add later
                            Log.d("IncomeFragment", "Found vector category with ID 2131165310: " + vectorCategory);
                        } else {
                            categories.add(category); // Add other categories normally
                            Log.d("IncomeFragment", "Added category: " + category);
                        }
                    }
                }
                Log.d("IncomeFragment", "Fetched " + categories.size() + " income categories.");

                // If the vector category was found, add it to the end of the list
                if (vectorCategory != null) {
                    categories.add(vectorCategory); // Add the vector category at the end
                    Log.d("IncomeFragment", "Moved vector category to the end: " + vectorCategory);
                }

                // Set categories in ViewModel
                viewModel.setIncomeCategories(categories);

                // Set the adapter only if categories are not null or empty
                if (!categories.isEmpty()) {
                    setCategoryAdapter(categories);
                } else {
                    Log.d("IncomeFragment", "No income categories found.");
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
            Log.d("IncomeFragment", "Categories list is null or empty. No categories to display."); // Log when categories are empty
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
            Log.d("IncomeFragment", "Category " + i + ": Name = " + categoryNames[i] + ", Vector Resource ID = " + vectorDrawableResources[i]); // Log each category
        }

        // Create and set the CategoryAdapter
        CategoryAdapter categoryAdapter = new CategoryAdapter(vectorDrawableResources, categoryNames, viewModel);
        recyclerView.setAdapter(categoryAdapter);
        Log.d("IncomeFragment", "CategoryAdapter set with " + categories.size() + " categories."); // Log the adapter setup
    }
}
