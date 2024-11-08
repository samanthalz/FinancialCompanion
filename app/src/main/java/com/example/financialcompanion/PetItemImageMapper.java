package com.example.financialcompanion;

import java.util.HashMap;
import java.util.Map;

public class PetItemImageMapper {

    // Mapping of pet types to items and their corresponding image resource IDs
    private static final Map<String, Map<String, Integer>> petItemImageMap = new HashMap<>();

    static {
        // Initialize the mapping for each pet type
        Map<String, Integer> dogItems = new HashMap<>();
        dogItems.put("Black Sunglasses", R.drawable.dog_with_blacksunglasses);
        dogItems.put("Yellow Sunglasses", R.drawable.dog_with_yellowsunglasses);
        dogItems.put("Black Spectacles", R.drawable.dog_with_blackspecs);
        dogItems.put("gold_chain", R.drawable.dog_with_goldchain);
        dogItems.put("Purple Bone Collar", R.drawable.dog_with_purplebonecollar);
        dogItems.put("Pink Bone Collar", R.drawable.dog_with_whitebonecollar);
        dogItems.put("Yellow Bone Collar", R.drawable.dog_with_yellowbonecollar);
        dogItems.put("Pink Ribbon", R.drawable.dog_with_ribbonright);
        petItemImageMap.put("Dog", dogItems);

        Map<String, Integer> catItems = new HashMap<>();
        catItems.put("Black Sunglasses", R.drawable.cat_with_blacksunglasses);
        catItems.put("Yellow Sunglasses", R.drawable.cat_with_yellowsunglasses);
        catItems.put("Black Spectacles", R.drawable.cat_with_blackspecs);
        catItems.put("gold_chain", R.drawable.cat_with_goldchain);
        catItems.put("Purple Bone Collar", R.drawable.cat_with_purplebonecollar);
        catItems.put("Pink Bone Collar", R.drawable.cat_with_whitebonecollar);
        catItems.put("Yellow Bone Collar", R.drawable.cat_with_yellowbonecollar);
        catItems.put("Pink Ribbon", R.drawable.cat_with_ribbonright);
        petItemImageMap.put("Cat", catItems);
    }

    // Method to get the image resource based on pet type and item ID
    public static int getImageResource(String petType, String itemId) {
        if (petItemImageMap.containsKey(petType)) {
            Map<String, Integer> items = petItemImageMap.get(petType);
            assert items != null;
            if (items.containsKey(itemId)) {
                return items.get(itemId); // Return the correct image resource ID
            }
        }
        return -1; // Return a default or error image if not found
    }
}

