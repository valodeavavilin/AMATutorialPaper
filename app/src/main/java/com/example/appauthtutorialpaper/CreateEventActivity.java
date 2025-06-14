package com.example.appauthtutorialpaper;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateEventActivity extends BaseDrawerActivity {

    private EditText titleEditText, descriptionEditText, dateEditText, timeEditText, locationEditText, imageUrlEditText;
    private Button saveEventBtn;
    private FirebaseFirestore firestore;
    private String uid, eventId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        firestore = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        // Legare UI
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        locationEditText = findViewById(R.id.locationEditText);
        imageUrlEditText = findViewById(R.id.imageUrlEditText);
        saveEventBtn = findViewById(R.id.saveEventBtn);

        dateEditText.setOnClickListener(v -> showDatePickerDialog());
        timeEditText.setOnClickListener(v -> showTimePickerDialog());

        eventId = getIntent().getStringExtra("eventId");
        isEditMode = eventId != null;

        if (isEditMode) {
            saveEventBtn.setText("Salvează Modificările");
            loadEventData();
        }

        saveEventBtn.setOnClickListener(v -> {
            if (isEditMode) {
                updateEvent();
            } else {
                createEvent();
            }
        });
    }
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // format: yyyy-MM-dd
                    String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    dateEditText.setText(formattedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }
    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    // Format 24h: HH:mm
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    timeEditText.setText(formattedTime);
                },
                hour, minute, true); // true = 24h format

        timePickerDialog.show();
    }

    private void createEvent() {
        String displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String creatorName = (displayName != null && !displayName.isEmpty())
                ? displayName
                : FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Map<String, Object> event = new HashMap<>();
        event.put("title", titleEditText.getText().toString().trim());
        event.put("description", descriptionEditText.getText().toString().trim());
        event.put("date", dateEditText.getText().toString().trim());
        event.put("time", timeEditText.getText().toString().trim());
        event.put("location", locationEditText.getText().toString().trim());
        event.put("imageUrl", imageUrlEditText.getText().toString().trim());
        event.put("creatorId", uid);
        event.put("creatorName", creatorName);
        event.put("status", "activ");
        event.put("participants", new ArrayList<>());

        firestore.collection("events")
                .add(event)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Eveniment creat!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadEventData() {
        firestore.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        titleEditText.setText(doc.getString("title"));
                        descriptionEditText.setText(doc.getString("description"));
                        dateEditText.setText(doc.getString("date"));
                        timeEditText.setText(doc.getString("time"));
                        locationEditText.setText(doc.getString("location"));
                        imageUrlEditText.setText(doc.getString("imageUrl"));
                    }
                });
    }

    private void updateEvent() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", titleEditText.getText().toString().trim());
        updates.put("description", descriptionEditText.getText().toString().trim());
        updates.put("date", dateEditText.getText().toString().trim());
        updates.put("time", timeEditText.getText().toString().trim());
        updates.put("location", locationEditText.getText().toString().trim());
        updates.put("imageUrl", imageUrlEditText.getText().toString().trim());

        firestore.collection("events").document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Modificări salvate!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
