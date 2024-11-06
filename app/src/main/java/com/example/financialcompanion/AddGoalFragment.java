package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class AddGoalFragment extends Fragment {

    private TextInputEditText goalLabelEditText;
    private TextInputEditText goalAmountEditText;
    private TextInputEditText goalDescriptionEditText;
    private Spinner goalAccountSpinner;
    private AppCompatButton goalDueDateButton;
    private MaterialButton createGoalButton;
    private String userId;
    private String selectedDueDate;
    final List<Account> accounts = new ArrayList<>();
    private String selectedAccount;

    public AddGoalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_goal, container, false);

        // Get the user ID, ensuring the user is logged in
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Initialize views
        goalLabelEditText = rootView.findViewById(R.id.goal_label_edit_text);
        goalAmountEditText = rootView.findViewById(R.id.goal_amount_edit_text);
        goalDescriptionEditText = rootView.findViewById(R.id.goal_description_edit_text);
        goalAccountSpinner = rootView.findViewById(R.id.goal_account_spinner);
        goalDueDateButton = rootView.findViewById(R.id.goal_due_date_button);
        createGoalButton = rootView.findViewById(R.id.create_goal_button);

        // Set up the spinner
        setUpGoalAccountSpinner();

        setUpGoalDueDate();

        // Handle button clicks
        goalDueDateButton.setOnClickListener(v -> selectDueDate());
        createGoalButton.setOnClickListener(v -> createGoal());

        return rootView;
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
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24); // Set back arrow icon

        // Set Navigation click listener for the back button
        toolbar.setNavigationOnClickListener(v -> {
            Log.d("AddAccountFragment", "Back pressed from origin: " + originFragment);

            // Check the origin fragment and navigate accordingly
            if ("home".equals(originFragment)) {
                navController.navigate(R.id.action_addGoalFragment_to_homeFragment);
                Log.d("AddAccountFragment", "Navigating back to HomeFragment.");
            } else if ("courses".equals(originFragment)) {
                navController.navigate(R.id.action_addGoalFragment_to_coursesFragment);
                Log.d("AddAccountFragment", "Navigating back to CoursesFragment.");
            } else if ("pet".equals(originFragment)) {
                navController.navigate(R.id.action_addGoalFragment_to_petFragment);
                Log.d("AddAccountFragment", "Navigating back to PetFragment.");
            } else if ("account".equals(originFragment)) {
                navController.navigate(R.id.action_addGoalFragment_to_accountFragment);
                Log.d("AddAccountFragment", "Navigating back to AccountFragment.");
            } else {
                // If the origin is not specified, just go back normally
                navController.popBackStack();
                Log.d("AddAccountFragment", "Navigating back to previous fragment.");
            }

        });
    }

    private void setUpGoalAccountSpinner() {
        // Reference to the accounts node in the Firebase database
        DatabaseReference accountsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("accounts");

        // Fetch the list of accounts from Firebase
        accountsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // List to hold account names, starting with "All" at the top
                List<String> accountNames = new ArrayList<>();
                accountNames.add("All");  // Default selection

                // Loop through the accounts and get the account names
                for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                    String accountName = accountSnapshot.child("accountName").getValue(String.class);
                    if (accountName != null) {
                        accountNames.add(accountName);
                    }
                }

                // Set the selectedAccount to "All" by default
                selectedAccount = "All";

                // Check if there are any accounts
                if (!accountNames.isEmpty()) {
                    // Set up the ArrayAdapter with the account names
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, accountNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    goalAccountSpinner.setAdapter(adapter);

                    // Handle item selection
                    goalAccountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            // Update the selected account when the user selects an item
                            selectedAccount = accountNames.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            selectedAccount = null; // No account selected
                        }
                    });
                } else {
                    // Handle case where there are no accounts
                    Toast.makeText(getContext(), "No accounts available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(getContext(), "Failed to load accounts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpGoalDueDate() {
        // Get the current date and add one day to get tomorrow's date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Add 1 day to current date
        Date tomorrow = calendar.getTime();

        // Format tomorrow's date to "dd/MM/yyyy"
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(tomorrow);

        // Set the default date to the goalDueDateButton
        goalDueDateButton.setText(formattedDate);

        // Save the formattedDate for use later
        selectedDueDate = formattedDate; // This will be used later when saving the goal
    }


    private void selectDueDate() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Set the minimum date to tomorrow
        calendar.add(Calendar.DAY_OF_YEAR, 1); // Set to tomorrow

        // Get the updated date for the minimum date
        int minYear = calendar.get(Calendar.YEAR);
        int minMonth = calendar.get(Calendar.MONTH);
        int minDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Create the DatePickerDialog with the minimum date set to tomorrow
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                // Create a Calendar object to hold the selected date
                Calendar calendar = Calendar.getInstance();
                calendar.set(selectedYear, selectedMonth, selectedDayOfMonth);

                // Format the date
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String selectedDueDate = dateFormat.format(calendar.getTime());

                // Update the UI with the formatted date
                goalDueDateButton.setText(selectedDueDate);
            }

        }, year, month, day);

        // Set the minimum date to tomorrow
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private void createGoal() {
        // Get the values from the input fields
        String goalLabel = Objects.requireNonNull(goalLabelEditText.getText()).toString().trim();
        String goalAmountStr = Objects.requireNonNull(goalAmountEditText.getText()).toString().trim();
        String goalDescription = Objects.requireNonNull(goalDescriptionEditText.getText()).toString().trim();
        double goalAmount = 0.0;

        try {
            // Convert the goal amount to double
            goalAmount = Double.parseDouble(goalAmountStr);
        } catch (NumberFormatException e) {
            return;
        }

        // Validate input fields
        if (goalLabel.isEmpty() || goalAmountStr.isEmpty() || goalDescription.isEmpty() || selectedAccount == null || selectedDueDate == null) {
            // Show validation error message (e.g., Toast)
            return;
        }

        // Generate a unique ID for the goal
        String goalId = UUID.randomUUID().toString();

        // Get the current system time for the createDate
        Date createDate = new Date();

        // Use the date selected by the user for the dueDate, parse the string to Date if needed
        String formattedDate = selectedDueDate;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dueDate = null;
        try {
            dueDate = dateFormat.parse(formattedDate);
        } catch (ParseException e) {
            // Handle the error in case of incorrect date format
            e.printStackTrace();
            return; // Exit method if the date format is invalid
        }

        // Create the list of accounts based on the selected account
        setAccountsBasedOnSelection(selectedAccount);

        // Set the goal status as "Ongoing"
        String status = "Ongoing";

        // Create a Goal object
        Goal newGoal = new Goal(goalId, goalLabel, goalAmount, createDate, dueDate, accounts, goalDescription, status);

        // Save the goal to the database (Firebase)
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference goalsRef = database.getReference("users").child(userId).child("goals").child(goalId);

        goalsRef.setValue(newGoal).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Goal was saved successfully
                Toast.makeText(getContext(), "Goal created successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Handle failure
                Toast.makeText(getContext(), "Failed to create goal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Create the list of accounts based on the selected account
    private void setAccountsBasedOnSelection(String selectedAccount) {
        // Check if "All" is selected
        if ("All".equals(selectedAccount)) {
            // If "All" is selected, get all accounts from Firebase
            getAllAccountsFromDatabase(new AccountsCallback() {
                @Override
                public void onAccountsRetrieved(List<Account> allAccounts) {
                    // Add all accounts to the list
                    accounts.addAll(allAccounts);
                    // You can proceed with the accounts list after this (e.g., update UI)
                }
            });
        } else {
            // If a specific account is selected, get that account by name
            getAccountByName(selectedAccount, new AccountCallback() {
                @Override
                public void onAccountFound(Account selectedAccountObj) {
                    if (selectedAccountObj != null) {
                        // Add the selected account to the list
                        accounts.add(selectedAccountObj);
                        // You can proceed with the accounts list after this (e.g., update UI)
                    }
                }
            });
        }
    }

    public void getAccountByName(String accountName, final AccountCallback callback) {
        // Retrieve all accounts from Firebase asynchronously
        getAllAccountsFromDatabase(new AccountsCallback() {
            @Override
            public void onAccountsRetrieved(List<Account> allAccounts) {
                // Iterate through all accounts and return the one matching the name
                for (Account account : allAccounts) {
                    if (account.getAccountName().equals(accountName)) {
                        callback.onAccountFound(account); // Pass the found account
                        return;
                    }
                }
                callback.onAccountFound(null); // Return null if no account matches
            }
        });
    }

    // Modify getAllAccountsFromDatabase to use the callback pattern
    private void getAllAccountsFromDatabase(final AccountsCallback callback) {
        // Create a list to hold all accounts
        final List<Account> allAccounts = new ArrayList<>();

        // Reference to the accounts node in the Firebase database
        DatabaseReference accountsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("accounts");

        // Fetch the list of accounts from Firebase
        accountsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Loop through the accounts and get the account details
                for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                    // Get account details
                    String accountName = accountSnapshot.child("accountName").getValue(String.class);
                    String accountId = accountSnapshot.child("id").getValue(String.class);
                    Double balance = accountSnapshot.child("balance").getValue(Double.class);
                    Integer iconId = accountSnapshot.child("icon_id").getValue(Integer.class);

                    // Create Account if all fields are available
                    if (accountName != null && accountId != null && balance != null && iconId != null) {
                        Account account = new Account(accountId, accountName, balance, iconId);
                        allAccounts.add(account);
                    }
                }
                // Once all accounts are added, pass them to the callback
                callback.onAccountsRetrieved(allAccounts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(requireContext(), "Failed to load accounts", Toast.LENGTH_SHORT).show();
                callback.onAccountsRetrieved(new ArrayList<>()); // Return an empty list if there's an error
            }
        });
    }

    // Define the callback interfaces
    public interface AccountCallback {
        void onAccountFound(Account account);
    }

    public interface AccountsCallback {
        void onAccountsRetrieved(List<Account> accounts);
    }


}
