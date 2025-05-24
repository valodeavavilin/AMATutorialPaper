package com.example.appauthtutorialpaper.models;

import java.util.List;

public class Event {
    public String id; // adăugăm id-ul manual
    public String title, description, date, time, location, imageUrl, creatorId;
    public String creatorName;
    public String status; // poate fi "activ" sau "anulat"
    public List<com.example.appauthtutorialpaper.models.Participant> participants;

    public Event() {} // Firebase needs empty constructor
}
