package com.example.BookingApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.BookingApp.dto.booking.BookingDto;
import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.exception.BookingException;
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
        
        try {
            List<BookingDto> bookings = bookingService.getUserBookings(currentUser);
            return ResponseEntity.ok(bookings);
        } catch (BookingException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/reserve")
    public ResponseEntity<?> createReservation(@Valid @RequestBody BookingDto bookingDto, HttpSession session) {
        UserDto currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        try {
            BookingDto createdBooking = bookingService.createReservation(bookingDto, currentUser);
            return ResponseEntity.ok(createdBooking);
        } catch (BookingException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Internal server error"));
        }
    }
    
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Long bookingId, HttpSession session) {
        UserDto currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        try {
            BookingDto confirmedBooking = bookingService.confirmBooking(bookingId, currentUser);
            return ResponseEntity.ok(confirmedBooking);
        } catch (BookingException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Internal server error"));
        }
    }
    
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId, HttpSession session) {
        UserDto currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        try {
            bookingService.cancelBooking(bookingId, currentUser);
            return ResponseEntity.ok(new SuccessResponse("Booking cancelled successfully"));
        } catch (BookingException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Internal server error"));
        }
    }
    
    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<?> getBookingByReference(@PathVariable String bookingReference) {
        try {
            Optional<BookingDto> booking = bookingService.getBookingByReference(bookingReference);
            return booking.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Internal server error"));
        }
    }
    
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
    
    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}