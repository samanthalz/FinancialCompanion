package com.example.financialcompanion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;

public class ProfileAccountsDialogFragment extends DialogFragment {

    private List<Integer> accountDrawables; // List of drawables for each account

    public ProfileAccountsDialogFragment(List<Integer> accountDrawables) {
        this.accountDrawables = accountDrawables;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // Inflate the layout for the dialog
        View view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_profile_accounts, null);
        GridView gridView = view.findViewById(R.id.grid_view_profile_accounts);

        // Set up the adapter with the OnItemClickListener
        ProfileAccountAdapter adapter = new ProfileAccountAdapter(requireContext(), accountDrawables, drawableRes -> {
            // Update the profile picture in the EditProfileFragment when an icon is selected
            updateProfilePicture(drawableRes);
            dismiss();  // Close the dialog
        });

        gridView.setAdapter(adapter);

        builder.setView(view)
                .setTitle("Select Profile Account")
                .setNegativeButton("Cancel", (dialog, id) -> dismiss())
                .setCancelable(true);

        return builder.create();
    }

    private void updateProfilePicture(int drawableRes) {
        // Access the profile picture ImageView from the EditProfileFragment and update it
        EditProfileFragment editProfileFragment = (EditProfileFragment) getParentFragment();
        if (editProfileFragment != null) {
            ImageView profilePicture = editProfileFragment.requireView().findViewById(R.id.profile_picture);
            profilePicture.setImageResource(drawableRes);

            // Now, update the profileImageVectorId in Firebase
            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

            // Update the profileImageVectorId
            userRef.child("profileImageVectorId").setValue(drawableRes)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Optionally, you can show a success message or a toast here
                            Log.d("ProfileUpdate", "Profile image vector ID updated successfully.");
                        } else {
                            // Handle failure here
                            Log.e("ProfileUpdate", "Failed to update profile image vector ID.", task.getException());
                        }
                    });
        }
    }



}

