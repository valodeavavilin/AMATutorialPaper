package com.example.appauthtutorialpaper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appauthtutorialpaper.models.Event;
import com.example.appauthtutorialpaper.models.Participant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDetailsActivity extends AppCompatActivity {

    private ImageView eventImage;
    private TextView eventTitle, eventDateTime, eventLocation, eventDescription;
    private TextView participantCount, participantNames;
    private Button participateBtn;

    private FirebaseFirestore firestore;
    private String eventId;
    private FirebaseUser currentUser;
    private DocumentReference eventRef;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventImage = findViewById(R.id.eventImage);
        eventTitle = findViewById(R.id.eventTitle);
        eventDateTime = findViewById(R.id.eventDateTime);
        eventLocation = findViewById(R.id.eventLocation);
        eventDescription = findViewById(R.id.eventDescription);
        participateBtn = findViewById(R.id.participateBtn);
        participantCount = findViewById(R.id.participantCount);
        participantNames = findViewById(R.id.participantNames);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null) {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        eventRef = firestore.collection("events").document(eventId);
        loadEventDetails();

        participateBtn.setOnClickListener(v -> {
            if (currentEvent == null) return;

            boolean alreadyParticipant = false;
            for (Participant p : currentEvent.participants) {
                if (p.uid.equals(currentUser.getUid())) {
                    alreadyParticipant = true;
                    break;
                }
            }

            if (alreadyParticipant) {
                removeParticipant();
            } else {
                addParticipant();
            }
        });
    }

    private void loadEventDetails() {
        eventRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentEvent = doc.toObject(Event.class);
                if (currentEvent != null) {
                    currentEvent.id = doc.getId();
                    updateUI();
                }
            }
        });
    }

    private void updateUI() {
        eventTitle.setText(currentEvent.title + "\n(Creat de: " + currentEvent.creatorName + ")");
        eventDateTime.setText(currentEvent.date + " at " + currentEvent.time);
        eventLocation.setText(currentEvent.location);
        eventDescription.setText(currentEvent.description);
        Glide.with(this).load(currentEvent.imageUrl).into(eventImage);

        if (currentEvent.participants == null)
            currentEvent.participants = new java.util.ArrayList<>();

        int count = currentEvent.participants.size();
        participantCount.setText("Participanți: " + count);

        StringBuilder names = new StringBuilder();
        for (Participant p : currentEvent.participants) {
            names.append("- ").append(p.name).append("\n");
        }
        participantNames.setText(names.toString());

        boolean isCreator = currentUser.getUid().equals(currentEvent.creatorId);
        boolean isAnulat = "anulat".equalsIgnoreCase(currentEvent.status);

        LinearLayout container = (LinearLayout) ((ScrollView) findViewById(R.id.main)).getChildAt(0);

        // 🔁 Ștergem vechiul buton de „anulează/activează” dacă există
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child.getTag() != null && child.getTag().equals("cancelButton")) {
                container.removeView(child);
                break;
            }
        }

        if (isCreator) {
            // 👉 Buton modifică
            participateBtn.setText("Modifică Eveniment");
            participateBtn.setVisibility(View.VISIBLE);
            participateBtn.setOnClickListener(v -> {
                Intent intent = new Intent(EventDetailsActivity.this, CreateEventActivity.class);
                intent.putExtra("eventId", currentEvent.id);
                startActivity(intent);
            });

            // 👉 Buton „Anulează” sau „Activează”
            Button statusBtn = new Button(this);
            statusBtn.setTag("cancelButton");

            if (isAnulat) {
                statusBtn.setText("Activează Eveniment");
                statusBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                statusBtn.setTextColor(getResources().getColor(android.R.color.white));
                statusBtn.setOnClickListener(v -> {
                    eventRef.update("status", "activ")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Evenimentul a fost activat!", Toast.LENGTH_SHORT).show();
                                loadEventDetails();
                            });
                });
            } else {
                statusBtn.setText("Anulează Eveniment");
                statusBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                statusBtn.setTextColor(getResources().getColor(android.R.color.white));
                statusBtn.setOnClickListener(v -> {
                    eventRef.update("status", "anulat")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Evenimentul a fost anulat!", Toast.LENGTH_SHORT).show();
                                loadEventDetails();
                            });
                });
            }

            container.addView(statusBtn);

        } else {
            // 👥 Utilizator obișnuit
            if (isAnulat) {
                participateBtn.setVisibility(View.GONE); // 🔒 nu poate participa
                Toast.makeText(this, "Acest eveniment este ANULAT", Toast.LENGTH_LONG).show();
            } else {
                // participare / renunțare
                boolean alreadyParticipant = false;
                for (Participant p : currentEvent.participants) {
                    if (p.uid.equals(currentUser.getUid())) {
                        alreadyParticipant = true;
                        break;
                    }
                }

                participateBtn.setText(alreadyParticipant ? "Renunță" : "Participă");
                participateBtn.setVisibility(View.VISIBLE);

                final boolean finalAlreadyParticipant = alreadyParticipant;
                participateBtn.setOnClickListener(v -> {
                    if (finalAlreadyParticipant) removeParticipant();
                    else addParticipant();
                });
            }
        }
    }






    private void addParticipant() {
        Map<String, Object> newParticipant = new HashMap<>();
        String fullName = currentUser.getDisplayName(); // sau preia din Realtime DB
        if (fullName == null || fullName.isEmpty()) fullName = currentUser.getEmail();

        newParticipant.put("uid", currentUser.getUid());
        newParticipant.put("name", fullName);

        eventRef.update("participants", com.google.firebase.firestore.FieldValue.arrayUnion(newParticipant))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Te-ai înscris la eveniment!", Toast.LENGTH_SHORT).show();
                    loadEventDetails();
                });
    }

    private void removeParticipant() {
        Map<String, Object> oldParticipant = new HashMap<>();
        String fullName = currentUser.getDisplayName();
        if (fullName == null || fullName.isEmpty()) fullName = currentUser.getEmail();

        oldParticipant.put("uid", currentUser.getUid());
        oldParticipant.put("name", fullName);

        eventRef.update("participants", com.google.firebase.firestore.FieldValue.arrayRemove(oldParticipant))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ai renunțat la participare!", Toast.LENGTH_SHORT).show();
                    loadEventDetails();
                });
    }
}
