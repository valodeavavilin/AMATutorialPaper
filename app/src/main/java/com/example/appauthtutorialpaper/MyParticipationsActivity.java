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
import java.util.Map;
public class MyParticipationsActivity extends AppCompatActivity{
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> participatingEvents = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String currentUid;

    private TextView emptyMessage;
    private Button seeAllEventsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_participations);

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

    @Override
    protected void onResume() {
        super.onResume();
        loadParticipatingEvents();
    }

    private void loadParticipatingEvents() {
        firestore.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
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
