package com.example.appauthtutorialpaper;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appauthtutorialpaper.adapters.EventAdapter;
import com.example.appauthtutorialpaper.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
public class MyCreatedEventsActivity extends AppCompatActivity{
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> myEvents = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String currentUid;
    private Button createEventBtn;
    private TextView emptyMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_created_events);

        recyclerView = findViewById(R.id.recyclerViewMyEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this, myEvents);
        recyclerView.setAdapter(adapter);
        emptyMessage = findViewById(R.id.emptyMessage);
        firestore = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();
        createEventBtn = findViewById(R.id.createEventBtn);
        createEventBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MyCreatedEventsActivity.this, CreateEventActivity.class);
            startActivity(intent);
        });
        loadMyEvents();


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyEvents();
    }

    private void loadMyEvents() {
        firestore.collection("events")
                .whereEqualTo("creatorId", currentUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myEvents.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.id = doc.getId();
                            myEvents.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (myEvents.isEmpty()) {
                        emptyMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        e.printStackTrace()
                );

    }


}
