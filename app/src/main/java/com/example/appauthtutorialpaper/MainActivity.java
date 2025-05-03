package com.example.appauthtutorialpaper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    //request code ca identific fapul ca rezultatul de la onActivityResult provine
    // din procesul sign-in
    private static final int RC_SIGN_IN = 1001;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private Button signin;
    private Button register;
    private Button googleSignInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inițializări Firebase
        mAuth = FirebaseAuth.getInstance();

        // Inițializăm butoane
        signin = findViewById(R.id.SignInBtn);
        register = findViewById(R.id.RegisterBtn);
        googleSignInBtn = findViewById(R.id.btnGoogleSignIn);

        // Click pe Sign-In standard
        signin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SigninActivity.class));
            finish();
        });

        // Click pe Register
        register.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            finish();
        });

        // Configurare Google Sign-In
        //gso are nevoie de id-ul din firebase console - SHA-1 fingerprint adaugat
        //obtinut cu ./gradlew signingReport
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInBtn.setOnClickListener(v -> signInWithGoogle());
    }

    //Metoda intai face signout daca e un cont conectat si apoi cere din nou
    //userului sa isi aleaga contul pt conectare
    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }
    //metoda  primește rezultatul alegerii contului Google
    //dacă este valid, trimite token-ul către Firebase pentru autentificare
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        //creaza un credential pe baza tokeinului primit
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) { //daca autentificarea reuseste
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        //obtinem referinta exacta din baza de date
                        userRef = FirebaseDatabase.getInstance("https://amatutorialpaper-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference("Users").child(firebaseUser.getUid());

                        // Verificăm dacă userul Google există deja în DB
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    // Salvăm doar dacă nu există deja
                                    User user = new User(
                                            firebaseUser.getDisplayName(),
                                            "",
                                            firebaseUser.getEmail(),
                                            "",
                                            null,
                                            firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null
                                    );
                                    //daca nu exisra il salvam
                                    userRef.setValue(user);
                                }

                                // Redirectionam către profil
                                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "Firebase Error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "Firebase auth failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
