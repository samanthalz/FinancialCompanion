package com.example.financialcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.media3.common.util.UnstableApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = SignupActivity.class.getSimpleName();

    EditText signupName, signupEmail, signupUsername, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);


        signupButton.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View view) {
                if (!validateField(signupName, "name") |
                        !validateField(signupEmail, "email") |
                        !validateField(signupUsername, "username") |
                        !validateField(signupPassword, "password")) {
                    // Show error messages to user based on validation failures
                    return;
                }

                String name = signupName.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String username = signupUsername.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    // Send verification email
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verificationTask -> {
                                                if (verificationTask.isSuccessful()) {
                                                    androidx.media3.common.util.Log.d(TAG, "Verification email sent to " + user.getEmail());
                                                    Intent intent = new Intent(SignupActivity.this, EmailVerificationActivity.class);
                                                    intent.putExtra("email", email);
                                                    intent.putExtra("name", name);
                                                    intent.putExtra("username", username);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    androidx.media3.common.util.Log.e(TAG, "sendEmailVerification failed!", verificationTask.getException());
                                                    // Display error message to the user
                                                    Toast.makeText(SignupActivity.this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show();
                                                    // You might want to add more detailed error handling based on the exception
                                                }
                                            });
                                }
                            } else {
                                // Handle registration error (e.g., display user-friendly error message)
                                Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                            }
                        });
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public Boolean validateField(EditText field, String fieldType) {
        String val = field.getText().toString().trim();

        switch (fieldType) {
            case "name":
                if (val.isEmpty()) {
                    field.setError("Name cannot be empty");
                    return false;
                }
                break;
            case "email":
                String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
                if (val.isEmpty()) {
                    field.setError("Email cannot be empty");
                    return false;
                } else if (!val.matches(emailPattern)) {
                    field.setError("Invalid email format");
                    return false;
                }
                break;
            case "username":
                if (val.isEmpty()) {
                    field.setError("Username cannot be empty");
                    return false;
                }
                break;
            case "password":
                if (val.isEmpty()) {
                    field.setError("Password cannot be empty");
                    return false;
                } else if (val.length() < 6) {
                    field.setError("Password must be at least 6 characters");
                    return false;
                }
                break;
            default:
                return false;
        }

        field.setError(null);
        return true;
    }

}