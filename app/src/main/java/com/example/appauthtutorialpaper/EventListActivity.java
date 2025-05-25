package com.example.appauthtutorialpaper;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appauthtutorialpaper.adapters.EventAdapter;
import com.example.appauthtutorialpaper.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends BaseDrawerActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recyclerView = findViewById(R.id.eventRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firestore = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();
        adapter = new EventAdapter(this, eventList);
        recyclerView.setAdapter(adapter);

        loadEvents();
        FloatingActionButton fab = findViewById(R.id.fabCreateEvent);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(EventListActivity.this, CreateEventActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadEvents();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void loadEvents() {
        listenerRegistration = firestore.collection("events")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    eventList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.id = doc.getId();
                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

}
