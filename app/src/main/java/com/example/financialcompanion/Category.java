package com.example.financialcompanion;

public class Category {
    private final String id; // Unique identifier for the category
    private final String name; // Name of the category
    private final int vectorResource; // Resource ID for the vector drawable

    // Constructor
    public Category(String id, String name, int vectorResource) {
        this.id = id;
        this.name = name;
        this.vectorResource = vectorResource;
    }

    public Category() {
        this.id = "";
        this.name = "";
        this.vectorResource = 0;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getVectorResource() {
        return vectorResource;
    }
}
