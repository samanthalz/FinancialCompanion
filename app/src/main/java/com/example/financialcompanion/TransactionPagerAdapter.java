package com.example.financialcompanion;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TransactionPagerAdapter extends FragmentStateAdapter {

    private IncomeFragment incomeFragment = new IncomeFragment();
    private ExpenseFragment expenseFragment = new ExpenseFragment();

    public TransactionPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        incomeFragment = new IncomeFragment();
        expenseFragment = new ExpenseFragment();
    }


    @NonNull
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return incomeFragment;
            case 1:
                return expenseFragment;
            default:
                return new Fragment(); // Default fragment (optional)
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of tabs (income and expense)
    }
}
