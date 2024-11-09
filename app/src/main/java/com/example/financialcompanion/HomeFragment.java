package com.example.financialcompanion;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class HomeFragment extends Fragment {

    private PieChart pieChart;
    private String userId;
    private SharedViewModel viewModel;
    private List<Category> expenseCategories;
    private TextView incomeTextView; // Use your actual TextView ID
    private TextView expenseTextView;
    private TextView viewAllTextView;
    private RecyclerView recentTransactionsRecyclerView;
    private final List<Transaction> transactionList = new ArrayList<>();
    private RecentTransactionAdapter adapter;
    private NestedScrollView financialSummaryLayout;
    private BottomNavigationView bottomNav;
    private Button accountButton;
    private Button goalButton;
    private Button summaryButton;
    private Button budgetButton;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment first
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the PieChart view from the inflated layout
        pieChart = view.findViewById(R.id.pieChart);


        // Get the user ID, ensuring the user is logged in
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // Check if the expense categories are already available
        expenseCategories = viewModel.getExpenseCategories().getValue();
        if (expenseCategories == null) {
            Log.d("Expense", "Initial expense categories are null, fetching categories...");
            fetchCategories(userId, this::loadMonthlyExpenses); // Pass loadMonthlyExpenses as a callback
        } else {
            Log.d("Expense", "Initial expense categories have " + expenseCategories.size() + " items.");
            loadMonthlyExpenses(); // Call it directly if already available
        }

        incomeTextView = view.findViewById(R.id.incomeTextView);
        expenseTextView = view.findViewById(R.id.expenseTextView);

        incomeTextView.setText(""); // Empty initially
        expenseTextView.setText(""); // Empty initially


        // Obtain NavController from the current fragment
        navController = NavHostFragment.findNavController(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize RecyclerView
        recentTransactionsRecyclerView = view.findViewById(R.id.recentTransactionsRecyclerView);
        recentTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        financialSummaryLayout = view.findViewById(R.id.home_scroll);

        // Use ViewTreeObserver to wait until the layout is fully rendered
        ViewTreeObserver vto = bottomNav.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to avoid repeated calls
                bottomNav.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get screen height
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                Log.d("HomeFragment", "Screen Height: " + screenHeight);

                // Set the ScrollView height to the remaining height (screenHeight - 333)
                ViewGroup.LayoutParams scrollViewParams = financialSummaryLayout.getLayoutParams();
                scrollViewParams.height = screenHeight - 333;
                financialSummaryLayout.setLayoutParams(scrollViewParams);

                Log.d("HomeFragment", "ScrollView Height Set To: " + scrollViewParams.height);
            }
        });


        // Initialize adapter with the empty list and set it to the RecyclerView
        // adapter = new RecentTransactionAdapter(transactionList, expenseCategories);
        adapter = new RecentTransactionAdapter(getContext(), transactionList, expenseCategories);
        recentTransactionsRecyclerView.setAdapter(adapter);
        fetchCategories(userId);

        // Load the latest transactions
        loadLatestTransactions();

        accountButton = view.findViewById(R.id.acc_button);
        accountButton.setOnClickListener(v -> openManageAccountFragment());

        goalButton = view.findViewById(R.id.goal_button);
        goalButton.setOnClickListener(v -> openGoalsFragment());

        summaryButton = view.findViewById(R.id.summary_button);
        summaryButton.setOnClickListener(v -> openSummaryFragment());

        budgetButton = view.findViewById(R.id.budget_button);
        budgetButton.setOnClickListener(v -> openBudgetFragment());

        viewAllTextView = view.findViewById(R.id.viewAllTextView);
        viewAllTextView.setOnClickListener(v -> openViewAllTransactionFragment());
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreNavigationBar();
    }

    // Method to hide the navigation bar when navigating to another fragment
    private void hideNavigationBar() {
        // Use findViewById to reference the views
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        View bottomAppBar =  requireActivity().findViewById(R.id.bottom_app_bar);
        FloatingActionButton fabAdd =  requireActivity().findViewById(R.id.fab_add);
        View financialSummaryLayout =  requireActivity().findViewById(R.id.financial_summary_layout);

        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.GONE);
        }
        if (fabAdd != null) {
            fabAdd.setVisibility(View.GONE);
        }
        if (financialSummaryLayout != null) {
            financialSummaryLayout.setVisibility(View.GONE);
        }
    }

    // Method to restore the navigation bar visibility when coming back to Home
    private void restoreNavigationBar() {
        // Use findViewById to reference the views in your activity or fragment
        BottomNavigationView bottomNav =  requireActivity().findViewById(R.id.bottom_navigation);
        View bottomAppBar =  requireActivity().findViewById(R.id.bottom_app_bar);
        FloatingActionButton fabAdd =  requireActivity().findViewById(R.id.fab_add);
        View financialSummaryLayout =  requireActivity().findViewById(R.id.financial_summary_layout);

        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.VISIBLE);
        }
        if (fabAdd != null) {
            fabAdd.setVisibility(View.VISIBLE);
        }
        if (financialSummaryLayout != null) {
            financialSummaryLayout.setVisibility(View.VISIBLE);
        }
    }

    private void openGoalsFragment() {
        // Create a Bundle to pass the origin argument
        Bundle args = new Bundle();
        args.putString("originFragment", "home");  // Add the origin fragment info

        // Use NavController to navigate, passing the arguments
        navController.navigate(R.id.action_homeFragment_to_goalsFragment, args);

        hideNavigationBar();
    }

    private void openManageAccountFragment() {

        // Create a Bundle to pass the origin argument
        Bundle args = new Bundle();
        args.putString("originFragment", "home");  // Add the origin fragment info

        // Use NavController to navigate, passing the arguments
        navController.navigate(R.id.action_homeFragment_to_manageAccountsFragment, args);

        hideNavigationBar();
    }

    private void openViewAllTransactionFragment() {

        // Create a Bundle to pass the origin argument
        Bundle args = new Bundle();
        args.putString("originFragment", "home");  // Add the origin fragment info

        // Use NavController to navigate, passing the arguments
        navController.navigate(R.id.action_homeFragment_to_allTransactionFragment, args);

        hideNavigationBar();
    }

    private void openSummaryFragment() {

        // Create a Bundle to pass the origin argument
        Bundle args = new Bundle();
        args.putString("originFragment", "home");  // Add the origin fragment info

        // Use NavController to navigate, passing the arguments
        navController.navigate(R.id.action_homeFragment_to_summaryFragment, args);

        hideNavigationBar();
    }

    private void openBudgetFragment() {

        // Create a Bundle to pass the origin argument
        Bundle args = new Bundle();
        args.putString("originFragment", "home");  // Add the origin fragment info

        // Use NavController to navigate, passing the arguments
        navController.navigate(R.id.action_homeFragment_to_budgetFragment, args);

        hideNavigationBar();
    }

    private void fetchCategories(String userId) {
        // Create a reference for both expense and income categories
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("categories");

        Log.d("HomeFragment", "Attempting to fetch both expense and income categories from Firebase...");

        // Create a list to hold all categories
        List<Category> allCategories = new ArrayList<>();

        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Fetch expense categories
                DataSnapshot expenseSnapshot = dataSnapshot.child("expense");
                for (DataSnapshot categorySnapshot : expenseSnapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null) {
                        allCategories.add(category); // Add all expense categories
                        Log.d("HomeFragment", "Added expense category: " + category);
                    }
                }
                Log.d("HomeFragment", "Fetched " + allCategories.size() + " expense categories.");

                // Fetch income categories
                DataSnapshot incomeSnapshot = dataSnapshot.child("income");
                for (DataSnapshot categorySnapshot : incomeSnapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null) {
                        allCategories.add(category); // Add all income categories
                        Log.d("HomeFragment", "Added income category: " + category);
                    }
                }
                Log.d("HomeFragment", "Fetched " + allCategories.size() + " income categories.");

                // Update the adapter with all categories if not empty
                if (!allCategories.isEmpty()) {
                    adapter.setExpenseCategories(allCategories);
                    Log.d("HomeFragment", "Updated adapter with combined categories.");
                } else {
                    Log.d("HomeFragment", "No categories found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching categories: " + databaseError.getMessage());
            }
        });
    }

    private void loadLatestTransactions() {
        // Fetch the latest 5 transactions from the database
        fetchLatestTransactions();
        // Update the adapter with the latest transactions
        adapter.updateTransactions(transactionList);
    }

    private void fetchLatestTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("accounts");

        // Listener to get all accounts and their transactions
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot accountsSnapshot) {
                for (DataSnapshot accountSnapshot : accountsSnapshot.getChildren()) {
                    // Iterate through all transactions
                    for (DataSnapshot transactionSnapshot : accountSnapshot.child("transactions").getChildren()) {
                        Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                        if (transaction != null) {
                            // Check the type of the transaction
                            String type = transaction.getType(); // Assuming getType() returns the transaction type
                            if ("income".equals(type) || "expense".equals(type)) {
                                transactionList.add(transaction);
                                Log.d("TransactionInfo", type.substring(0, 1).toUpperCase() + type.substring(1) + " Transaction: " + transaction.toString() + " | Date: " + transaction.getDate());
                            }
                        }
                    }
                }

                // Sort transactions by timestamp and get the latest 5
                transactionList.sort(new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction t1, Transaction t2) {
                        return Long.compare(t2.getDate(), t1.getDate());
                    }
                });

                // Get the latest 5 transactions
                List<Transaction> latestTransactions = transactionList.size() > 5
                        ? transactionList.subList(0, 5)
                        : transactionList;

                // Update adapter with the latest transactions
                adapter.updateTransactions(latestTransactions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchCategories(String userId, Runnable onCategoriesFetched) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("categories").child("expense");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    categories.add(category);
                }
                viewModel.setExpenseCategories(categories);
                expenseCategories = viewModel.getExpenseCategories().getValue();
                onCategoriesFetched.run(); // Call the callback after fetching categories
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching categories: " + databaseError.getMessage());
            }
        });
    }

    private void loadMonthlyExpenses() {
        DatabaseReference userAccountsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("accounts");

        userAccountsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot accountsSnapshot) {
                for (DataSnapshot accountSnapshot : accountsSnapshot.getChildren()) {
                    String accountId = accountSnapshot.getKey(); // Get each account ID

                    // Reference to the transactions under the current account
                    DatabaseReference transactionsRef = accountSnapshot.child("transactions").getRef();

                    transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot transactionsSnapshot) {
                            // Get the current month and year
                            Calendar calendar = Calendar.getInstance();
                            int currentYear = calendar.get(Calendar.YEAR);
                            int currentMonth = calendar.get(Calendar.MONTH); // January is 0, December is 11

                            // Map to hold total expenses by category
                            Map<String, Float> categoryExpenseMap = new HashMap<>();
                            // Initialize variables to hold total income and expenses
                            float totalIncome = 0f;
                            float totalExpense = 0f;

                            for (DataSnapshot transactionSnapshot : transactionsSnapshot.getChildren()) {
                                // Access transaction data
                                String transactionId = transactionSnapshot.getKey(); // Transaction ID
                                Integer amountInt = transactionSnapshot.child("amount").getValue(Integer.class);
                                Integer categoryId = transactionSnapshot.child("categoryId").getValue(Integer.class);
                                String type = transactionSnapshot.child("type").getValue(String.class); // Assuming type is stored

                                // Initialize a variable to hold the timestamp
                                Long timestamp = null;

                                // Check if "date" node exists and retrieve the timestamp
                                if (transactionSnapshot.child("date").exists()) {
                                    timestamp = transactionSnapshot.child("date").getValue(Long.class); // Retrieve as long timestamp
                                }

                                Log.d("TransactionDate", "Retrieved Timestamp: " + timestamp + ", Current Month: " + currentMonth + ", Current Year: " + currentYear);

                                // Check if the timestamp was retrieved successfully
                                if (timestamp != null) {
                                    // Convert the timestamp to Calendar object for easier date manipulation
                                    Calendar transactionDate = Calendar.getInstance();
                                    transactionDate.setTimeInMillis(timestamp);

                                    int transactionMonth = transactionDate.get(Calendar.MONTH);  // Get the month from the timestamp
                                    int transactionYear = transactionDate.get(Calendar.YEAR);   // Get the year from the timestamp

                                    // Log the retrieved date
                                    Log.d("TransactionDate", "Transaction Date: " + transactionDate.getTime().toString());

                                    // Check if the transaction is from the current month and year
                                    if (transactionMonth == currentMonth && transactionYear == currentYear) {
                                        // The logic to check for transaction type and update the map
                                        if ("expense".equals(type)) {
                                            // Check if the categoryId and amountInt are not null
                                            if (categoryId != null && amountInt != null) {
                                                // Ensure category is not null before putting into the map
                                                String category = String.valueOf(categoryId); // Convert categoryId to String if needed

                                                // Use Optional to safely handle the update
                                                Float currentAmount = Optional.ofNullable(categoryExpenseMap.get(category)).orElse(0f);
                                                categoryExpenseMap.put(category, currentAmount + amountInt.floatValue());

                                                // Update totalExpense
                                                totalExpense += amountInt.floatValue();

                                                // Log the current state of categoryExpenseMap
                                                Log.d("CategoryExpenseMap", "Current categoryExpenseMap: " + categoryExpenseMap.toString());
                                            } else {
                                                // Handle the case where categoryId or amountInt is null
                                                Log.w("TransactionWarning", "Transaction with ID: " + transactionId + " has null category or amount.");
                                            }
                                        } else if ("income".equals(type)) {
                                            // Sum up the total income
                                            if (amountInt != null) {
                                                totalIncome += amountInt.floatValue();
                                            } else {
                                                // Handle the case where amountInt is null
                                                Log.w("TransactionWarning", "Income with ID: " + transactionId + " has null amount.");
                                            }
                                        }
                                    }
                                } else {
                                    // Handle case when date is missing
                                    Log.w("TransactionWarning", "Transaction with ID: " + transactionId + " has missing date.");
                                }
                            }

                            // Log the size of expenseCategories
                            if (expenseCategories == null || expenseCategories.isEmpty()) {
                                Log.d("Expense2", "Expense categories are null or empty.");
                            } else {
                                Log.d("Expense2", "Expense categories have " + expenseCategories.size() + " items.");
                            }

                            // Pass the category expense map to setup the pie chart
                            if (!categoryExpenseMap.isEmpty()) {
                                setupPieChart(categoryExpenseMap, expenseCategories);
                            } else {
                                // Empty case: Show "No Expense" with a single color
                                setupPieChart(new HashMap<>(), expenseCategories);
                            }

                            // Update the UI with total income and expenses
                            updateUI(totalIncome, totalExpense);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void updateUI(float totalIncome, float totalExpense) {
        // Create a fade-in animation
        Animation fadeIn = new AlphaAnimation(0, 1); // From fully transparent to fully opaque
        fadeIn.setDuration(100); // Adjust duration for smoothness

        if (totalIncome >= 0) {
            incomeTextView.setText(getString(R.string.rm_text_ph, totalIncome));
        } else {
            incomeTextView.setText(getString(R.string.rm_text_ph, 0.00f));
        }

        if (totalExpense >= 0) {
            expenseTextView.setText(getString(R.string.rm_text_ph, totalExpense));
        } else {
            expenseTextView.setText(getString(R.string.rm_text_ph, 0.00f));
        }

        // Start the fade-in animation
        incomeTextView.startAnimation(fadeIn);
        expenseTextView.startAnimation(fadeIn);
    }


    private void setupPieChart(Map<String, Float> categoryExpenseMap, List<Category> categoryList) {
        List<PieEntry> entries = new ArrayList<>();

        // Check if the map is empty
        if (categoryExpenseMap.isEmpty()) {
            // Display a full circle with "No Expenses" text
            pieChart.setCenterText("No Expenses Recorded");
        } else {
            for (Map.Entry<String, Float> entry : categoryExpenseMap.entrySet()) {
                // Use the category ID to get the name from the category list
                int categoryId = Integer.parseInt(entry.getKey());
                Log.d("CategoryID", "Category ID: " + categoryId); // Log the category ID
                String categoryName = "Unknown Category"; // Fallback name

                // Log the content of categoryList
                if (categoryList != null && !categoryList.isEmpty()) {
                    Log.d("CategoryList", "Categories in the list:");
                    for (Category category : categoryList) {
                        Log.d("CategoryList", "ID: " + category.getId() + ", Name: " + category.getName());
                    }
                } else {
                    Log.d("CategoryList", "Category list is empty or null.");
                }

                // Find the category by ID
                assert categoryList != null;
                for (Category category : categoryList) {
                    Log.d("CategorySearch", "Checking category vector resource: " + category.getVectorResource()); // Log the vector resource being checked
                    if (category.getVectorResource() == categoryId) { // Use == for int comparison
                        categoryName = category.getName(); // Get the name if vector resource matches
                        Log.d("CategorySearch", "Found category: " + categoryName + " for vector resource: " + categoryId); // Log when the category is found
                        break; // Exit the loop once the category is found
                    }
                }
                entries.add(new PieEntry(entry.getValue(), categoryName));
                Log.d("PieEntries", "Entries before pie chart creation: " + entries);
            }
            pieChart.setCenterText("Expenses by Category");
        }

        // Initialize the map for aggregated data
        Map<String, Float> aggregatedData = new HashMap<>();

        for (PieEntry entry : entries) {
            String label = entry.getLabel();
            float value = entry.getValue();

            // Log the processing entry
            Log.d("PieChartDebug", "Processing entry: Label=" + label + ", Value=" + value);

            // Check if the label is not null
            if (label != null) {
                // Safely aggregate the values
                // Using put method with existing value check
                Float currentTotal = aggregatedData.get(label);
                if (currentTotal == null) {
                    currentTotal = 0f; // Default to 0 if the label does not exist
                }
                currentTotal += value; // Accumulate the value
                aggregatedData.put(label, currentTotal);

                // Log the total for the label
                Log.d("PieChartDebug", "Current total for " + label + ": " + currentTotal);
            } else {
                // Handle the case where the label is null if needed
                Log.w("PieChart", "Encountered null label in PieEntry, value: " + value);
            }
        }

        List<PieEntry> aggregatedEntries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : aggregatedData.entrySet()) {
            // Only add entries with positive values
            if (entry.getValue() > 0) {
                aggregatedEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        Log.d("PieChartDebug", "Number of PieEntries created: " + aggregatedEntries.size());

        for (PieEntry pieEntry : aggregatedEntries) {
            Log.d("PieEntryDebug", "PieEntry: Label=" + pieEntry.getLabel() + ", Value=" + pieEntry.getValue());
        }

        PieDataSet dataSet = getPieDataSet(aggregatedEntries);
        PieData data = new PieData(dataSet);
        pieChart.setCenterTextSize(18f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setHoleRadius(65f);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate(); // Refresh the chart

        Legend legend = pieChart.getLegend();
        legend.setTextSize(16f); // Adjust the legend font size
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setWordWrapEnabled(true); // Enable word wrap for legend text
        legend.setXEntrySpace(15f); // Set horizontal space between entries
        legend.setYEntrySpace(8f); // Set vertical space between entries
        legend.setFormSize(12f); // Set size of legend forms (squares)

        pieChart.setData(data);

    }

    private static @NonNull PieDataSet getPieDataSet(List<PieEntry> aggregatedEntries) {
        PieDataSet dataSet = new PieDataSet(aggregatedEntries, "");

        // Get colors from the getPieColors() function
        int[] colors = getPieColors();

        // Create a list to hold the final colors for the dataset
        List<Integer> finalColors = new ArrayList<>();

        // Convert the colors array to a list for shuffling
        List<Integer> colorList = new ArrayList<>();
        for (int color : colors) {
            colorList.add(color);
        }

        // Shuffle the color list to randomize the color assignment
        Collections.shuffle(colorList);

        // Assign colors to each entry, cycling through the shuffled list
        for (int i = 0; i < aggregatedEntries.size(); i++) {
            finalColors.add(colorList.get(i % colorList.size())); // Cycle through shuffled colors
        }

        // Set the colors for the dataset
        dataSet.setColors(finalColors);

        dataSet.setValueTextSize(0f); // Hide values in slices
        dataSet.setValueTextColor(Color.TRANSPARENT); // Set text color to transparent

        return dataSet;
    }

    public static int[] getPieColors() {
        return new int[] {
                Color.parseColor("#FF7043"), // Coral (Warm)
                Color.parseColor("#FF8A65"), // Light Deep Orange
                Color.parseColor("#FFB74D"), // Light Orange
                Color.parseColor("#FFC107"), // Amber
                Color.parseColor("#FFEB3B"), // Bright Yellow
                Color.parseColor("#CDDC39"), // Lime
                Color.parseColor("#8BC34A"), // Light Green
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#388E3C"), // Dark Green
                Color.parseColor("#009688"), // Teal
                Color.parseColor("#00BCD4"), // Cyan
                Color.parseColor("#03A9F4"), // Light Blue
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#1976D2"), // Dark Blue
                Color.parseColor("#3F51B5"), // Indigo
                Color.parseColor("#673AB7"), // Deep Purple
                Color.parseColor("#8E24AA"), // Rich Purple
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#D5006D"), // Bright Pink A400
                Color.parseColor("#F50057"), // Bright Pink
                Color.parseColor("#E91E63"), // Pink
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#EF5350"), // Light Red
                Color.parseColor("#FF5722"), // Deep Orange
                Color.parseColor("#FF9100"), // Bright Orange
                Color.parseColor("#FFAB40"), // Soft Orange
                Color.parseColor("#795548"), // Brown
                Color.parseColor("#8D6E63"), // Light Brown
                Color.parseColor("#BDBDBD"), // Grey
                Color.parseColor("#9E9E9E"), // Dark Grey
                Color.parseColor("#607D8B"), // Blue Grey
                Color.parseColor("#546E7A")  // Slate Blue Grey
        };
    }
}