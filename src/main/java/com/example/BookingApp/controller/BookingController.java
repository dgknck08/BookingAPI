package com.example.BookingApp.controller;

import com.example.BookingApp.dto.booking.BookingCreateRequest;
import com.example.BookingApp.dto.booking.BookingResponse;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.exception.BookingException;
import com.example.BookingApp.service.AuthService;
import com.example.BookingApp.service.BookingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;
    private final AuthService authService;
    
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getUserBookings(HttpSession session) {
        UserResponse currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            List<BookingResponse> bookings = bookingService.getUserBookings(currentUser.id());
            return ResponseEntity.ok(bookings);
        } catch (BookingException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/reserve")
    public ResponseEntity<?> createReservation(@Valid @RequestBody BookingCreateRequest request, HttpSession session) {
        UserResponse currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        try {
            request.setUserId(currentUser.id());
            BookingResponse createdBooking = bookingService.createReservation(request);
            return ResponseEntity.ok(createdBooking);
        } catch (BookingException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Internal server error"));
        }
    }
    
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Long bookingId, HttpSession session) {
        UserResponse currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        try {
            BookingResponse confirmedBooking = bookingService.confirmBooking(bookingId, currentUser.id());
            return ResponseEntity.ok(confirmedBooking);
        } catch (BookingException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Internal server error"));
        }
    }
    
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId, HttpSession session) {
        UserResponse currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        
        try {
            bookingService.cancelBooking(bookingId, currentUser.id());
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
            Optional<BookingResponse> booking = bookingService.getBookingByReference(bookingReference);
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