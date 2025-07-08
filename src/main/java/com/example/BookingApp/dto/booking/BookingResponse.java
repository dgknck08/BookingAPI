package com.example.BookingApp.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.BookingApp.entityenums.BookingStatus;

public record BookingResponse(
	    Long id,
	    Long eventId,
	    Long seatId,
	    String bookingReference,
	    BigDecimal totalAmount,
	    BookingStatus status,
	    LocalDateTime reservedUntil,
	    LocalDateTime bookedAt,
	    LocalDateTime confirmedAt
	) {}