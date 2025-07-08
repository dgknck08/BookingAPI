package com.example.BookingApp.service;

import com.example.BookingApp.dto.event.response.EventResponse;
import com.example.BookingApp.dto.event.response.SeatResponse;
import com.example.BookingApp.entity.Event;
import com.example.BookingApp.entityenums.EventStatus;
import com.example.BookingApp.entityenums.EventType;
import com.example.BookingApp.entityenums.SeatStatus;
import com.example.BookingApp.mapper.EventMapper;
import com.example.BookingApp.mapper.SeatMapper;
import com.example.BookingApp.repository.EventRepository;
import com.example.BookingApp.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final EventMapper eventMapper;
    private final SeatMapper seatMapper;
    
    public List<EventResponse> getAllActiveEvents() {
        List<Event> events = eventRepository.findByStatusOrderByEventDateAsc(EventStatus.ACTIVE);
        return events.stream()
                .map(this::mapEventWithSeatCounts)
                .toList();
    }
    
    public List<EventResponse> getEventsByType(EventType eventType) {
        List<Event> events = eventRepository.findByEventTypeAndStatusOrderByEventDateAsc(eventType, EventStatus.ACTIVE);
        return events.stream()
                .map(this::mapEventWithSeatCounts)
                .toList();
    }
    
    public List<EventResponse> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Event> events = eventRepository.findByDateRangeAndStatus(startDate, endDate, EventStatus.ACTIVE);
        return events.stream()
                .map(this::mapEventWithSeatCounts)
                .toList();
    }
    
    public List<EventResponse> getEventsByCity(String city) {
        List<Event> events = eventRepository.findByCityAndStatus(city, EventStatus.ACTIVE);
        return events.stream()
                .map(this::mapEventWithSeatCounts)
                .toList();
    }
    
    public List<EventResponse> searchEvents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveEvents();
        }
        
        List<Event> events = eventRepository.findByKeywordAndStatus(keyword.trim(), EventStatus.ACTIVE);
        return events.stream()
                .map(this::mapEventWithSeatCounts)
                .toList();
    }
    
    public List<String> getSearchSuggestions(String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return List.of();
        }
        
        return eventRepository.findTitleSuggestions(keyword.trim(), EventStatus.ACTIVE)
                .stream()
                .limit(5)
                .toList();
    }
    
    public Optional<EventResponse> getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .map(this::mapEventWithSeats);
    }
    
    public List<SeatResponse> getAvailableSeats(Long eventId) {
        return seatRepository.findByEventIdAndStatus(eventId, SeatStatus.AVAILABLE)
                .stream()
                .map(seatMapper::toResponse)
                .toList();
    }
    
    public List<EventResponse> advancedSearch(String keyword, EventType eventType, String city, 
                                            LocalDateTime startDate, LocalDateTime endDate) {
        List<Event> events = eventRepository.findByStatusOrderByEventDateAsc(EventStatus.ACTIVE);
        
        return events.stream()
                .filter(e -> keyword == null || keyword.trim().isEmpty() || 
                           matchesKeyword(e, keyword.trim()))
                .filter(e -> eventType == null || e.getEventType().equals(eventType))
                .filter(e -> city == null || city.trim().isEmpty() || 
                           e.getVenue().getCity().equalsIgnoreCase(city.trim()))
                .filter(e -> startDate == null || e.getEventDate().isAfter(startDate) || 
                           e.getEventDate().isEqual(startDate))
                .filter(e -> endDate == null || e.getEventDate().isBefore(endDate) || 
                           e.getEventDate().isEqual(endDate))
                .map(this::mapEventWithSeatCounts)
                .toList();
    }
    
    private boolean matchesKeyword(Event event, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return (event.getTitle() != null && event.getTitle().toLowerCase().contains(lowerKeyword)) ||
               (event.getDescription() != null && event.getDescription().toLowerCase().contains(lowerKeyword)) ||
               (event.getOrganizer() != null && event.getOrganizer().toLowerCase().contains(lowerKeyword)) ||
               (event.getVenue() != null && event.getVenue().getName() != null && 
                event.getVenue().getName().toLowerCase().contains(lowerKeyword));
    }
    
    private EventResponse mapEventWithSeatCounts(Event event) {
        EventResponse response = eventMapper.toResponse(event);
        
        long totalSeats = seatRepository.countByEventIdAndStatus(event.getId(), SeatStatus.AVAILABLE) +
                         seatRepository.countByEventIdAndStatus(event.getId(), SeatStatus.RESERVED) +
                         seatRepository.countByEventIdAndStatus(event.getId(), SeatStatus.BOOKED);
        long availableSeats = seatRepository.countByEventIdAndStatus(event.getId(), SeatStatus.AVAILABLE);
        
        return new EventResponse(
            response.id(),
            response.title(),
            response.description(),
            response.eventType(),
            response.venue(),
            response.eventDate(),
            response.eventEndDate(),
            response.bookingStartDate(),
            response.bookingEndDate(),
            response.basePrice(),
            response.imageUrl(),
            response.organizer(),
            response.status(),
            response.availableSeats(),
            availableSeats,
            totalSeats
        );
    }
    
    private EventResponse mapEventWithSeats(Event event) {
        EventResponse response = mapEventWithSeatCounts(event);
        List<SeatResponse> availableSeats = seatRepository.findByEventIdAndStatus(event.getId(), SeatStatus.AVAILABLE)
                .stream()
                .map(seatMapper::toResponse)
                .toList();
        
        return new EventResponse(
            response.id(),
            response.title(),
            response.description(),
            response.eventType(),
            response.venue(),
            response.eventDate(),
            response.eventEndDate(),
            response.bookingStartDate(),
            response.bookingEndDate(),
            response.basePrice(),
            response.imageUrl(),
            response.organizer(),
            response.status(),
            availableSeats,
            response.availableSeatCount(),
            response.totalSeatCount()
        );
    }
}