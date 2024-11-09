package com.example.financialcompanion;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AllTransactionFragment extends Fragment {

    private RecyclerView transactionRecyclerView;
    private RecentTransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private List<Category> expenseCategories = new ArrayList<>();
    private String userId;
    private Button dateRangeDisplay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_transaction, container, false);

        // Initialize the RecyclerView
        transactionRecyclerView = view.findViewById(R.id.transactionRecyclerView);

        // Initialize adapter with the empty list and set it to the RecyclerView
        // adapter = new RecentTransactionAdapter(transactionList, expenseCategories);
        adapter = new RecentTransactionAdapter(getContext(), transactionList, expenseCategories);
        transactionRecyclerView.setAdapter(adapter);

        // Get the user ID, ensuring the user is logged in
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        fetchCategories(userId);

        // Load the latest transactions
        loadTransactions();

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
            Log.d("TransactionListFragment", "Back pressed from origin: " + originFragment);

            // Check the origin fragment and navigate accordingly
            if ("home".equals(originFragment)) {
                // Check if the action exists in the navigation graph
                navController.navigate(R.id.action_allTransactionFragment_to_homeFragment);

            } else {
                // If the origin is not specified, just go back normally
                navController.popBackStack();
                Log.d("TransactionListFragment", "Navigating back to previous fragment.");
            }
        });

        dateRangeDisplay = view.findViewById(R.id.date_range_display);
        dateRangeDisplay.setOnClickListener(v -> {
            requireView().findViewById(R.id.no_transactions_text).setVisibility(View.GONE);
            requireView().findViewById(R.id.transactionRecyclerView).setVisibility(View.VISIBLE);
            showDateRangeDialog();
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactions(); // Ensure transactions are loaded each time the fragment is resumed
        // Re-set the date range display click listener, if necessary
        dateRangeDisplay.setOnClickListener(v -> {
            requireView().findViewById(R.id.no_transactions_text).setVisibility(View.GONE);
            requireView().findViewById(R.id.transactionRecyclerView).setVisibility(View.VISIBLE);
            showDateRangeDialog();
        });
    }


    private void showDateRangeDialog() {
        final Calendar calendar = Calendar.getInstance();

        // Start Date Picker Dialog
        final DatePickerDialog startDatePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar startCalendar = Calendar.getInstance();
                    startCalendar.set(year, month, dayOfMonth);
                    // Set startCalendar to the start of the day (00:00:00)
                    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    startCalendar.set(Calendar.MINUTE, 0);
                    startCalendar.set(Calendar.SECOND, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);

                    // End Date Picker Dialog after selecting start date
                    DatePickerDialog endDatePickerDialog = new DatePickerDialog(requireContext(),
                            (endView, endYear, endMonth, endDayOfMonth) -> {
                                Calendar endCalendar = Calendar.getInstance();
                                endCalendar.set(endYear, endMonth, endDayOfMonth);
                                // Set endCalendar to the end of the day (23:59:59)
                                endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                                endCalendar.set(Calendar.MINUTE, 59);
                                endCalendar.set(Calendar.SECOND, 59);
                                endCalendar.set(Calendar.MILLISECOND, 999);

                                // Format the date range display
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                                String dateRangeText = dateFormat.format(startCalendar.getTime()) + " - " + dateFormat.format(endCalendar.getTime());

                                ((TextView) requireView().findViewById(R.id.date_range_display)).setText(dateRangeText);
                                filterTransactionsByDateRange(startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis());

                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                    // Set title for the End Date Picker
                    endDatePickerDialog.setTitle("Select End Date");
                    endDatePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Apply", endDatePickerDialog);
                    endDatePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> dialog.dismiss());
                    endDatePickerDialog.show();

                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Set title for the Start Date Picker
        startDatePickerDialog.setTitle("Select Start Date");
        startDatePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Next", startDatePickerDialog);
        startDatePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> dialog.dismiss());
        startDatePickerDialog.show();
    }


    private void filterTransactionsByDateRange(long startDate, long endDate) {
        if (transactionList == null || transactionList.isEmpty()) {
            // Handle empty list appropriately if data isn't ready
            return;
        }

        List<Transaction> filteredTransactions = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            if (transaction.getDate() >= startDate && transaction.getDate() <= endDate) {
                filteredTransactions.add(transaction);
            }
        }

        // Always set RecyclerView to visible, then show or hide "No Transactions" based on the filtered list
        requireView().findViewById(R.id.transactionRecyclerView).setVisibility(View.VISIBLE);

        if (filteredTransactions.isEmpty()) {
            // Show "No Transactions" text if the filtered list is empty
            requireView().findViewById(R.id.no_transactions_text).setVisibility(View.VISIBLE);
            requireView().findViewById(R.id.transactionRecyclerView).setVisibility(View.GONE);
        } else {
            // Hide "No Transactions" text and show RecyclerView when there are filtered transactions
            requireView().findViewById(R.id.no_transactions_text).setVisibility(View.GONE);
        }

        // Update the adapter with the filtered transactions
        adapter.updateTransactions(filteredTransactions);
    }


    private void fetchCategories(String userId) {
        // Create a reference for both expense and income categories
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("categories");

        Log.d("AllTransFragment", "Attempting to fetch both expense and income categories from Firebase...");

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
                        Log.d("AllTransFragment", "Added expense category: " + category);
                    }
                }
                Log.d("AllTransFragment", "Fetched " + allCategories.size() + " expense categories.");

                // Fetch income categories
                DataSnapshot incomeSnapshot = dataSnapshot.child("income");
                for (DataSnapshot categorySnapshot : incomeSnapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null) {
                        allCategories.add(category); // Add all income categories
                        Log.d("AllTransFragment", "Added income category: " + category);
                    }
                }
                Log.d("AllTransFragment", "Fetched " + allCategories.size() + " income categories.");

                // Update the adapter with all categories if not empty
                if (!allCategories.isEmpty()) {
                    adapter.setExpenseCategories(allCategories);
                    Log.d("AllTransFragment", "Updated adapter with combined categories.");
                } else {
                    Log.d("AllTransFragment", "No categories found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching categories: " + databaseError.getMessage());
            }
        });
    }

    private void loadTransactions() {
        // Fetch all transactions from the database
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

                // Update adapter with the latest transactions
                adapter.updateTransactions(transactionList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AllTransFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }
}

