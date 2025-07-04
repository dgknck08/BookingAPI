package com.example.BookingApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.BookingApp.dto.event.EventDto;
import com.example.BookingApp.dto.event.SeatDto;
import com.example.BookingApp.entityenums.EventType;
import com.example.BookingApp.service.EventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {
    
    @Autowired
    private EventService eventService;
    
    @GetMapping("/public")
    public ResponseEntity<List<EventDto>> getAllEvents() {
        List<EventDto> events = eventService.getAllActiveEvents();
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long eventId) {
        Optional<EventDto> event = eventService.getEventById(eventId);
        return event.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/public/type/{eventType}")
    public ResponseEntity<List<EventDto>> getEventsByType(@PathVariable EventType eventType) {
        List<EventDto> events = eventService.getEventsByType(eventType);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/city/{city}")
    public ResponseEntity<List<EventDto>> getEventsByCity(@PathVariable String city) {
        List<EventDto> events = eventService.getEventsByCity(city);
        return ResponseEntity.ok(events);
    }
    
    // Temel arama
    @GetMapping("/public/search")
    public ResponseEntity<List<EventDto>> searchEvents(@RequestParam String keyword) {
        List<EventDto> events = eventService.searchEvents(keyword);
        return ResponseEntity.ok(events);
    }
    
    // Otomatik tamamlama önerileri
    @GetMapping("/public/search/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(@RequestParam String keyword) {
        List<String> suggestions = eventService.getSearchSuggestions(keyword);
        return ResponseEntity.ok(suggestions);
    }
    
    // Kategori ile filtrelenmiş arama
    @GetMapping("/public/search/type/{eventType}")
    public ResponseEntity<List<EventDto>> searchEventsByType(
            @RequestParam String keyword, 
            @PathVariable EventType eventType) {
        List<EventDto> events = eventService.searchEventsByType(keyword, eventType);
        return ResponseEntity.ok(events);
    }
    
    // Şehir ile filtrelenmiş arama
    @GetMapping("/public/search/city/{city}")
    public ResponseEntity<List<EventDto>> searchEventsByCity(
            @RequestParam String keyword, 
            @PathVariable String city) {
        List<EventDto> events = eventService.searchEventsByCity(keyword, city);
        return ResponseEntity.ok(events);
    }
    
    // Gelişmiş arama - birden fazla filtre
    @GetMapping("/public/search/advanced")
    public ResponseEntity<List<EventDto>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<EventDto> events = eventService.advancedSearch(keyword, eventType, city, startDate, endDate);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/date-range")
    public ResponseEntity<List<EventDto>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<EventDto> events = eventService.getEventsByDateRange(startDate, endDate);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/{eventId}/seats")
    public ResponseEntity<List<SeatDto>> getAvailableSeats(@PathVariable Long eventId) {
        List<SeatDto> seats = eventService.getAvailableSeats(eventId);
        return ResponseEntity.ok(seats);
    }
}