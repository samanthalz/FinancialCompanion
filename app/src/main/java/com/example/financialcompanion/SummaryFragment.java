package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SummaryFragment extends Fragment {

    private PieChart pieChart;
    private String userId;
    private long dateAccountCreated; // Retrieved from the database
    private Button selectDateButton; // Button to open date picker
    private TextView selectedMonthTextView; // To show the selected month/year

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment first
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        // Initialize the PieChart view from the inflated layout
        pieChart = view.findViewById(R.id.pieChart);
        selectDateButton = view.findViewById(R.id.selectDateButton);
        selectedMonthTextView = view.findViewById(R.id.selectedMonthTextView);

        // Get the user ID, ensuring the user is logged in
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        getDateAccountCreatedFromDatabase(userId, new DateCallback() {
            @Override
            public void onDateRetrieved(long dateAccountCreated) {
                // Use the fetched date
                Log.d("DateFetched", "Account creation date: " + dateAccountCreated);

                // Set the default date (based on the dateAccountCreated)
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(dateAccountCreated);
                int defaultMonth = calendar.get(Calendar.MONTH); // Get month from account creation date
                int defaultYear = calendar.get(Calendar.YEAR); // Get year from account creation date
                // selectedMonthTextView.setText((defaultMonth + 1) + "/" + defaultYear);

                // Define a display format for the month and year
                String myFormat = "MMMM yyyy"; // E.g., "January 2023"
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

                // Set the formatted date in the TextView
                selectedMonthTextView.setText(sdf.format(calendar.getTime()));

                // Set up the button to open a date picker
                selectDateButton.setOnClickListener(v -> showDatePicker(defaultMonth, defaultYear));
                //selectDateButton.setOnClickListener(v -> showMonthYearPicker());

                // Setup Pie chart with default month and year data
                loadMonthlyExpenseData(defaultMonth, defaultYear);
            }

            @Override
            public void onFailure() {
                // Handle the failure case
                Log.e("DateFetched", "Could not retrieve account creation date");
            }
        });

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
            Log.d("SummaryFragment", "Back pressed from origin: " + originFragment);

            // Check the origin fragment and navigate accordingly
            if ("home".equals(originFragment)) {
                // Check if the action exists in the navigation graph
                navController.navigate(R.id.action_summaryFragment_to_homeFragment);

            } else {
                // If the origin is not specified, just go back normally
                navController.popBackStack();
                Log.d("SummaryFragment", "Navigating back to previous fragment.");
            }
        });
    }

    public interface DateCallback {
        void onDateRetrieved(long dateAccountCreated);
        void onFailure();
    }

    // Fetch the account creation date (in milliseconds) from Firebase or your database
    private void getDateAccountCreatedFromDatabase(String userId, DateCallback callback) {
        // Assuming you're using Firebase Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Fetch the 'dateAccountCreated' from Firebase
        userRef.child("dateAccountCreated").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Long dateCreated = task.getResult().getValue(Long.class); // Get the value as long
                if (dateCreated != null) {
                    callback.onDateRetrieved(dateCreated);
                } else {
                    // Handle null value
                    Log.e("FirebaseError", "Account creation date not found");
                    callback.onFailure();
                }
            } else {
                // Handle failure
                Log.e("FirebaseError", "Failed to fetch account creation date");
                callback.onFailure();
            }
        });
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

                    // Load data based on the selected month and year
                    loadMonthlyExpenseData(month, year);
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

    // Load expenses for the selected month and year
    private void loadMonthlyExpenseData(int selectedMonth, int selectedYear) {
        // First, we need to fetch both the expense data and the categories asynchronously.
        fetchExpenseData(selectedMonth, selectedYear, new OnExpenseDataFetchedListener() {
            @Override
            public void onExpenseDataFetched(Map<String, Float> expenseData) {
                // After fetching expense data, fetch categories
                getCategoriesFromDatabase(userId, new OnCategoriesFetchedListener() {
                    @Override
                    public void onCategoriesFetched(List<Category> categories) {
                        // Now that both the expense data and categories are fetched, set up the pie chart
                        setupPieChart(expenseData, categories);
                    }
                });
            }
        });
    }

    // Callback when expense data is fetched
    public interface OnExpenseDataFetchedListener {
        void onExpenseDataFetched(Map<String, Float> expenseData);
    }

    // Callback when categories are fetched
    public interface OnCategoriesFetchedListener {
        void onCategoriesFetched(List<Category> categories);
    }


    // Fetch the expense data from Firebase or database based on month/year
    private void fetchExpenseData(int month, int year, final OnExpenseDataFetchedListener listener) {
        final Map<String, Float> categoryExpenseMap = new HashMap<>();

        DatabaseReference userAccountsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("accounts");

        userAccountsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot accountsSnapshot) {
                for (DataSnapshot accountSnapshot : accountsSnapshot.getChildren()) {
                    String accountId = accountSnapshot.getKey();

                    DatabaseReference transactionsRef = accountSnapshot.child("transactions").getRef();
                    transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot transactionsSnapshot) {
                            for (DataSnapshot transactionSnapshot : transactionsSnapshot.getChildren()) {
                                Integer amountInt = transactionSnapshot.child("amount").getValue(Integer.class);
                                Integer categoryId = transactionSnapshot.child("categoryId").getValue(Integer.class);
                                String type = transactionSnapshot.child("type").getValue(String.class);
                                Long timestamp = transactionSnapshot.child("date").getValue(Long.class);

                                if (timestamp != null && categoryId != null && amountInt != null && "expense".equals(type)) {
                                    Calendar transactionDate = Calendar.getInstance();
                                    transactionDate.setTimeInMillis(timestamp);

                                    int transactionMonth = transactionDate.get(Calendar.MONTH);
                                    int transactionYear = transactionDate.get(Calendar.YEAR);

                                    if (transactionMonth == month && transactionYear == year) {
                                        String category = String.valueOf(categoryId);
                                        Float currentAmount = Optional.ofNullable(categoryExpenseMap.get(category)).orElse(0f);
                                        categoryExpenseMap.put(category, currentAmount + amountInt.floatValue());
                                    }
                                }
                            }
                            // Notify listener when data is fetched
                            listener.onExpenseDataFetched(categoryExpenseMap);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Firebase", "Error fetching transactions: " + databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching accounts: " + databaseError.getMessage());
            }
        });
    }

    // Retrieve categories from the database (or predefined categories)
    private void getCategoriesFromDatabase(String userId, final OnCategoriesFetchedListener listener) {
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("categories")
                .child("expense");

        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    categories.add(category);
                }
                Log.d("Firebase", "Fetched " + categories.size() + " categories.");

                // Notify listener when categories are fetched
                listener.onCategoriesFetched(categories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching categories: " + databaseError.getMessage());
            }
        });
    }


    // Method to setup the pie chart
    private void setupPieChart(Map<String, Float> categoryExpenseMap, List<Category> categoryList) {
        List<PieEntry> entries = new ArrayList<>();

        // Check if the map is empty
        if (categoryExpenseMap.isEmpty()) {
            pieChart.setCenterText("No Expenses Recorded");
        } else {
            for (Map.Entry<String, Float> entry : categoryExpenseMap.entrySet()) {
                int categoryId = Integer.parseInt(entry.getKey());
                Log.d("CategoryID", "Category ID: " + categoryId);
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
                    if (category.getVectorResource() == categoryId) {
                        categoryName = category.getName();
                        Log.d("CategorySearch", "Found category: " + categoryName + " for vector resource: " + categoryId);
                        break;
                    }
                }
                entries.add(new PieEntry(entry.getValue(), categoryName));
            }
            pieChart.setCenterText("Expenses by Category");
        }

        PieDataSet dataSet = getPieDataSet(entries);
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

    private PieDataSet getPieDataSet(List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "");

        int[] colors = getPieColors();
        List<Integer> colorList = new ArrayList<>();
        for (int color : colors) {
            colorList.add(color);
        }

        Collections.shuffle(colorList);

        // Assign colors to each entry
        dataSet.setColors(colorList);
        dataSet.setValueTextSize(0f); // Hide values

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