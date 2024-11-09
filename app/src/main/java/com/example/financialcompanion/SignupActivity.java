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
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

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
            @Override
            public void onClick(View view) {
                if (!validateField(signupName, "name") |
                        !validateField(signupEmail, "email") |
                        !validateField(signupUsername, "username") |
                        !validateField(signupPassword, "password")) {
                    return;
                }

                String name = signupName.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String username = signupUsername.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                // Check if email is already in use
                auth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                List<String> signInMethods = task.getResult().getSignInMethods();
                                if (signInMethods != null && !signInMethods.isEmpty()) {
                                    // Email is already in use
                                    signupEmail.setError("Email is already registered");
                                } else {
                                    // Email is not in use, proceed with sign-up
                                    auth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    FirebaseUser user = auth.getCurrentUser();
                                                    if (user != null) {
                                                        user.sendEmailVerification()
                                                                .addOnCompleteListener(verificationTask -> {
                                                                    if (verificationTask.isSuccessful()) {
                                                                        Log.d(TAG, "Verification email sent to " + user.getEmail());
                                                                        Intent intent = new Intent(SignupActivity.this, EmailVerificationActivity.class);
                                                                        intent.putExtra("email", email);
                                                                        intent.putExtra("name", name);
                                                                        intent.putExtra("username", username);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    } else {
                                                                        Log.e(TAG, "sendEmailVerification failed!", verificationTask.getException());
                                                                        Toast.makeText(SignupActivity.this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                } else {
                                                    Log.w(TAG, "createUserWithEmailAndPassword:failure", task1.getException());
                                                    Toast.makeText(SignupActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                Log.e(TAG, "fetchSignInMethods failed", task.getException());
                                Toast.makeText(SignupActivity.this, "Failed to check email. Please try again.", Toast.LENGTH_SHORT).show();
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
                } else if (val.length() < 8) {
                    field.setError("Password must be at least 8 characters");
                    return false;
                } else if (!val.matches(".*[A-Z].*")) {
                    field.setError("Password must contain at least one uppercase letter");
                    return false;
                } else if (!val.matches(".*[a-z].*")) {
                    field.setError("Password must contain at least one lowercase letter");
                    return false;
                } else if (!val.matches(".*\\d.*")) {
                    field.setError("Password must contain at least one number");
                    return false;
                } else if (!val.matches(".*[@#$%^&+=!].*")) {
                    field.setError("Password must contain at least one special character (@#$%^&+=!)");
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