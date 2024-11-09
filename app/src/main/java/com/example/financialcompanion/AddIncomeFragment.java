package com.example.financialcompanion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AddIncomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private SharedViewModel viewModel;
    private Spinner categoryTypeSpinner;
    TextInputEditText categoryNameEditText;
    private int selectedVectorResource;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_income, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewAddIncome);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        Log.d("AddIncomeFragment", "User ID: " + userId); // Log the user ID

        fetchCategories(userId);

        categoryTypeSpinner = view.findViewById(R.id.categoryTypeSpinner);
        categoryNameEditText = view.findViewById(R.id.categoryNameEditText);

        // Set up the Spinner with "Income" and "Expense" options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.transaction_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryTypeSpinner.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the origin fragment info from arguments
        String originFragment = getArguments() != null ? getArguments().getString("originFragment") : "";

        // Initialize the toolbar and set the navigation icon click listener
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar); // Set the toolbar as the action bar

        // Get the NavController for this fragment
        NavController navController = Navigation.findNavController(view);

        // Enable the Up button and set the back arrow icon
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24); // Set your back arrow icon

        // Set Navigation click listener for the back button
        toolbar.setNavigationOnClickListener(v -> {
            Log.d("AddIncomeFragment", "Back button clicked");
            // Go back to the previous fragment on the back stack
            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Set the click listener for the checkmark icon (Save button)
        ImageView saveButton = view.findViewById(R.id.action_done);
        saveButton.setOnClickListener(v -> {
            // Perform the save action (e.g., save data to the database)
            saveDataToDatabase();

            // After saving, navigate back to the previous fragment (like the back arrow does)
            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });


    }

    private void saveDataToDatabase() {
        // Get the category name from the input field
        String categoryName = String.valueOf(categoryNameEditText.getText());

        // Get the selected transaction type from the spinner (Income or Expense)
        String transactionType = categoryTypeSpinner.getSelectedItem().toString().toLowerCase(); // Convert to lowercase ("income" or "expense")

        // Get the selected vector resource (from the adapter or directly passed if available)
        int vectorResource  = selectedVectorResource;

        // Example of saving to Firebase
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String categoryId = database.push().getKey(); // Generate a unique key for the category

        // Create Category object with ID, Name, and selected Vector Resource
        Category category = new Category(categoryId, categoryName, vectorResource );

        // Save data in Firebase under "users/{userId}/categories/{transactionType}/{categoryId}"
        assert categoryId != null;
        database.child("users").child(userId).child("categories")
                .child(transactionType) // This will be either "income" or "expense" in lowercase
                .child(categoryId) // Unique category ID
                .setValue(category)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AddIncomeFragment", "Category saved successfully");
                    } else {
                        Log.d("AddIncomeFragment", "Error saving category", task.getException());
                    }
                });
    }

    private void fetchCategories(String userId) {
        List<Category> defaultCategories = new ArrayList<>();

        // Define your own default categories
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.app_desktop_monitor_cpu_pc));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.aroma_smell));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.audio_headphones_listen_ear));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.bank_medical_blood_hospital));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.beauty_cosmetics_mails_gel_polish_cosmetics_nail_makeup_beauty_mails));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.brush_makeup_beauty));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.bus_vehicle_transport_school));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.calculator_tool_math_school));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.cam_retro_camera_pic_photo_old));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.color_pallete_brush_paint));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.computer_electronics_device_technology));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.desktop_app_phone));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.desktop_study_light_app_lamp));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.diamond_bracelette_jewelery_accessories_pearls_pearls_tiara_bracelette_ring));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.dress_style_clothing_fashion_shirt_footwear_shoes));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.emergency_healthcare_hospital));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.emergency_hospital_aid_medical));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.flovouring_sauce_seasoning));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.food_bowl_noodles_ramen));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.food_fast_thigh_fried_chicken));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.food_pizza_fast_bake_bread));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.fruit_apple_school_science));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.glasses_protection_sunglasses));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.grocery_food_gastronomy));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.hair_dressing_brush_hairstyle));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.headphone_app_desktop));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.hospital_healthcare_emergency));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.juice_beverage_refreshment));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.makeup_beauty_lipstick_fashion));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.makeup_bronzator_care));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.medical_pharmacy_drug_health));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.medical_syringe_injection_health));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.medicine_health_doctor_medical_checkup_healthcare_hospital));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.menu_cook_dish_rice_food));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.note_school_book_paper));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.patty_food_bread_hamburger));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.plant_grilled_vegetable_corn));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.prawn_animal_shrimp_seafood));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.school_pack_container_bag));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.school_stationery_pencil_pen));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.snacks_french_fast_fries_food));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.street_kiosk_asian_kiosk_trolley));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.sweets_frozen_cream_ice_cup));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.technology_pc_desktop));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.television_antenna_screen));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.tooth_health_dental_healthcare));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.woman_pouch_hand_female));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.woman_shoe_fashion_person_female_footwear_shoes));
        defaultCategories.add(new Category(UUID.randomUUID().toString(), "",R.drawable.wrist_hand_time_clock_clock));

        setCategoryAdapter(defaultCategories);
    }

    private void setCategoryAdapter(List<Category> categories) {
        // Check if categories is null or empty
        if (categories == null || categories.isEmpty()) {
            Log.d("AddIncomeFragment", "Categories list is null or empty. No categories to display."); // Log when categories are empty
            // Handle the case where there are no categories, maybe show a placeholder or empty state
            return;
        }

        // Create arrays to hold category names and drawable resource IDs
        String[] categoryNames = new String[categories.size()];
        int[] vectorDrawableResources = new int[categories.size()];

        // Fill the arrays with data from the categories list
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getName(); // Get the category name
            vectorDrawableResources[i] = categories.get(i).getVectorResource(); // Get the vector drawable resource ID
            Log.d("AddIncomeFragment", "Category " + i + ": Name = " + categoryNames[i] + ", Vector Resource ID = " + vectorDrawableResources[i]); // Log each category
        }

        // Create and set the CategoryAdapter
        CategoryAdapter categoryAdapter = new CategoryAdapter(vectorDrawableResources, categoryNames, viewModel);

        // Set the listener as an anonymous class
        categoryAdapter.setOnIconSelectedListener(new CategoryAdapter.OnIconSelectedListener() {
            @Override
            public void onIconSelected(int selectedIconId) {
                selectedVectorResource = selectedIconId;
                Log.d("AddIncomeFragment", "Selected Icon ID: " + selectedIconId);
            }
        });

        recyclerView.setAdapter(categoryAdapter);
        Log.d("AddIncomeFragment", "CategoryAdapter set with " + categories.size() + " categories."); // Log the adapter setup
    }
}