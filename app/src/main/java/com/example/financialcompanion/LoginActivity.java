package com.example.financialcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    TextView signupRedirectText;
    Button loginButton;
    TextView forgotPasswordText;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        SpannableString content = new SpannableString(forgotPasswordText.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        forgotPasswordText.setText(content);

        // Button click listener for user login
        loginButton.setOnClickListener(view -> {
            if (validateEmail() && validatePassword()) {
                loginUser();
            }
        });

        // Redirect to Signup page
        signupRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        FirebaseApp.initializeApp(this);

        // Forgot Password Click Listener
        forgotPasswordText.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this, "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the user is already authenticated
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish(); // Finish LoginActivity so the user can't return to it by pressing back
        }
    }

    // Validate Email
    public Boolean validateEmail() {
        String email = loginEmail.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        if (email.isEmpty()) {
            loginEmail.setError("Email cannot be empty");
            return false;
        } else if (!email.matches(emailPattern)) {
            loginEmail.setError("Invalid email format");
            return false;
        } else {
            loginEmail.setError(null);
            return true;
        }
    }

    // Validate Password
    public Boolean validatePassword() {
        String password = loginPassword.getText().toString().trim();
        if (password.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    // Method to log in the user using Firebase Authentication
    public void loginUser() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        // Sign in with email and password using FirebaseAuth
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Successful login, redirect to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finish LoginActivity so the user can't go back to it
            } else {
                // Login failed, handle different error cases
                String errorMessage;
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidUserException e) {
                    // Email is not registered
                    errorMessage = "No account found with this email. Please sign up first.";
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    // Invalid email format or wrong password
                    if (e.getErrorCode().equals("ERROR_INVALID_EMAIL")) {
                        errorMessage = "Invalid email format. Please enter a valid email.";
                    } else {
                        errorMessage = "Incorrect password. Please try again.";
                    }
                } catch (Exception e) {
                    // Other errors
                    errorMessage = "Login failed. Please try again.";
                }

                // Show the specific error message as a toast
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
