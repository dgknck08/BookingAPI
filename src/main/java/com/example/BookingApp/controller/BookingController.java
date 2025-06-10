package com.example.BookingApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.BookingApp.dto.booking.BookingDto;
import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.service.AuthService;
import com.example.BookingApp.service.BookingService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private AuthService authService;

    @GetMapping
    public ResponseEntity<List<BookingDto>> getUserBookings(HttpSession session) {
        UserDto currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<BookingDto> bookings = bookingService.getUserBookings(currentUser);
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/reserve")
    public ResponseEntity<BookingDto> createReservation(@Valid @RequestBody BookingDto bookingDto, HttpSession session) {
        UserDto currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            BookingDto createdBooking = bookingService.createReservation(bookingDto, currentUser);
            return ResponseEntity.ok(createdBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingDto> confirmBooking(@PathVariable Long bookingId, HttpSession session) {
        UserDto currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            BookingDto confirmedBooking = bookingService.confirmBooking(bookingId, currentUser);
            return ResponseEntity.ok(confirmedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId, HttpSession session) {
        UserDto currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            bookingService.cancelBooking(bookingId, currentUser);
            return ResponseEntity.ok("Booking cancelled successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<BookingDto> getBookingByReference(@PathVariable String bookingReference) {
        Optional<BookingDto> booking = bookingService.getBookingByReference(bookingReference);
        return booking.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
}
