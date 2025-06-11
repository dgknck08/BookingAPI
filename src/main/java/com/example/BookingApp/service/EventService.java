package com.example.BookingApp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.BookingApp.dto.event.EventDto;
import com.example.BookingApp.dto.event.SeatDto;
import com.example.BookingApp.dto.event.VenueDto;
import com.example.BookingApp.entity.Event;
import com.example.BookingApp.entity.Seat;
import com.example.BookingApp.entityenums.EventStatus;
import com.example.BookingApp.entityenums.EventType;
import com.example.BookingApp.entityenums.SeatStatus;
import com.example.BookingApp.repository.EventRepository;
import com.example.BookingApp.repository.SeatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    public List<EventDto> getAllActiveEvents() {
        List<Event> events = eventRepository.findByStatusOrderByEventDateAsc(EventStatus.ACTIVE);
        return events.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<EventDto> getEventsByType(EventType eventType) {
        List<Event> events = eventRepository.findByEventTypeAndStatusOrderByEventDateAsc(eventType, EventStatus.ACTIVE);
        return events.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<EventDto> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Event> events = eventRepository.findByDateRangeAndStatus(startDate, endDate, EventStatus.ACTIVE);
        return events.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<EventDto> getEventsByCity(String city) {
        List<Event> events = eventRepository.findByCityAndStatus(city, EventStatus.ACTIVE);
        return events.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<EventDto> searchEvents(String keyword) {
        List<Event> events = eventRepository.findByTitleOrDescriptionContainingIgnoreCase(keyword);
        return events.stream()
                .filter(e -> e.getStatus() == EventStatus.ACTIVE)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<EventDto> getEventById(Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        return eventOpt.map(this::convertToDtoWithSeats);
    }
    
    public List<SeatDto> getAvailableSeats(Long eventId) {
        List<Seat> seats = seatRepository.findByEventIdAndStatus(eventId, SeatStatus.AVAILABLE);
        return seats.stream().map(this::convertSeatToDto).collect(Collectors.toList());
    }
    
    private EventDto convertToDto(Event event) {
        EventDto dto = new EventDto();
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setEventType(event.getEventType());
        dto.setVenueId(event.getVenue().getId());
        dto.setVenue(convertVenueToDto(event.getVenue()));
        dto.setEventDate(event.getEventDate());
        dto.setEventEndDate(event.getEventEndDate());
        dto.setBookingStartDate(event.getBookingStartDate());
        dto.setBookingEndDate(event.getBookingEndDate());
        dto.setBasePrice(event.getBasePrice());
        dto.setImageUrl(event.getImageUrl());
        dto.setOrganizer(event.getOrganizer());
        dto.setStatus(event.getStatus());
        
        // Set seat counts
        long totalSeats = seatRepository.countByEventIdAndStatus(event.getId(), SeatStatus.AVAILABLE) +
                         seatRepository.countByEventIdAndStatus(event.getId(), SeatStatus.RESERVED) +
                         seatRepository.countByEventIdAndStatus(event.getId(), SeatStatus.BOOKED);
        long availableSeats = seatRepository.countByEventIdAndStatus(event.getId(), SeatStatus.AVAILABLE);
        
        dto.setTotalSeatCount(totalSeats);
        dto.setAvailableSeatCount(availableSeats);
        
        return dto;
    }
    
    private EventDto convertToDtoWithSeats(Event event) {
        EventDto dto = convertToDto(event);
        List<Seat> availableSeats = seatRepository.findByEventIdAndStatus(event.getId(), SeatStatus.AVAILABLE);
        dto.setAvailableSeats(availableSeats.stream().map(this::convertSeatToDto).collect(Collectors.toList()));
        return dto;
    }
    
    private VenueDto convertVenueToDto(com.example.BookingApp.entity.Venue venue) {
        VenueDto dto = new VenueDto();
        dto.setId(venue.getId());
        dto.setName(venue.getName());
        dto.setAddress(venue.getAddress());
        dto.setCity(venue.getCity());
        dto.setCountry(venue.getCountry());
        dto.setCapacity(venue.getCapacity());
        dto.setDescription(venue.getDescription());
        dto.setImageUrl(venue.getImageUrl());
        return dto;
    }
    
    private SeatDto convertSeatToDto(Seat seat) {
        SeatDto dto = new SeatDto();
        dto.setId(seat.getId());
        dto.setEventId(seat.getEvent().getId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setRowNumber(seat.getRowNumber());
        dto.setSection(seat.getSection());
        dto.setSeatType(seat.getSeatType());
        dto.setStatus(seat.getStatus());
        dto.setPrice(seat.getPrice());
        dto.setXPosition(seat.getXPosition());
        dto.setYPosition(seat.getYPosition());
        return dto;
    }
}