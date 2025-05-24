package com.example.appauthtutorialpaper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SigninActivity extends BaseDrawerActivity {

    private EditText email;
    private EditText password;
    private Button signin;

    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        signin=findViewById(R.id.signin);
        Button forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);

        auth=FirebaseAuth.getInstance();

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email=email.getText().toString();
                String txt_password=password.getText().toString();
                loginUser(txt_email,txt_password);
            }
        });

        forgotPasswordBtn.setOnClickListener(v -> showResetPasswordDialog());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loginUser(String Email, String Password) {

        if (Email.isEmpty() || Password.isEmpty()) {
            Toast.makeText(SigninActivity.this, "Please fill both Email and Password fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(Email, Password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(SigninActivity.this, "Sign-in successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SigninActivity.this, EventListActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SigninActivity.this, "Authentication error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText emailInput = new EditText(this);
        emailInput.setHint("Enter your email");
        emailInput.setPadding(50, 30, 50, 30);
        builder.setView(emailInput);

        builder.setPositiveButton("Send Email", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Email field is empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Reset link sent to your email!", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

}