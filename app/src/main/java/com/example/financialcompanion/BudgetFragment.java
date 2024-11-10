package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class BudgetFragment extends Fragment {
    private DatabaseReference mDatabase;
    private RecyclerView budgetRecyclerView;
    private BudgetAdapter mAdapter;
    private List<Budget> mBudgetList;
    private TextView totalBudgetAmount, totalSpentAmount, noBudgetText, totalBudgetText, totalSpentText;
    private Button monthRangeDisplay;
    private TextView selectedMonthTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        budgetRecyclerView = view.findViewById(R.id.budgetRecyclerView);
        totalBudgetAmount = view.findViewById(R.id.totalBudgetAmount);
        totalSpentAmount = view.findViewById(R.id.totalSpentAmount);
        totalBudgetText = view.findViewById(R.id.totalBudgetText);
        totalSpentText = view.findViewById(R.id.totalSpentText);
        noBudgetText = view.findViewById(R.id.no_budget_text);
        monthRangeDisplay = view.findViewById(R.id.month_range_display);
        selectedMonthTextView = view.findViewById(R.id.selectedMonthTextView);

        // Set up RecyclerView
        budgetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize your data model and adapter
        mBudgetList = new ArrayList<>();
        mAdapter = new BudgetAdapter(mBudgetList);
        budgetRecyclerView.setAdapter(mAdapter);

        // Set the default date (based on the current date)
        Calendar calendar = Calendar.getInstance();
        int defaultMonth = calendar.get(Calendar.MONTH);  // Current month
        int defaultYear = calendar.get(Calendar.YEAR);    // Current year

        // Set default selectedMonthRange to current month and year in "MMMM yyyy" format
        selectedMonthRange = getMonthYearString(defaultMonth, defaultYear);

        // Set the formatted date in the TextView
        selectedMonthTextView.setText(selectedMonthRange);

        monthRangeDisplay.setOnClickListener(v -> showDatePicker(defaultMonth, defaultYear));

        // Load budget data for the current month and year
        loadBudgetData();

        return view;
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
            Log.d("BudgetFragment", "Back pressed from origin: " + originFragment);

            // Check the origin fragment and navigate accordingly
            if ("home".equals(originFragment)) {
                // Check if the action exists in the navigation graph
                navController.navigate(R.id.action_budgetFragment_to_homeFragment);

            } else {
                // If the origin is not specified, just go back normally
                navController.popBackStack();
                Log.d("BudgetFragment", "Navigating back to previous fragment.");
            }
        });
    }

    private String selectedMonthRange;  // Store the current selected month/year range

    private void loadBudgetData() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Fetch the budget data for the selected month range from Firebase
        mDatabase.child("users").child(userId).child("budget")
                .orderByChild("monthYear").equalTo(selectedMonthRange) // Filter by month range
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Initialize an empty list to hold budget items
                            List<Budget> budgets = new ArrayList<>();
                            double totalBudget = 0;
                            final double[] totalSpent = {0};

                            // Loop through the budgets and calculate total budget and total spent
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Budget budget = snapshot.getValue(Budget.class);
                                if (budget != null) {
                                    totalBudget += budget.getAmount();
                                    budgets.add(budget); // Add budget to the list

//                                    // Manually access the transactions from the snapshot and calculate total spent
//                                    DataSnapshot transactionsSnapshot = snapshot.child("transactions");
//                                    List<Transaction> transactions = new ArrayList<>();
//                                    for (DataSnapshot transactionSnapshot : transactionsSnapshot.getChildren()) {
//                                        Transaction transaction = transactionSnapshot.getValue(Transaction.class);
//                                        if (transaction != null) {
//                                            transactions.add(transaction);
//                                        }
//                                    }
//
//                                    // Calculate total spent using the list of transactions
//                                    totalSpent += getSpentAmount(transactions);
                                    // Reference to user's categories and accounts in the database
                                    DatabaseReference categoriesRef = mDatabase.child("users").child(userId).child("categories").child("expense");
                                    DatabaseReference accountsRef = mDatabase.child("users").child(userId).child("accounts");

                                    // Map to store categoryId -> categoryName for expense categories
                                    Map<String, String> categoryMap = new HashMap<>();

                                    categoriesRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot categoriesSnapshot) {
                                            // Populate the category map with categoryId as the key and categoryName as the value
                                            for (DataSnapshot categorySnapshot : categoriesSnapshot.getChildren()) {
                                                String categoryId = categorySnapshot.getKey();
                                                String categoryName = categorySnapshot.child("name").getValue(String.class);

                                                if (categoryId != null && categoryName != null) {
                                                    categoryMap.put(categoryId, categoryName);
                                                }
                                            }

                                            // After loading categories, access accounts and filter transactions based on budget category
                                            accountsRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot accountsSnapshot) {
                                                    List<Transaction> matchingTransactions = new ArrayList<>();

                                                    for (DataSnapshot accountSnapshot : accountsSnapshot.getChildren()) {
                                                        // Access each account's transactions
                                                        DataSnapshot transactionsSnapshot = accountSnapshot.child("transactions");

                                                        for (DataSnapshot transactionSnapshot : transactionsSnapshot.getChildren()) {
                                                            Transaction transaction = transactionSnapshot.getValue(Transaction.class);

                                                            if (transaction != null && "expense".equals(transaction.getType())) {
                                                                // Retrieve the category name using categoryId from the transaction
                                                                String transactionCategoryName = categoryMap.get(transaction.getCategoryId());

                                                                // Check if the transaction's category name matches the budget category name
                                                                if (budget.getCategory().equals(transactionCategoryName)) {
                                                                    matchingTransactions.add(transaction);
                                                                }
                                                            }
                                                        }
                                                    }

                                                    // Calculate total spent based on matching transactions
                                                    totalSpent[0] = getSpentAmount(matchingTransactions);
                                                    // Use totalSpent as needed (e.g., update UI or save to database)
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    Log.e("DatabaseError", "Failed to retrieve accounts: " + databaseError.getMessage());
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.e("DatabaseError", "Failed to retrieve categories: " + databaseError.getMessage());
                                        }
                                    });
                                }
                            }

                            // Update the UI with total budget and spent amounts
                            totalBudgetAmount.setText("RM " + totalBudget);
                            totalSpentAmount.setText("RM " + totalSpent[0]);

                            // Log to check how many budgets were added to the list
                            Log.d("BudgetFragment", "Total budgets added: " + budgets.size());

                            // Set up RecyclerView with budget items
                            mAdapter = new BudgetAdapter(budgets);
                            budgetRecyclerView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();
                            Log.d("BudgetFragment", "Adapter set to RecyclerView.");

                            noBudgetText.setVisibility(View.GONE); // Hide "No budgets" text
                            budgetRecyclerView.setVisibility(View.VISIBLE);
                            totalBudgetAmount.setVisibility(View.VISIBLE);
                            totalSpentAmount.setVisibility(View.VISIBLE);
                            totalBudgetText.setVisibility(View.VISIBLE);
                            totalSpentText.setVisibility(View.VISIBLE);
                        } else {
                            // Show "No budgets" message if no data is found
                            noBudgetText.setVisibility(View.VISIBLE);
                            budgetRecyclerView.setVisibility(View.GONE);
                            totalBudgetAmount.setVisibility(View.GONE);
                            totalSpentAmount.setVisibility(View.GONE);
                            totalBudgetText.setVisibility(View.GONE);
                            totalSpentText.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                    }
                });
    }

    // Utility function to format month and year to "MMMM yyyy" string
    private String getMonthYearString(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        // Define the display format for month and year
        String myFormat = "MMMM yyyy"; // E.g., "January 2023"
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    // Function to calculate total spent from the list of transactions
    private double getSpentAmount(List<Transaction> transactions) {
        double totalSpent = 0;

        // Sum up the spent amounts for each transaction
        if (transactions != null) {
            for (Transaction transaction : transactions) {
                totalSpent += transaction.getAmount();
            }
        }

        return totalSpent;
    }

    private void showDatePicker(int currentMonth, int currentYear) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentMonth);
        calendar.set(Calendar.YEAR, currentYear);

        // Define the display format for month and year
        String myFormat = "MMMM yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        // Create a DatePickerDialog with day hidden
        DatePickerDialog monthDatePickerDialog = new DatePickerDialog(
                requireContext(),
                AlertDialog.THEME_HOLO_LIGHT,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);

                    // Update the TextView with the selected month and year
                    selectedMonthTextView.setText(sdf.format(calendar.getTime()));

                    // Update the selectedMonthRange and load data based on the selected month and year
                    selectedMonthRange = getMonthYearString(month, year);
                    loadBudgetData();  // Reload data for the selected month range
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                // Hide the day spinner
                int daySpinnerId = getResources().getIdentifier("day", "id", "android");
                if (daySpinnerId != 0) {
                    View daySpinner = getDatePicker().findViewById(daySpinnerId);
                    if (daySpinner != null) {
                        daySpinner.setVisibility(View.GONE);
                    }
                }
            }
        };

        monthDatePickerDialog.setTitle("Select Month and Year");
        monthDatePickerDialog.show();
    }

}