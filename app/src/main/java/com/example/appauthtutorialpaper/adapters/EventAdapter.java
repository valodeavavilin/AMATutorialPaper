package com.example.appauthtutorialpaper.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appauthtutorialpaper.R;
import com.example.appauthtutorialpaper.models.Event;
import com.example.appauthtutorialpaper.EventDetailsActivity; // vei crea în pasul următor

import java.text.BreakIterator;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.creator.setText("Creat de: " + event.creatorName);
        holder.title.setText(event.title);
        holder.location.setText(event.location);
        holder.dateTime.setText(event.date + " " + event.time);
        holder.participants.setText(String.valueOf(event.participants != null ? event.participants.size() : 0));

        Glide.with(context).load(event.imageUrl).into(holder.image);

        // Vizibilitate status
        if ("anulat".equalsIgnoreCase(event.status)) {
            holder.status.setVisibility(View.VISIBLE);
        } else {
            holder.status.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("eventId", event.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, creator, location, dateTime, participants, status;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.eventImage);
            title = itemView.findViewById(R.id.eventTitle);
            creator = itemView.findViewById(R.id.eventCreator);
            location = itemView.findViewById(R.id.eventLocation);
            dateTime = itemView.findViewById(R.id.eventDateTime);
            participants = itemView.findViewById(R.id.eventParticipants);
            status = itemView.findViewById(R.id.eventStatus);
        }
    }
}
