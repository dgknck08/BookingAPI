package com.example.BookingApp.dto.event;

import com.example.BookingApp.entityenums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventCreateRequest {
    @NotBlank(message = "Event title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Event type is required")
    private EventType eventType;
    
    @NotNull(message = "Venue is required")
    private Long venueId;
    
    @NotNull(message = "Event date is required")
    private LocalDateTime eventDate;
    
    private LocalDateTime eventEndDate;
    private LocalDateTime bookingStartDate;
    private LocalDateTime bookingEndDate;
    
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;
    
    private String imageUrl;
    private String organizer;
}