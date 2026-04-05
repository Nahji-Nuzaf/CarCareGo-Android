package com.example.carcarego.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carcarego.R;
import com.example.carcarego.databinding.ActivityLoginBinding;
import com.example.carcarego.databinding.ActivitySignupBinding;
import com.example.carcarego.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        binding.tvGoToLogin.setOnClickListener(view->{
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
//            finish();
        });
        binding.btnSignup.setOnClickListener(view -> {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            if(name.isEmpty()){
                binding.etName.setError("Name is required");
                binding.etName.requestFocus();
                return;
            }
            if(email.isEmpty()){
                binding.etEmail.setError("Email is required");
                binding.etEmail.requestFocus();
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.etEmail.setError("Enter Valid Email");
                binding.etEmail.requestFocus();
                return;
            }
            if(password.isEmpty()){
                binding.etPassword.setError("Password is required");
                binding.etPassword.requestFocus();
                return;
            }
            if (password.length() < 6){
                binding.etPassword.setError("Password must be at least 6 characters");
                binding.etPassword.requestFocus();
                return;
            }
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String uid =  task.getResult().getUser().getUid();
                        User user = User.builder()
                                .uid(uid)
                                .name(name)
                                .email(email).build();
                        firebaseFirestore.collection("users").document(uid).set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Saved Success", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    }
                }
            });
        });
    }
}