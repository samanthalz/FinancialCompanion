package com.example.financialcompanion;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedIconId = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> incomeCategories = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> expenseCategories = new MutableLiveData<>();
    private final MutableLiveData<List<Account>> accounts = new MutableLiveData<>(new ArrayList<>());

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



}

