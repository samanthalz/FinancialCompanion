package com.example.financialcompanion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class AddBudgetFragment extends Fragment {

    private TextView currentMonthTextView;
    private Spinner categorySpinner;
    private String userId;
    private Button saveButton;
    private TextInputEditText budgetAmountEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_budget, container, false);

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Initialize Views
        currentMonthTextView = view.findViewById(R.id.currentMonthTextView);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        saveButton = view.findViewById(R.id.saveButton);
        budgetAmountEditText = view.findViewById(R.id.budgetAmountEditText);

        // Set onClickListener for the Save button
        saveButton.setOnClickListener(v -> saveBudgetToDatabase());


        // Set the current month and year on the TextView
        setCurrentMonthYear();

        // Fetch the categories from Firebase (pass userId)
        fetchCategories(userId);

        return view;
    }

    private void saveBudgetToDatabase() {
        // Get the selected category and budget amount
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String budgetAmountString = Objects.requireNonNull(budgetAmountEditText.getText()).toString();

        if (budgetAmountString.isEmpty()) {
            budgetAmountEditText.setError("Please enter a budget amount");
            return;
        }

        double budgetAmount = Double.parseDouble(budgetAmountString);

        // Get current month and year for display or storage
        String currentMonthYear = currentMonthTextView.getText().toString();
        currentMonthYear = currentMonthYear.replace("Budget for: ", "");

        // Create a Budget object
        Budget newBudget = new Budget(selectedCategory, budgetAmount, currentMonthYear);


        // Save the Budget data to Firebase under users/uid/budget
        DatabaseReference budgetRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("budget");

        // Push the new budget to Firebase and get the generated ID
        String budgetId = budgetRef.push().getKey(); // Get Firebase-generated ID

        // Set the ID on the Budget object
        newBudget.setId(budgetId);

        // Save the Budget object with the generated ID
        assert budgetId != null;
        budgetRef.child(budgetId).setValue(newBudget)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Budget saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to save budget", Toast.LENGTH_SHORT).show();
                    }
                    // Clear the TextInputEditText regardless of success or failure
                    budgetAmountEditText.setText(""); // Clear the field
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the origin fragment info from arguments
        String originFragment = getArguments() != null ? getArguments().getString("originFragment") : "";

        // Initialize the toolbar and set the navigation icon click listener
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar); // Set the toolbar as the action bar

        // Get the NavController for this fragment
        NavController navController = Navigation.findNavController(view);

        // Enable the Up button and set the back arrow icon
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24); // Set your back arrow icon

        // Set Navigation click listener for the back button
        toolbar.setNavigationOnClickListener(v -> {
            Log.d("ManageAccountsFragment", "Back pressed from origin: " + originFragment);

            // Check the origin fragment and navigate accordingly
            if ("home".equals(originFragment)) {
                // Check if the action exists in the navigation graph
                navController.navigate(R.id.action_addBudgetFragment_to_homeFragment);

            } else if ("account".equals(originFragment)) {
                navController.navigate(R.id.action_addBudgetFragment_to_accountFragment);

            } else if ("pet".equals(originFragment)) {
                navController.navigate(R.id.action_addBudgetFragment_to_petFragment);

            } else if ("courses".equals(originFragment)) {
                navController.navigate(R.id.action_addBudgetFragment_to_coursesFragment);

            } else {
                // If the origin is not specified, just go back normally
                navController.popBackStack();
                Log.d("AddBudgetFragment", "Navigating back to previous fragment.");
            }
        });
    }

    private void fetchCategories(String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("categories").child("expense");

        Log.d("BudgetFragment", "Attempting to fetch expense categories from Firebase...");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                Category vectorCategory = null;

                // Iterate over categories from Firebase and populate the list
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null) {
                        if (category.getVectorResource() == R.drawable.baseline_add_36_grey) { // Check if it's the vector category
                            vectorCategory = category; // Store it to add later
                        } else {
                            categories.add(category); // Add other categories normally
                            Log.d("BudgetExpense", "Added category: " + category);
                        }
                    }
                }

                // Add the vector category at the end of the list if it exists
                if (vectorCategory != null) {
                    categories.add(vectorCategory); // Add the vector category at the end
                    Log.d("BudgetExpense", "Moved vector category to the end: " + vectorCategory);
                }

                // Set the adapter if categories are not empty
                if (!categories.isEmpty()) {
                    setCategoryAdapter(categories);
                } else {
                    Log.d("BudgetExpense", "No expense categories found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching categories: " + databaseError.getMessage());
            }
        });
    }

    private void setCategoryAdapter(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return; // Handle the empty case if necessary
        }

        // Filter out categories with the name "Create"
        List<Category> filteredCategories = new ArrayList<>();
        for (Category category : categories) {
            if (!category.getName().equals("Create")) { // Check if the category name is not "Create"
                filteredCategories.add(category); // Add it to the new list
            }
        }

        // Create an array to hold the filtered category names
        String[] categoryNames = new String[filteredCategories.size()];

        // Fill the array with data from the filtered categories list
        for (int i = 0; i < filteredCategories.size(); i++) {
            categoryNames[i] = filteredCategories.get(i).getName(); // Get the category name
        }

        // Create an ArrayAdapter for the Spinner using the filtered category names
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter on the Spinner
        categorySpinner.setAdapter(categoryAdapter);
    }

    // Method to get and display current month and year
    private void setCurrentMonthYear() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based, so add 1
        int year = calendar.get(Calendar.YEAR);

        String currentMonthYear = "Budget for: " + getMonthName(month) + " " + year;
        currentMonthTextView.setText(currentMonthYear);
    }

    // Helper method to get the month name from the month number
    private String getMonthName(int month) {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return months[month - 1]; // Adjust for 0-based index
    }
}
