package com.example.BookingApp.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.BookingApp.dto.event.response.EventResponse;
import com.example.BookingApp.dto.event.response.SeatResponse;
import com.example.BookingApp.entityenums.EventType;
import com.example.BookingApp.exception.EventException;
import com.example.BookingApp.service.EventService;
import com.example.BookingApp.audit.AuditLogger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping("/public")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        log.info("Fetching all active events");
        
        try {
            List<EventResponse> events = eventService.getAllActiveEvents();
            log.info("Successfully retrieved {} active events", events.size());
            
            return ResponseEntity.ok(events);
            
        } catch (EventException e) {
            log.error("Event service error while fetching all events: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching all events: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve events: " + e.getMessage());
        }
    }

    @GetMapping("/public/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long eventId) {
        log.info("Fetching event by ID: {}", eventId);
        
        try {
            return eventService.getEventById(eventId)
                    .map(event -> {
                        log.info("Successfully retrieved event - ID: {}, Title: {}", eventId, event.title());
                        return ResponseEntity.ok(event);
                    })
                    .orElseGet(() -> {
                        log.warn("Event not found with ID: {}", eventId);
                        return ResponseEntity.notFound().build();
                    });
                    
        } catch (EventException e) {
            log.error("Event service error while fetching event by ID {}: {}", eventId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching event by ID {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve event: " + e.getMessage());
        }
    }

    @GetMapping("/public/type/{eventType}")
    public ResponseEntity<List<EventResponse>> getEventsByType(@PathVariable EventType eventType) {
        log.info("Fetching events by type: {}", eventType);
        
        try {
            List<EventResponse> events = eventService.getEventsByType(eventType);
            log.info("Successfully retrieved {} events for type: {}", events.size(), eventType);
            
            return ResponseEntity.ok(events);
            
        } catch (EventException e) {
            log.error("Event service error while fetching events by type {}: {}", eventType, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching events by type {}: {}", eventType, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve events by type: " + e.getMessage());
        }
    }

    @GetMapping("/public/city/{city}")
    public ResponseEntity<List<EventResponse>> getEventsByCity(@PathVariable String city) {
        log.info("Fetching events by city: {}", city);
        
        try {
            List<EventResponse> events = eventService.getEventsByCity(city);
            log.info("Successfully retrieved {} events for city: {}", events.size(), city);
            
            return ResponseEntity.ok(events);
            
        } catch (EventException e) {
            log.error("Event service error while fetching events by city {}: {}", city, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching events by city {}: {}", city, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve events by city: " + e.getMessage());
        }
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<EventResponse>> searchEvents(@RequestParam String keyword) {
        log.info("Searching events with keyword: {}", keyword);
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("Empty or null keyword provided for search");
                throw new IllegalArgumentException("Search keyword cannot be empty");
            }
            
            List<EventResponse> events = eventService.searchEvents(keyword);
            log.info("Successfully found {} events for keyword: {}", events.size(), keyword);
            
            return ResponseEntity.ok(events);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid search parameter: {}", e.getMessage());
            throw e;
        } catch (EventException e) {
            log.error("Event service error while searching events with keyword {}: {}", keyword, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while searching events with keyword {}: {}", keyword, e.getMessage(), e);
            throw new RuntimeException("Failed to search events: " + e.getMessage());
        }
    }

    @GetMapping("/public/search/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(@RequestParam String keyword) {
        log.info("Fetching search suggestions for keyword: {}", keyword);
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("Empty or null keyword provided for suggestions");
                throw new IllegalArgumentException("Search keyword cannot be empty");
            }
            
            List<String> suggestions = eventService.getSearchSuggestions(keyword);
            log.info("Successfully retrieved {} suggestions for keyword: {}", suggestions.size(), keyword);
            
            return ResponseEntity.ok(suggestions);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid suggestion parameter: {}", e.getMessage());
            throw e;
        } catch (EventException e) {
            log.error("Event service error while fetching suggestions for keyword {}: {}", keyword, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching suggestions for keyword {}: {}", keyword, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve suggestions: " + e.getMessage());
        }
    }

    @GetMapping("/public/search/advanced")
    public ResponseEntity<List<EventResponse>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Advanced search - Keyword: {}, Type: {}, City: {}, Start: {}, End: {}", 
                keyword, eventType, city, startDate, endDate);
        
        try {
            List<EventResponse> events = eventService.advancedSearch(keyword, eventType, city, startDate, endDate);
            log.info("Advanced search returned {} events", events.size());
            
            return ResponseEntity.ok(events);
            
        } catch (EventException e) {
            log.error("Event service error during advanced search: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during advanced search: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform advanced search: " + e.getMessage());
        }
    }

    @GetMapping("/public/date-range")
    public ResponseEntity<List<EventResponse>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Fetching events by date range - Start: {}, End: {}", startDate, endDate);
        
        try {
            if (startDate == null || endDate == null) {
                log.warn("Null date parameters provided for date range search");
                throw new IllegalArgumentException("Start date and end date are required");
            }
            
            if (startDate.isAfter(endDate)) {
                log.warn("Invalid date range provided - Start: {}, End: {}", startDate, endDate);
                throw new IllegalArgumentException("Start date must be before end date");
            }
            
            List<EventResponse> events = eventService.getEventsByDateRange(startDate, endDate);
            log.info("Successfully retrieved {} events for date range {} to {}", events.size(), startDate, endDate);
            
            return ResponseEntity.ok(events);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid date range parameters: {}", e.getMessage());
            throw e;
        } catch (EventException e) {
            log.error("Event service error while fetching events by date range: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching events by date range: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve events by date range: " + e.getMessage());
        }
    }

    @GetMapping("/{eventId}/seats")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable Long eventId) {
        log.info("Fetching available seats for event ID: {}", eventId);
        
        try {
            List<SeatResponse> seats = eventService.getAvailableSeats(eventId);
            log.info("Successfully retrieved {} available seats for event ID: {}", seats.size(), eventId);
            
            return ResponseEntity.ok(seats);
            
        } catch (EventException e) {
            log.error("Event service error while fetching seats for event ID {}: {}", eventId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching seats for event ID {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve available seats: " + e.getMessage());
        }
    }
}