package com.example.financialcompanion;

public class Account {
    private String acc_id;                // Unique identifier for the account
    private String accountName;        // Name of the account
    private double balance;            // Current balance of the account

    // Constructor
    public Account(String id, String accountName, double balance) {
        this.acc_id = id;                 // Initialize the account ID
        this.accountName = accountName;
        this.balance = balance;
    }

    // Getters
    public String getId() {
        return acc_id;                    // Getter for account ID
    }

    public String getAccountName() {
        return accountName;
    }

    public double getBalance() {
        return balance;
    }

    // Setters
    public void setId(String id) {
        this.acc_id = id;                // Setter for account ID
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}