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

import android.widget.ImageButton;
import android.widget.PopupMenu;
import java.util.Collections;
import java.util.Comparator;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import java.util.Collections;
import java.util.Comparator;

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
        ImageButton btnSort = findViewById(R.id.btnSort);
        btnSort.setOnClickListener(v -> showSortMenu(v));
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

    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Dată crescător");
        popup.getMenu().add("Dată descrescător");
        popup.getMenu().add("Participanți crescător");
        popup.getMenu().add("Participanți descrescător");
        popup.getMenu().add("Titlu A-Z");
        popup.getMenu().add("Titlu Z-A");

        popup.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();

            switch (selected) {
                case "Dată crescător":
                    Collections.sort(eventList, Comparator.comparing(e -> e.date));
                    break;
                case "Dată descrescător":
                    Collections.sort(eventList, (e1, e2) -> e2.date.compareTo(e1.date));
                    break;
                case "Participanți crescător":
                    Collections.sort(eventList, Comparator.comparingInt(e -> e.participants != null ? e.participants.size() : 0));
                    break;
                case "Participanți descrescător":
                    Collections.sort(eventList, (e1, e2) -> {
                        int size1 = e1.participants != null ? e1.participants.size() : 0;
                        int size2 = e2.participants != null ? e2.participants.size() : 0;
                        return Integer.compare(size2, size1);
                    });
                    break;
                case "Titlu A-Z":
                    Collections.sort(eventList, Comparator.comparing(e -> e.title.toLowerCase()));
                    break;
                case "Titlu Z-A":
                    Collections.sort(eventList, (e1, e2) -> e2.title.toLowerCase().compareTo(e1.title.toLowerCase()));
                    break;
            }

            adapter.notifyDataSetChanged();
            return true;
        });

        popup.show();
    }

    private void applySort(int position) {
        switch (position) {
            case 1: // Data crescător
                Collections.sort(eventList, Comparator.comparing(e -> e.date));
                break;
            case 2: // Data descrescător
                Collections.sort(eventList, (e1, e2) -> e2.date.compareTo(e1.date));
                break;
            case 3: // Participanți descrescător
                Collections.sort(eventList, (e1, e2) -> {
                    int size1 = e1.participants != null ? e1.participants.size() : 0;
                    int size2 = e2.participants != null ? e2.participants.size() : 0;
                    return Integer.compare(size2, size1);
                });
                break;
            case 4: // Titlu A-Z
                Collections.sort(eventList, Comparator.comparing(e -> e.title.toLowerCase()));
                break;
            default:
                break;
        }
        adapter.notifyDataSetChanged();
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
