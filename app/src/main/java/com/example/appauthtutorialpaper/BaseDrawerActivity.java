package com.example.appauthtutorialpaper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
public abstract class BaseDrawerActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base_drawer);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Hamburger button (Drawer Toggle)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupDrawerNavigation();
    }
    // Permite activităților copil să injecteze layout-ul propriu
    @Override
    public void setContentView(int layoutResID) {
        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        if (contentFrame != null) {
            LayoutInflater.from(this).inflate(layoutResID, contentFrame, true);
        }
    }

    // Setează comportamentul meniului hamburger
    private void setupDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_all_events) {
                    startActivity(new Intent(BaseDrawerActivity.this, EventListActivity.class));
                } else if (itemId == R.id.nav_my_events) {
                    startActivity(new Intent(BaseDrawerActivity.this, MyCreatedEventsActivity.class));
                } else if (itemId == R.id.nav_participations) {
                    startActivity(new Intent(BaseDrawerActivity.this, MyParticipationsActivity.class));
                } else if (itemId == R.id.nav_create_event) {
                    startActivity(new Intent(BaseDrawerActivity.this, CreateEventActivity.class));
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(BaseDrawerActivity.this, ProfileActivity.class));
                } else if (itemId == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(BaseDrawerActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });
    }
}
