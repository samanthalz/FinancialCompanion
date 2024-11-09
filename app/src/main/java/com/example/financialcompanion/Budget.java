package com.example.financialcompanion;

public class Budget {
    private String id;
    private String category;
    private double amount;
    private String monthYear;

    // Constructor
    public Budget(String category, double amount, String monthYear) {
        this.category = category;
        this.amount = amount;
        this.monthYear = monthYear;
    }

    public Budget() {}

    // Getters and setters (optional)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }
}

