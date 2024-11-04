package com.example.financialcompanion;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Integer> selectedIconId = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> incomeCategories = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> expenseCategories = new MutableLiveData<>();

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

}

