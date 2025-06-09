package com.example.BookingApp.dto.event;


import java.math.BigDecimal;

import com.example.BookingApp.entityenums.SeatStatus;
import com.example.BookingApp.entityenums.SeatType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatDto {
    private Long id;
    private Long eventId;
    private String seatNumber;
    private String rowNumber;
    private String section;
    private SeatType seatType;
    private SeatStatus status;
    private BigDecimal price;
    private Integer xPosition;
    private Integer yPosition;	
}