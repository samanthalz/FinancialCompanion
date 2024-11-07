package com.example.financialcompanion;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedIconId = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> incomeCategories = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> expenseCategories = new MutableLiveData<>();
    private final MutableLiveData<List<Account>> accounts = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<List<Goal>> goalsListLiveData = new MutableLiveData<>();
    private DatabaseReference goalsRef;

    public SharedViewModel() {
        // Initialize Firebase reference
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        goalsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("goals");

        // Listen to real-time updates
        goalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Goal> updatedGoalsList = new ArrayList<>();

                for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                    Goal goal = goalSnapshot.getValue(Goal.class);
                    if (goal != null) {
                        updatedGoalsList.add(goal);
                    }
                }

                StringBuilder logBuilder = new StringBuilder("Updated Goals List:\n");
                for (Goal goal : updatedGoalsList) {
                    logBuilder.append(goal.toString()).append("\n");
                }
                Log.e("SVM", logBuilder.toString());
                goalsListLiveData.setValue(updatedGoalsList); // Update LiveData with the new list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading goals", databaseError.toException());
            }
        });
    }

    public LiveData<List<Category>> getIncomeCategories() {
        return incomeCategories;
    }

    public LiveData<List<Category>> getExpenseCategories() {
        return expenseCategories;
    }

    public void setIncomeCategories(List<Category> categories) {
        incomeCategories.setValue(categories);
    }

    public void setExpenseCategories(List<Category> categories) {
        expenseCategories.setValue(categories);
    }

    public LiveData<Integer> getSelectedIconId() {
        return selectedIconId;
    }

    public void setSelectedIconId(int iconId) {
        selectedIconId.setValue(iconId);
    }

    public LiveData<List<Account>> getAccounts() {
        return accounts;
    }

    public void addAccount(Account newAccount) {
        List<Account> currentList = accounts.getValue();
        if (currentList != null) {
            currentList.add(newAccount);
            accounts.setValue(currentList); // Notify observers of the updated list
            Log.d("SharedViewModel", "Account added: " + newAccount.toString());
        }
    }

    public void setAccounts(List<Account> newAccounts) {
        accounts.setValue(newAccounts); // Used for initial setup or bulk updates
        Log.d("AccountViewModel", "Accounts list set: " + newAccounts.toString());
    }

    public LiveData<List<Goal>> getGoals() {
        return goalsListLiveData;
    }

    // Method to update goals list in the ViewModel
    public void setGoals(List<Goal> goals) {
        goalsListLiveData.setValue(goals);
    }


}

