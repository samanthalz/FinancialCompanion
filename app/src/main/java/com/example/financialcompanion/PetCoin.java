package com.example.financialcompanion;

public class PetCoin {
    private int balance;

    public PetCoin() {
        this.balance = 0;  // Default value, you can set a different default if needed
    }

    public PetCoin(int initialBalance) {
        this.balance = initialBalance;
    }

    // Getter and setter for the balance
    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    // Method to add coins to the balance
    public void addCoins(int amount) {
        this.balance += amount;
    }

    // Method to deduct coins for a purchase (e.g., when user buys something)
    public boolean deductCoins(int amount) {
        if (balance >= amount) {
            this.balance -= amount;
            return true;  // Success
        }
        return false;  // Insufficient balance
    }
}
