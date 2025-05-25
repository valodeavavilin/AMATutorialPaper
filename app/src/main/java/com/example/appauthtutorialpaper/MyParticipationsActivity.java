package com.example.appauthtutorialpaper;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import com.google.firebase.firestore.ListenerRegistration;

public class MyParticipationsActivity extends BaseDrawerActivity{
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> participatingEvents = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String currentUid;

    private TextView emptyMessage;
    private Button seeAllEventsBtn;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_participations);
        ImageButton btnSort = findViewById(R.id.btnSort);
        btnSort.setOnClickListener(v -> showSortMenu(v));
        recyclerView = findViewById(R.id.recyclerViewParticipations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this, participatingEvents);
        recyclerView.setAdapter(adapter);

        emptyMessage = findViewById(R.id.emptyMessage);
        seeAllEventsBtn = findViewById(R.id.seeAllEventsBtn);

        firestore = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();

        loadParticipatingEvents();

        seeAllEventsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MyParticipationsActivity.this, EventListActivity.class);
            startActivity(intent);
        });
    }


    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Data crescător");
        popup.getMenu().add("Data descrescător");
        popup.getMenu().add("Participanți crescător");
        popup.getMenu().add("Participanți descrescător");
        popup.getMenu().add("Titlu A-Z");
        popup.getMenu().add("Titlu Z-A");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Data crescător":
                    Collections.sort(participatingEvents, Comparator.comparing(e -> e.date));
                    break;
                case "Data descrescător":
                    Collections.sort(participatingEvents, (e1, e2) -> e2.date.compareTo(e1.date));
                    break;
                case "Participanți crescător":
                    Collections.sort(participatingEvents, Comparator.comparingInt(e -> e.participants != null ? e.participants.size() : 0));
                    break;
                case "Participanți descrescător":
                    Collections.sort(participatingEvents, (e1, e2) -> {
                        int size1 = e1.participants != null ? e1.participants.size() : 0;
                        int size2 = e2.participants != null ? e2.participants.size() : 0;
                        return Integer.compare(size2, size1);
                    });
                    break;
                case "Titlu A-Z":
                    Collections.sort(participatingEvents, Comparator.comparing(e -> e.title.toLowerCase()));
                    break;
                case "Titlu Z-A":
                    Collections.sort(participatingEvents, (e1, e2) -> e2.title.toLowerCase().compareTo(e1.title.toLowerCase()));
                    break;
            }
            adapter.notifyDataSetChanged();
            return true;
        });

        popup.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadParticipatingEvents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }


    private void loadParticipatingEvents() {
        listenerRegistration = firestore.collection("events")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    participatingEvents.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        List<Map<String, Object>> participants = (List<Map<String, Object>>) doc.get("participants");
                        if (participants != null) {
                            for (Map<String, Object> p : participants) {
                                String uid = (String) p.get("uid");
                                if (currentUid.equals(uid)) {
                                    Event event = doc.toObject(Event.class);
                                    if (event != null) {
                                        event.id = doc.getId();
                                        participatingEvents.add(event);
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (participatingEvents.isEmpty()) {
                        emptyMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }

}
