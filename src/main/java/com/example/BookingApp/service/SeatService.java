package com.example.BookingApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BookingApp.dto.event.SeatDto;
import com.example.BookingApp.entity.Event;
import com.example.BookingApp.entity.Seat;
import com.example.BookingApp.entityenums.SeatStatus;
import com.example.BookingApp.entityenums.SeatType;
import com.example.BookingApp.repository.EventRepository;
import com.example.BookingApp.repository.SeatRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeatService {
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Cacheable(value = "seats", key = "#eventId")
    public List<SeatDto> getSeatsByEvent(Long eventId) {
        List<Seat> seats = seatRepository.findByEventIdOrderBySectionAscRowNumberAscSeatNumberAsc(eventId);
        return seats.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Cacheable(value = "seats", key = "#eventId + '_' + #status")
    public List<SeatDto> getSeatsByEventAndStatus(Long eventId, SeatStatus status) {
        List<Seat> seats = seatRepository.findByEventIdAndStatus(eventId, status);
        return seats.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public Optional<SeatDto> getSeatById(Long seatId) {
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        return seatOpt.map(this::convertToDto);
    }
    
    @Transactional
    public List<SeatDto> generateSeatsForEvent(Long eventId, int rows, int seatsPerRow, BigDecimal basePrice) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            throw new RuntimeException("Event not found");
        }
        
        Event event = eventOpt.get();
        
        if (seatRepository.countByEventId(eventId) > 0) {
            throw new RuntimeException("Seats already exist for this event");
        }
        
        List<Seat> seats = generateSeatLayout(event, rows, seatsPerRow, basePrice);
        seats = seatRepository.saveAll(seats);
        
        return seats.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Transactional
    public SeatDto updateSeatStatus(Long seatId, SeatStatus newStatus) {
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        if (seatOpt.isEmpty()) {
            throw new RuntimeException("Seat not found");
        }
        
        Seat seat = seatOpt.get();
        seat.setStatus(newStatus);
        seat = seatRepository.save(seat);
        
        return convertToDto(seat);
    }
    
    @Transactional
    public SeatDto updateSeatPrice(Long seatId, BigDecimal newPrice) {
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        if (seatOpt.isEmpty()) {
            throw new RuntimeException("Seat not found");
        }
        
        Seat seat = seatOpt.get();
        seat.setPrice(newPrice);
        seat = seatRepository.save(seat);
        
        return convertToDto(seat);
    }
    
    public List<SeatDto> getSeatsBySection(Long eventId, String section) {
        List<Seat> seats = seatRepository.findByEventIdAndSectionOrderByRowNumberAscSeatNumberAsc(eventId, section);
        return seats.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<SeatDto> getSeatsByPriceRange(Long eventId, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Seat> seats = seatRepository.findByEventIdAndPriceBetweenOrderByPriceAsc(eventId, minPrice, maxPrice);
        return seats.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    private List<Seat> generateSeatLayout(Event event, int rows, int seatsPerRow, BigDecimal basePrice) {
        List<Seat> seats = new java.util.ArrayList<>();
        
        for (int row = 1; row <= rows; row++) {
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                Seat seat = new Seat();
                seat.setEvent(event);
                seat.setSeatNumber(String.format("%02d", seatNum));
                seat.setRowNumber(String.format("%c", 'A' + row - 1));
                seat.setSection(determineSectionByRow(row, rows));
                seat.setSeatType(determineSeatType(row, rows));
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setPrice(calculateSeatPrice(basePrice, row, rows));
                seat.setXPosition(seatNum * 50); // For visualization
                seat.setYPosition(row * 40); // For visualization
                
                seats.add(seat);
            }
        }
        
        return seats;
    }
    
    private String determineSectionByRow(int row, int totalRows) {
        if (row <= totalRows / 3) {
            return "VIP";
        } else if (row <= (totalRows * 2) / 3) {
            return "PREMIUM";
        } else {
            return "GENERAL";
        }
    }
    
    private SeatType determineSeatType(int row, int totalRows) {
        if (row <= totalRows / 3) {
            return SeatType.VIP;
        } else if (row <= (totalRows * 2) / 3) {
            return SeatType.PREMIUM;
        } else {
            return SeatType.STANDARD;
        }
    }
    
    private BigDecimal calculateSeatPrice(BigDecimal basePrice, int row, int totalRows) {
        if (row <= totalRows / 3) {
            return basePrice.multiply(new BigDecimal("2.0")); 
        } else if (row <= (totalRows * 2) / 3) {
            return basePrice.multiply(new BigDecimal("1.5")); 
        } else {
            return basePrice; 
        }
    }
    
    private SeatDto convertToDto(Seat seat) {
        SeatDto dto = new SeatDto();
        dto.setId(seat.getId());
        dto.setEventId(seat.getEvent().getId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setRowNumber(seat.getRowNumber());
        dto.setSection(seat.getSection());
        dto.setSeatType(seat.getSeatType());
        dto.setStatus(seat.getStatus());
        dto.setPrice(seat.getPrice());
        dto.setXPosition(seat.getXPosition());
        dto.setYPosition(seat.getYPosition());
        return dto;
    }
}

