package com.example.financialcompanion;

import static androidx.navigation.Navigation.findNavController;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.text.ParseException;

public class TransactionFragment extends Fragment {

    private TextView tvSelectedText;
    private EditText etNotes;
    private StringBuilder currentAmount = new StringBuilder();
    private static final String PLACEHOLDER_TEXT = "0";
    private Button btnDate;
    private Calendar calendar;
    private ViewPager2 viewPager;
    private Button btnAccount;
    private Button btnTick;
    private int selectedIconId = -1; // Variable to store selected icon ID
    private TransactionPagerAdapter adapter;
    private TabLayout tabLayout;
    private String userId;

    // Define the TextWatcher as a field if it's not already
    private boolean isClearingFields = false; // Flag to prevent TextWatcher from acting when resetting

    private final TextWatcher amountTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No action needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isClearingFields) {
                // Clear placeholder if user starts typing and not resetting fields
                if (PLACEHOLDER_TEXT.equals(s.toString())) {
                    tvSelectedText.setText(""); // Clear placeholder text
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // No action needed
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        adapter = new TransactionPagerAdapter(this);

        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        tabLayout = view.findViewById(R.id.tabLayout);

        // Link TabLayout and ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Income" : "Expense");
        }).attach();

        // Attach the TextWatcher to the TextView
        tvSelectedText = view.findViewById(R.id.tv_selected_text);
        tvSelectedText.setText(PLACEHOLDER_TEXT);
        tvSelectedText.addTextChangedListener(amountTextWatcher);

        etNotes = view.findViewById(R.id.et_notes);

        // Set up buttons
        setupNumberButton(view, R.id.btn_7);
        setupNumberButton(view, R.id.btn_8);
        setupNumberButton(view, R.id.btn_9);
        setupNumberButton(view, R.id.btn_4);
        setupNumberButton(view, R.id.btn_5);
        setupNumberButton(view, R.id.btn_6);
        setupNumberButton(view, R.id.btn_1);
        setupNumberButton(view, R.id.btn_2);
        setupNumberButton(view, R.id.btn_3);
        setupNumberButton(view, R.id.btn_0);

        // Set up the dot button
        setupDotButton(view, R.id.btn_dot);

        // Set up the backspace button
        setupBackspaceButton(view, R.id.btn_backspace);

        btnAccount = view.findViewById(R.id.btn_account);
        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the AccountDialogFragment when the button is clicked
                AccountDialog dialog = new AccountDialog();
                dialog.show(getChildFragmentManager(), "accountDialog");
            }
        });

        return view;
    }

    private void setupNumberButton(View view, int buttonId) {
        Button button = view.findViewById(buttonId);
        button.setOnClickListener(v -> {
            // Get the button text and append it to currentAmount
            String buttonText = button.getText().toString();
            currentAmount.append(buttonText);
            // Update the TextView with the new amount
            updateTextView();
        });
    }

    private void setupDotButton(View view, int buttonId) {
        Button button = view.findViewById(buttonId);
        button.setOnClickListener(v -> {
            // Append a dot if it's not already present in the current amount
            if (!currentAmount.toString().contains(".")) {
                currentAmount.append(".");
                updateTextView();
            }
        });
    }

    private void setupBackspaceButton(View view, int buttonId) {
        Button button = view.findViewById(buttonId);
        button.setOnClickListener(v -> {
            // Remove the last character from currentAmount if it's not empty
            if (currentAmount.length() > 0) {
                currentAmount.deleteCharAt(currentAmount.length() - 1);
                updateTextView();
            }
        });
    }

    private void updateTextView() {
        // Temporarily remove the TextWatcher
        tvSelectedText.removeTextChangedListener(amountTextWatcher);

        // Update the TextView based on the current amount
        if (currentAmount.length() > 0) {
            tvSelectedText.setText(currentAmount.toString());
        } else {
            tvSelectedText.setText(PLACEHOLDER_TEXT); // Show placeholder if empty
        }

        // Reattach the TextWatcher
        tvSelectedText.addTextChangedListener(amountTextWatcher);
    }

    @OptIn(markerClass = UnstableApi.class)
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
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24); // Set back arrow icon

        // Set Navigation click listener for the back button
        toolbar.setNavigationOnClickListener(v -> {
            Log.d("TransactionFragment", "Back pressed from origin: " + originFragment);

            // Check the origin fragment and navigate accordingly
            if ("home".equals(originFragment)) {
                navController.navigate(R.id.action_transactionFragment_to_homeFragment);
                Log.d("TransactionFragment", "Navigating back to HomeFragment.");
            } else if ("courses".equals(originFragment)) {
                navController.navigate(R.id.action_transactionFragment_to_coursesFragment);
                Log.d("TransactionFragment", "Navigating back to CoursesFragment.");
            } else if ("pet".equals(originFragment)) {
                navController.navigate(R.id.action_transactionFragment_to_petFragment);
                Log.d("TransactionFragment", "Navigating back to PetFragment.");
            } else if ("account".equals(originFragment)) {
                navController.navigate(R.id.action_transactionFragment_to_accountFragment);
                Log.d("TransactionFragment", "Navigating back to AccountFragment.");
            } else {
                // If the origin is not specified, just go back normally
                navController.popBackStack();
                Log.d("TransactionFragment", "Navigating back to previous fragment.");
            }

        });

        btnDate = view.findViewById(R.id.btn_date);
        calendar = Calendar.getInstance();
        updateDateButton(); // Set initial date to today's date
        //btnDate.setOnClickListener(v -> showDatePicker());
        btnDate.setOnClickListener(v -> showDateTimePickerDialog());

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(); // Get the current user's UID
        AccountManager accountManager = new AccountManager(userId);

        btnTick = view.findViewById(R.id.btn_tick);
        btnTick.setOnClickListener(v -> {
            String amountText = tvSelectedText.getText().toString();

            // Check if the amount is not the placeholder text
            if (PLACEHOLDER_TEXT.equals(amountText)) {
                Toast.makeText(getContext(), "Please fill in the amount", Toast.LENGTH_SHORT).show();
            } else {
                // Retrieve the amount, description, selected icon ID, and other details
                double amount = Double.parseDouble(amountText.trim()); // Parse amount
                String description = etNotes.getText().toString().trim(); // Retrieve notes

                String dateString = btnDate.getText().toString(); // Retrieve the date as a String
                // Create a new Calendar instance to get the current time
                Calendar currentCalendar = Calendar.getInstance();
                int second = currentCalendar.get(Calendar.SECOND);

                long timestamp = 0;

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Log.d("DateDebug", "dateString: " + dateString);
                Date date = null;
                if (!dateString.isEmpty()) {
                    try {
                        dateString = dateString + ":" + String.format("%02d", second);  // Adding seconds if needed
                        date = sdf.parse(dateString);
                        if (date != null) {
                            timestamp = date.getTime();  // Convert to long timestamp
                            Log.d("DateDebug", "Timestamp: " + timestamp);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("DateDebug", "dateString is null or empty.");
                }

                // Get the current tab position
                int currentTabPosition = tabLayout.getSelectedTabPosition();

                // Determine transaction type based on current tab
                String transactionType = currentTabPosition == 0 ? "income" : "expense";

                String accountType = btnAccount.getText().toString(); // Get selected account type
                Log.d("TransactionActivity", "Selected Account Type: " + accountType);

                int categoryId = selectedIconId;

                // Retrieve the account ID based on the account type
                String accountId = accountManager.getAccountId(accountType);
                if (accountId != null) {
                    // Create an instance of the Transaction class
                    Transaction transaction = new Transaction(
                            amount,
                            timestamp,
                            transactionType,
                            description,
                            accountId,
                            categoryId
                    );

                    String trans_id = transaction.generateUniqueId();
                    transaction.setId(trans_id);

                    // Save the transaction to the database
                    saveTransactionToDatabase(transaction);
                } else {
                    Log.d("AccountManager", "Account ID for " + accountType + " not found.");
                }
            }
        });

        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.getSelectedIconId().observe(getViewLifecycleOwner(), iconId -> {
            this.selectedIconId = iconId;
            Toast.makeText(getContext(), "Selected icon ID: " + iconId, Toast.LENGTH_SHORT).show(); // Show icon ID in Toast
        });

    }

    private void saveTransactionToDatabase(Transaction transaction) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Define the account ID (assuming you have it from the selected account type)
        String accountId = transaction.getAccountId(); // Get the account ID from the transaction object

        // Push the transaction data to the correct path
        databaseReference.child("accounts") // Root node
                .child(accountId) // User's specific account ID (e.g., savingsId)
                .child("transactions") // Transactions node
                .child(transaction.getId()) // Unique transaction ID
                .setValue(transaction) // Save the transaction object
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Transaction saved successfully
                        Toast.makeText(getContext(), "Transaction saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle failure
                        Toast.makeText(getContext(), "Failed to save transaction", Toast.LENGTH_SHORT).show();
                    }
                    clearFields();
                });
    }

    private void clearFields() {
        isClearingFields = true; // Set flag to indicate resetting fields

        tvSelectedText.removeTextChangedListener(amountTextWatcher); // Temporarily remove the TextWatcher
        tvSelectedText.setText(PLACEHOLDER_TEXT);                    // Set placeholder text
        currentAmount.setLength(0);                                  // Clear any previous amount input
        etNotes.setText("");                                         // Clear the notes field
        tvSelectedText.clearFocus();                                 // Clear focus from TextView
        tvSelectedText.addTextChangedListener(amountTextWatcher);    // Re-attach the TextWatcher

        isClearingFields = false; // Reset flag after clearing
    }

    private void showDateTimePickerDialog() {
        // Get the current date and time
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Log the values
        Log.d("DateTimePicker", "Year: " + year);
        Log.d("DateTimePicker", "Month: " + (month + 1)); // Month is zero-based
        Log.d("DateTimePicker", "Day: " + day);
        Log.d("DateTimePicker", "Hour: " + hour);
        Log.d("DateTimePicker", "Minute: " + minute);

        // Create new instances of DatePicker and TimePicker each time
        DatePicker datePicker = new DatePicker(requireContext());
        datePicker.init(year, month, day, null); // Initialize with current date

        TimePicker timePicker = new TimePicker(requireContext());
        timePicker.setHour(hour);
        timePicker.setMinute(minute);

        // Create a LinearLayout to hold both the DatePicker and TimePicker
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(datePicker);
        layout.addView(timePicker);

        // Wrap the layout in a ScrollView to allow scrolling
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.addView(layout);

        // Build the AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Select Date and Time")
                .setView(scrollView) // Set the ScrollView as the view
                .setPositiveButton("OK", (dialogInterface, which) -> {
                    // Update calendar with selected date and time
                    calendar.set(Calendar.YEAR, datePicker.getYear());
                    calendar.set(Calendar.MONTH, datePicker.getMonth());
                    calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    calendar.set(Calendar.MINUTE, timePicker.getMinute());

                    // Update the button text to the selected date and time
                    updateDateButton();
                })
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.show(); // Show the dialog
    }

    private void updateDateButton() {
        String myFormat = "dd/MM/yyyy HH:mm"; // Format for the date
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        btnDate.setText(sdf.format(calendar.getTime())); // Update button text with the formatted date
    }

}