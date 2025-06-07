package com.example.BookingApp.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDto {
    private Long id;
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    @NotNull(message = "Seat ID is required")
    private Long seatId;
    
    private String bookingReference;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime reservedUntil;
    private LocalDateTime bookedAt;
    private LocalDateTime confirmedAt;
}
