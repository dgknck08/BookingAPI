package com.example.BookingApp.repository;




import org.springframework.stereotype.Repository;

import com.example.BookingApp.entity.Event;

import java.util.*;

@Repository
public class EventRepository {
    private final Map<Long, Event> events = new HashMap<>();
    private Long idCounter = 1L;

    public List<Event> findAll() {
        return new ArrayList<>(events.values());
    }

    public Event findById(Long id) {
        return events.get(id);
    }

    public Event save(Event event) {
        if (event.getId() == null) {
            event.setId(idCounter);
        }
        events.put(event.getId(), event);
        return event;
    }

    public void delete(Long id) {
        events.remove(id);
    }
}

