package com.example.BookingApp.dto.event.response;

import com.example.BookingApp.entityenums.EventStatus;
import com.example.BookingApp.entityenums.EventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
    Long id,
    String title,
    String description,
    EventType eventType,
    VenueResponse venue,
    LocalDateTime eventDate,
    LocalDateTime eventEndDate,
    LocalDateTime bookingStartDate,
    LocalDateTime bookingEndDate,
    BigDecimal basePrice,
    String imageUrl,
    String organizer,
    EventStatus status,
    List<SeatResponse> availableSeats,
    long availableSeatCount,
    long totalSeatCount
) {}