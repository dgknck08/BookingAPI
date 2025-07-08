package com.example.BookingApp.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingCreateRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotNull(message = "Seat ID is required")
    private Long seatId;
    
    private Long userId;
}