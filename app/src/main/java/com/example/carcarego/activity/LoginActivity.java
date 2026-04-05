package com.example.carcarego.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carcarego.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        binding.tvGoToSignup.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
        binding.btnLogin.setOnClickListener(view -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            if (!validateInputs(email, password)) return;
            binding.btnLogin.setEnabled(false);
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        binding.btnLogin.setEnabled(true);
                        if (task.isSuccessful()) {
                            markOnboardingComplete();
                            updateUI(firebaseAuth.getCurrentUser());
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                            Toast.makeText(this, "Login Failed: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            binding.etEmail.setError("Email is Required");
            binding.etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email");
            binding.etEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError("Password is Required");
            binding.etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return false;
        }
        return true;
    }
    private void markOnboardingComplete() {
        SharedPreferences preferences = getSharedPreferences("CarCareGoPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}