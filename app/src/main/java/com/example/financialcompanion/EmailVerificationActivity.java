package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class EmailVerificationActivity extends AppCompatActivity {

    FirebaseDatabase database;
    FirebaseAuth.AuthStateListener authStateListener;
    DatabaseReference reference;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private SharedViewModel sharedViewModel;

    private static final String TAG = "EmailVerification";

    private final int[] incomeVectorDrawableResources  = {
            R.drawable.atm_withdraw_cashout_money, // ATM Withdrawal
            R.drawable.income_wallet_salary_earnings, // Income
            R.drawable.money_revenew_bag_bonus, // Bonus
            R.drawable.safe_savings_percentage_deposit, // Savings
            R.drawable.send_finance_cash_money_money_transfer_transaction, // Money Transfer
            R.drawable.present_gift_present_gift_box, // Gift
            R.drawable.baseline_add_36_grey
    };

    private final String[] incomeCategoryNames  = {
            "ATM",
            "Salary",
            "Bonus",
            "Dividend",
            "Transfer",
            "Gift",
            "Create"
    };

    private final int[] expenseVectorDrawableResources = {
            R.drawable.expenses_fee_tax_bill,
            R.drawable.grocery_food_gastronomy,
            R.drawable.entertainment_theater_drama,
            R.drawable.case_travel_vacation_suitcase,
            R.drawable.drugs_health_medicine_pills,
            R.drawable.restaurant_fork_kitchen_food_fork_fork_food_expense,
            R.drawable.service_expenses_car_drive,
            R.drawable.passive_earnings_income_money,
            R.drawable.beauty_health_heart_cosmetics,
            R.drawable.subscription_fees_newsletter_email,
            R.drawable.shop_store_shopping_bag,
            R.drawable.transport_tickets_train_metro,
            R.drawable.station_fuel_gas_car,
            R.drawable.kid_baby_child_care_stroller,
            R.drawable.insurance_protection_safety_shield,
            R.drawable.baseline_add_36_grey
    };

    private final String[] expenseCategoryNames  = {
            "Tax",
            "Grocery",
            "Fun",
            "Travel",
            "Health",
            "Food",
            "Car",
            "Utilities",
            "Beauty",
            "Membership",
            "Shopping",
            "Transport",
            "Fuel",
            "Kids",
            "Insurance",
            "Create"
    };

    @OptIn(markerClass = UnstableApi.class)
    @SuppressLint({"SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        Button resendVerificationButton = findViewById(R.id.resend_button);

        // Get user's details
        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");

        // Set the email in the TextView
        TextView emailTextView = findViewById(R.id.email_textview);
        if (email != null) {
            emailTextView.setText(email);
        }

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                user.reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (user.isEmailVerified()) {
                            // Email is verified, handle accordingly
                            runOnUiThread(() -> {
                                createUser(name, email, username);
                            });
                        } else {
                            // Email not yet verified, start periodic check
                            startPeriodicVerificationCheck(user, name, email, username);
                        }
                    } else {
                        Log.e(TAG, "Failed to reload user: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
            }
        };


        // Handle resend verification button
        resendVerificationButton.setOnClickListener(view -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(EmailVerificationActivity.this, "Verification email resent.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EmailVerificationActivity.this, "Failed to resend verification email.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Initialize the SharedViewModel
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
    }

    private void startPeriodicVerificationCheck(FirebaseUser user, String name, String email, String username) {
        final Handler handler = new Handler();
        Runnable checkRunnable = new Runnable() {
            @Override
            public void run() {
                user.reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && user.isEmailVerified()) {
                        // Email is now verified, redirect to LoginActivity
                        runOnUiThread(() -> {
                            createUser(name, email, username);
                        });
                    } else {
                        // Email still not verified, schedule another check
                        handler.postDelayed(this, 5000); // Check again in 5 seconds
                    }
                });
            }
        };
        handler.postDelayed(checkRunnable, 5000); // Initial check after 5 seconds
    }

    @OptIn(markerClass = UnstableApi.class)
    private void createUser(String name, String email, String username) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
        User newUser = new User(name, email, username);

        // Generate a unique ID for the savings account
        String accountId = reference.push().getKey(); // Generates a unique key for the account

        // Create an account with the unique ID
        Account savingsAccount = new Account(accountId, "Savings", 0.0, R.drawable.finance_bank_piggy_business_money_icon);
        newUser.addAccount(savingsAccount); // Add the account to the user

        // Mark this user as a first-time user
        newUser.setFirstTimeUser(true);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            newUser.setUid(uid);

            // Save the user with the account included in the user object
            reference.child(uid).setValue(newUser)
                    .addOnSuccessListener(aVoid -> {
                        // User created successfully
                        Log.d(TAG, "User created successfully");
                        Toast.makeText(EmailVerificationActivity.this, "Email verified! Redirecting to login..", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EmailVerificationActivity.this, LoginActivity.class));
                        finish(); // Finish this activity
                    })
                    .addOnFailureListener(e -> {
                        // Failed to create user
                        Log.e(TAG, "Failed to create user: " + e.getMessage());
                    });

            reference.child(uid).child("petType").setValue("Cat");
            reference.child(uid).child("profileImageVectorId").setValue(R.drawable.avatar_batman_hero_comics);
            long currentTime = System.currentTimeMillis();
            reference.child(uid).child("dateAccountCreated").setValue(currentTime);

            // Initialize and save categories for this user
            initializeCategories(uid);
        } else {
            // Handle the case where the user is not logged in (e.g., show an error message)
            Log.e(TAG, "User not logged in");
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializeCategories(String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("categories");

        // List to hold income and expense categories separately
        List<Category> incomeCategories = new ArrayList<>();
        List<Category> expenseCategories = new ArrayList<>();

        // Initialize income categories
        DatabaseReference incomeRef = databaseReference.child("income"); // Reference for income categories
        for (int i = 0; i < incomeCategoryNames.length; i++) {
            String categoryId = UUID.randomUUID().toString(); // Generate unique ID
            Category category = new Category(categoryId, incomeCategoryNames[i], incomeVectorDrawableResources[i]);
            incomeCategories.add(category);

            // Save the category to Firebase
            incomeRef.child(categoryId).setValue(category);
        }

        // Initialize expense categories
        DatabaseReference expenseRef = databaseReference.child("expense"); // Reference for expense categories
        for (int i = 0; i < expenseCategoryNames.length; i++) {
            String categoryId = UUID.randomUUID().toString(); // Generate unique ID
            Category category = new Category(categoryId, expenseCategoryNames[i], expenseVectorDrawableResources[i]);
            expenseCategories.add(category);

            // Save the category to Firebase
            expenseRef.child(categoryId).setValue(category);
        }

        // Set the categories in the view model
        sharedViewModel.setIncomeCategories(incomeCategories);
        sharedViewModel.setExpenseCategories(expenseCategories);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

}


