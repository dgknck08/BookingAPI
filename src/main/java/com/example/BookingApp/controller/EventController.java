package com.example.BookingApp.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.BookingApp.dto.event.response.EventResponse;
import com.example.BookingApp.dto.event.response.SeatResponse;
import com.example.BookingApp.entityenums.EventType;
import com.example.BookingApp.service.EventService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;



@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EventController {
    
    private final EventService eventService;
    
    @GetMapping("/public")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllActiveEvents();
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long eventId) {
        return eventService.getEventById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/public/type/{eventType}")
    public ResponseEntity<List<EventResponse>> getEventsByType(@PathVariable EventType eventType) {
        List<EventResponse> events = eventService.getEventsByType(eventType);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/city/{city}")
    public ResponseEntity<List<EventResponse>> getEventsByCity(@PathVariable String city) {
        List<EventResponse> events = eventService.getEventsByCity(city);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/search")
    public ResponseEntity<List<EventResponse>> searchEvents(@RequestParam String keyword) {
        List<EventResponse> events = eventService.searchEvents(keyword);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/search/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(@RequestParam String keyword) {
        List<String> suggestions = eventService.getSearchSuggestions(keyword);
        return ResponseEntity.ok(suggestions);
    }
    
    @GetMapping("/public/search/advanced")
    public ResponseEntity<List<EventResponse>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<EventResponse> events = eventService.advancedSearch(keyword, eventType, city, startDate, endDate);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/date-range")
    public ResponseEntity<List<EventResponse>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<EventResponse> events = eventService.getEventsByDateRange(startDate, endDate);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/{eventId}/seats")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable Long eventId) {
        List<SeatResponse> seats = eventService.getAvailableSeats(eventId);
        return ResponseEntity.ok(seats);
    }
}