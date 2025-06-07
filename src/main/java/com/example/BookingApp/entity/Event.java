package com.example.BookingApp.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    
    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;
    
    private LocalDateTime eventDate;
    private LocalDateTime eventEndDate;
    private LocalDateTime bookingStartDate;
    private LocalDateTime bookingEndDate;
    
    private BigDecimal basePrice;
    private String imageUrl;
    private String organizer;
    
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.ACTIVE;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
