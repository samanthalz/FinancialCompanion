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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupUsername, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;

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
                    return;  // Stop the process if any validation fails
                }

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String name = signupName.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String username = signupUsername.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                // Proceed to register the user if all fields are valid
                User user = new User(name, email, username, password);
                reference.child(username).setValue(user);

                Toast.makeText(SignupActivity.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
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
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
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