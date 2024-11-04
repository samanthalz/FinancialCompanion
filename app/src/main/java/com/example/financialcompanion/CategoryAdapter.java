package com.example.financialcompanion;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final int[] vectorDrawableResources;
    private final String[] categoryNames;
    private int selectedPos = 0; // Keeps track of selected item
    private int selectedIconId; // Variable to store selected icon ID
    private final SharedViewModel viewModel;


    public CategoryAdapter(int[] vectorDrawableResources, String[] categoryNames, SharedViewModel viewModel) {
        this.vectorDrawableResources = vectorDrawableResources;
        this.categoryNames = categoryNames;
        this.viewModel = viewModel;

        // Pass the pre-selected icon ID to the ViewModel
        selectedIconId = vectorDrawableResources[selectedPos]; // Get icon ID from selectedPos (default 0)
        viewModel.setSelectedIconId(selectedIconId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(vectorDrawableResources[position], categoryNames[position]);

        holder.itemView.setSelected(selectedPos == position);

        if (selectedPos == position) {
            holder.iconView.setBackgroundResource(R.drawable.circle_selected_background);
        } else {
            holder.iconView.setBackgroundResource(R.drawable.circle_background); // Assuming you have a default background
        }

        // Set the click listener on the ImageView to update iconView's background
        holder.imageView.setOnClickListener(v -> {
            // Refresh the previous selected item
            notifyItemChanged(selectedPos);

            // Update the selected position
            selectedPos = holder.getLayoutPosition();

            //Get the iconId for the selected position
            selectedIconId = vectorDrawableResources[selectedPos];
            viewModel.setSelectedIconId(selectedIconId);
            // Refresh the newly selected item
            notifyItemChanged(selectedPos);
        });
    }

    @Override
    public int getItemCount() {
        return categoryNames.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView textView;
        private final View iconView; // Reference to iconView

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewCategory);
            textView = itemView.findViewById(R.id.textViewCategoryName);
            iconView = itemView.findViewById(R.id.iconView); // Initialize iconView
        }

        public void bind(int imageResource, String categoryName) {
            imageView.setImageResource(imageResource);
            textView.setText(categoryName);
        }
    }
}



