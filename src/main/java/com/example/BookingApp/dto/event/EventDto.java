package com.example.BookingApp.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.BookingApp.entityenums.EventStatus;
import com.example.BookingApp.entityenums.EventType;


@Getter
@Setter

@NoArgsConstructor
public class EventDto {
    
    @NotBlank(message = "Event title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Event type is required")
    private EventType eventType;
    
    @NotNull(message = "Venue is required")
    private Long venueId;
    
    private VenueDto venue;
    
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
    private EventStatus status;
    private List<SeatDto> availableSeats;
    private long availableSeatCount;
    private long totalSeatCount;
}
