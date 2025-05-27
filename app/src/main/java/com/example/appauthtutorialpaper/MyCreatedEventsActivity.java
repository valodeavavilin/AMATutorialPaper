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
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class MyCreatedEventsActivity extends BaseDrawerActivity{
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> myEvents = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String currentUid;
    private Button createEventBtn;
    private TextView emptyMessage;
    private ListenerRegistration listenerRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_created_events);
        ImageButton btnSort = findViewById(R.id.btnSort);
        btnSort.setOnClickListener(this::showSortMenu);
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
    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Dată (crescător)");
        popup.getMenu().add("Dată (descrescător)");
        popup.getMenu().add("Titlu A-Z");
        popup.getMenu().add("Titlu Z-A");
        popup.getMenu().add("Participanți (crescător)");
        popup.getMenu().add("Participanți (descrescător)");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Dată (crescător)":
                    myEvents.sort(Comparator.comparing(e -> e.date));
                    break;
                case "Dată (descrescător)":
                    myEvents.sort((e1, e2) -> e2.date.compareTo(e1.date));
                    break;
                case "Titlu A-Z":
                    myEvents.sort(Comparator.comparing(e -> e.title.toLowerCase()));
                    break;
                case "Titlu Z-A":
                    myEvents.sort((e1, e2) -> e2.title.toLowerCase().compareTo(e1.title.toLowerCase()));
                    break;
                case "Participanți (crescător)":
                    myEvents.sort(Comparator.comparingInt(e -> e.participants != null ? e.participants.size() : 0));
                    break;
                case "Participanți (descrescător)":
                    myEvents.sort((e1, e2) -> {
                        int s1 = e1.participants != null ? e1.participants.size() : 0;
                        int s2 = e2.participants != null ? e2.participants.size() : 0;
                        return Integer.compare(s2, s1);
                    });
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
        loadMyEvents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void loadMyEvents() {
        listenerRegistration = firestore.collection("events")
                .whereEqualTo("creatorId", currentUid)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) return;

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
                });
    }


}
