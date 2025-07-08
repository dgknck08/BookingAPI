package com.example.BookingApp.dto.event.response;

import java.math.BigDecimal;

import com.example.BookingApp.entityenums.SeatStatus;
import com.example.BookingApp.entityenums.SeatType;

public record SeatResponse(
	    Long id,
	    Long eventId,
	    String seatNumber,
	    String rowNumber,
	    String section,
	    SeatType seatType,
	    SeatStatus status,
	    BigDecimal price,
	    Integer xPosition,
	    Integer yPosition
	) {}