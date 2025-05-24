package com.example.appauthtutorialpaper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appauthtutorialpaper.MainActivity;
import com.example.appauthtutorialpaper.R;
import com.example.appauthtutorialpaper.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Button logout, saveBtn;
    private EditText fnameText, lnameText, emailText, phoneText, passwordText;
    private DatabaseReference userRef;
    private CircleImageView profileImage;
    private Uri imageUri;
    private StorageReference storageRef;
    private Button createEventBtn;
    private Button viewEventsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        fnameText = findViewById(R.id.fname);
        lnameText = findViewById(R.id.lname);
        emailText = findViewById(R.id.email);
        phoneText = findViewById(R.id.phone);
        passwordText = findViewById(R.id.password);
        logout = findViewById(R.id.logoutbtn);
        saveBtn = findViewById(R.id.savebtn);
        profileImage = findViewById(R.id.profileImage);
        storageRef = FirebaseStorage.getInstance().getReference("ProfileImages");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance("https://amatutorialpaper-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("Users").child(uid);

        // Citim datele
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    fnameText.setText(user.firstName);
                    lnameText.setText(user.lastName);
                    emailText.setText(user.email);
                    phoneText.setText(user.phone);
                    passwordText.setText(user.password);

                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null && firebaseUser.getProviderData().size() > 1) {
                        for (UserInfo profile : firebaseUser.getProviderData()) {
                            if ("google.com".equals(profile.getProviderId())) {
                                passwordText.setVisibility(View.GONE);

                                // Glide în loc de Picasso + Google profile photo
                                Uri photoUrl = firebaseUser.getPhotoUrl();
                                if (photoUrl != null) {
                                    Glide.with(ProfileActivity.this)
                                            .load(photoUrl)
                                            .placeholder(R.drawable.ic_launcher_foreground) // fallback dacă nu are poză
                                            .into(profileImage);
                                }

                                break;
                            }
                        }
                    } else {
                        // Dacă nu e cont Google, folosește URL-ul salvat în Realtime Database
                        if (user.profileImageUrl != null) {
                            Glide.with(ProfileActivity.this)
                                    .load(user.profileImageUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(profileImage);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });

        // Salvăm modificările
        saveBtn.setOnClickListener(v -> {
            String newFirst = fnameText.getText().toString();
            String newLast = lnameText.getText().toString();
            String newPhone = phoneText.getText().toString();

            userRef.child("firstName").setValue(newFirst);
            userRef.child("lastName").setValue(newLast);
            userRef.child("phone").setValue(newPhone);

            Toast.makeText(ProfileActivity.this, "Changes saved!", Toast.LENGTH_SHORT).show();
        });

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1001);
        });

        // 🔓 Logout
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        createEventBtn = findViewById(R.id.createEventBtn);
        createEventBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, CreateEventActivity.class));
        });

        viewEventsBtn = findViewById(R.id.viewEventsBtn);
        viewEventsBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EventListActivity.class));
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);

            uploadImageToFirebase();
        }
    }


    private void uploadImageToFirebase() {
        if (imageUri == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference fileRef = storageRef.child(uid + ".jpg");

        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Salvează URL-ul în baza de date
                userRef.child("profileImageUrl").setValue(uri.toString());
                Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}
