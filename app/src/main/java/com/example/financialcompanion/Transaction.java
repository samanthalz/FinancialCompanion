package com.example.financialcompanion;

import java.util.Date;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Transaction {
    private String trans_id;               // Unique identifier for the transaction
    private double amount;           // Amount of the transaction
    private Long date;               // Date of the transaction
    private String type;             // Type of transaction (e.g., "income", "expense")
    private String description;      // Description of the transaction
    private String accountId;        // ID of the associated account
    private int categoryId;       // ID of the associated category (vector resource)

    // Constructor
    public Transaction(double amount, Long date, String type, String description,
                       String accountId, int categoryId) {
        this.trans_id = "";
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.description = description;
        this.accountId = accountId;
        this.categoryId = categoryId; // Initialize the category ID
    }

    public Transaction() {
    }

    // Getters
    public String getId() {
        return trans_id;
    }

    public double getAmount() {
        return amount;
    }

    public Long getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getAccountId() {
        return accountId;
    }

    public int getCategoryId() {
        return categoryId; // Getter for category ID
    }

    public void setId(String trans_id) {
        this.trans_id = trans_id;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId; // Setter for category ID
    }

    public String generateUniqueId() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        return databaseReference.push().getKey(); // Generates a unique key for the current node
    }
}