package com.example.BookingApp.controller;

import com.example.BookingApp.dto.booking.BookingCreateRequest;
import com.example.BookingApp.dto.booking.BookingResponse;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.exception.BookingException;
import com.example.BookingApp.exception.UserNotLoggedInException;
import com.example.BookingApp.service.AuthService;
import com.example.BookingApp.service.BookingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    
    private final BookingService bookingService;
    private final AuthService authService;
    
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getUserBookings(HttpSession session) {
        log.info("Fetching user bookings - Session ID: {}", session.getId());
        
        try {
            UserResponse currentUser = authService.getCurrentUser(session);
            if (currentUser == null) {
                log.warn("Unauthorized access attempt to user bookings - Session ID: {}", session.getId());
                throw new UserNotLoggedInException("Authentication required");
            }
            
            List<BookingResponse> bookings = bookingService.getUserBookings(currentUser.id());
            log.info("Successfully retrieved {} bookings for user ID: {}", bookings.size(), currentUser.id());
            
            return ResponseEntity.ok(bookings);
            
        } catch (UserNotLoggedInException e) {
            log.error("Authentication error while fetching bookings: {}", e.getMessage());
            throw e;
        } catch (BookingException e) {
            log.error("Booking service error while fetching user bookings: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching user bookings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve bookings: " + e.getMessage());
        }
    }
    
    @PostMapping("/reserve")
    public ResponseEntity<BookingResponse> createReservation(@Valid @RequestBody BookingCreateRequest request, HttpSession session) {
        log.info("Creating reservation - Event ID: {}, Seat ID: {}, Session ID: {}", 
                request.getEventId(), request.getSeatId(), session.getId());
        
        try {
            UserResponse currentUser = authService.getCurrentUser(session);
            if (currentUser == null) {
                log.warn("Unauthorized reservation attempt - Session ID: {}", session.getId());
                throw new UserNotLoggedInException("Authentication required");
            }
            
            request.setUserId(currentUser.id());
            BookingResponse createdBooking = bookingService.createReservation(request);
            
            log.info("Reservation created successfully - Booking ID: {}, Reference: {}, User ID: {}", 
                    createdBooking.id(), createdBooking.bookingReference(), currentUser.id());
            
         
            
            return ResponseEntity.ok(createdBooking);
            
        } catch (UserNotLoggedInException e) {
            log.error("Authentication error during reservation: {}", e.getMessage());
            throw e;
        } catch (BookingException e) {
            log.error("Booking error during reservation - Event ID: {}, Seat ID: {}, Error: {}", 
                    request.getEventId(), request.getSeatId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during reservation - Event ID: {}, Seat ID: {}, Error: {}", 
                    request.getEventId(), request.getSeatId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create reservation: " + e.getMessage());
        }
    }
    
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long bookingId, HttpSession session) {
        log.info("Confirming booking - Booking ID: {}, Session ID: {}", bookingId, session.getId());
        
        try {
            UserResponse currentUser = authService.getCurrentUser(session);
            if (currentUser == null) {
                log.warn("Unauthorized booking confirmation attempt - Booking ID: {}, Session ID: {}", 
                        bookingId, session.getId());
                throw new UserNotLoggedInException("Authentication required");
            }
            
            BookingResponse confirmedBooking = bookingService.confirmBooking(bookingId, currentUser.id());
            
            log.info("Booking confirmed successfully - Booking ID: {}, Reference: {}, User ID: {}", 
                    confirmedBooking.id(), confirmedBooking.bookingReference(), currentUser.id());
            
            
            return ResponseEntity.ok(confirmedBooking);
            
        } catch (UserNotLoggedInException e) {
            log.error("Authentication error during booking confirmation: {}", e.getMessage());
            throw e;
        } catch (BookingException e) {
            log.error("Booking error during confirmation - Booking ID: {}, Error: {}", bookingId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during booking confirmation - Booking ID: {}, Error: {}", 
                    bookingId, e.getMessage(), e);
            throw new RuntimeException("Failed to confirm booking: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<SuccessResponse> cancelBooking(@PathVariable Long bookingId, HttpSession session) {
        log.info("Cancelling booking - Booking ID: {}, Session ID: {}", bookingId, session.getId());
        
        try {
            UserResponse currentUser = authService.getCurrentUser(session);
            if (currentUser == null) {
                log.warn("Unauthorized booking cancellation attempt - Booking ID: {}, Session ID: {}", 
                        bookingId, session.getId());
                throw new UserNotLoggedInException("Authentication required");
            }
            
            bookingService.cancelBooking(bookingId, currentUser.id());
            
            log.info("Booking cancelled successfully - Booking ID: {}, User ID: {}", bookingId, currentUser.id());
            
            
            return ResponseEntity.ok(new SuccessResponse("Booking cancelled successfully"));
            
        } catch (UserNotLoggedInException e) {
            log.error("Authentication error during booking cancellation: {}", e.getMessage());
            throw e;
        } catch (BookingException e) {
            log.error("Booking error during cancellation - Booking ID: {}, Error: {}", bookingId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during booking cancellation - Booking ID: {}, Error: {}", 
                    bookingId, e.getMessage(), e);
            throw new RuntimeException("Failed to cancel booking: " + e.getMessage());
        }
    }
    
    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<BookingResponse> getBookingByReference(@PathVariable String bookingReference) {
        log.info("Fetching booking by reference - Reference: {}", bookingReference);
        
        try {
            Optional<BookingResponse> booking = bookingService.getBookingByReference(bookingReference);
            
            if (booking.isPresent()) {
                log.info("Booking found by reference - Reference: {}, Booking ID: {}", 
                        bookingReference, booking.get().id());
                return ResponseEntity.ok(booking.get());
            } else {
                log.warn("Booking not found by reference - Reference: {}", bookingReference);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error fetching booking by reference - Reference: {}, Error: {}", 
                    bookingReference, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve booking: " + e.getMessage());
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