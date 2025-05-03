package com.example.appauthtutorialpaper;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class RegisterActivity extends AppCompatActivity {


    private EditText email;
    private EditText password;
    private Button register;
    private FirebaseAuth auth;
    private EditText firstName, lastName, phone;
    private DatabaseReference databaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        register=findViewById(R.id.register);
        auth=FirebaseAuth.getInstance();
        firstName = findViewById(R.id.firstname);
        lastName = findViewById(R.id.lastname);
        phone = findViewById(R.id.phone);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inițializare referință Firebase Realtime Database
        databaseRef = FirebaseDatabase.getInstance("https://amatutorialpaper-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Users");

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_first = firstName.getText().toString();
                String txt_last = lastName.getText().toString();
                String txt_phone = phone.getText().toString();
                String txt_mail=email.getText().toString();
                String txt_password=password.getText().toString();



                if (TextUtils.isEmpty(txt_mail) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_first) || TextUtils.isEmpty(txt_last) || TextUtils.isEmpty(txt_phone)){
                    Toast.makeText(RegisterActivity.this,"All fields are required!", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length()<8) {
                    Toast.makeText(RegisterActivity.this,"Password too need to contain al least 8 characters!",Toast.LENGTH_SHORT);
                }else {
                    registerUser(txt_mail, txt_password, txt_first, txt_last, txt_phone);
                }
            }
        });
    }

    private void registerUser(String email, String password, String first, String last, String phone) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = auth.getCurrentUser().getUid();
                User user = new User(first, last, email, phone, password, null); // poza e null pentru acum

                databaseRef.child(userId).setValue(user).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "User registered!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, ProfileActivity.class));
                        finish();
                    }
                });
            } else {
                Toast.makeText(RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}