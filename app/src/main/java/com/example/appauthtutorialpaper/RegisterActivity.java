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

public class RegisterActivity extends AppCompatActivity {


    private EditText email;
    private EditText password;
    private Button register;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        register=findViewById(R.id.register);
        auth=FirebaseAuth.getInstance();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_mail=email.getText().toString();
                String txt_password=password.getText().toString();

                if (TextUtils.isEmpty(txt_mail) || TextUtils.isEmpty(txt_password)){
                    Toast.makeText(RegisterActivity.this,"Empty credentials", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length()<8) {
                    Toast.makeText(RegisterActivity.this,"Password too need to contain al least 8 characters!",Toast.LENGTH_SHORT);
                }else {
                    registerUser(txt_mail,txt_password);
                }
            }
        });
    }

    private void registerUser(String Mail, String Password) {
    auth.createUserWithEmailAndPassword(Mail,Password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()){
                Toast.makeText(RegisterActivity.this, "User was successful registered!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, ProfileActivity.class));
                finish();
            }else {
                Toast.makeText(RegisterActivity.this,"Registration failed!", Toast.LENGTH_SHORT).show();
            }
        }
    });
    }
}