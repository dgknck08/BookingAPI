package com.example.BookingApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import com.example.BookingApp.entityenums.SeatStatus;
import com.example.BookingApp.entityenums.SeatType;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    
    private String seatNumber;
    private String rowNumber;
    private String section;
    
    @Enumerated(EnumType.STRING)
    private SeatType seatType;
    
    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;
    
    private BigDecimal price;
    private Integer xPosition; // For seat map visualization
    private Integer yPosition; // For seat map visualization
}
