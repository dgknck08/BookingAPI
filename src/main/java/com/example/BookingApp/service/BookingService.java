package com.example.BookingApp.service;

import com.example.BookingApp.dto.booking.BookingCreateRequest;
import com.example.BookingApp.dto.booking.BookingResponse;
import com.example.BookingApp.exception.BookingException;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    
    List<BookingResponse> getUserBookings(Long userId);
    
    BookingResponse createReservation(BookingCreateRequest request) throws BookingException;
    
    BookingResponse confirmBooking(Long bookingId, Long userId) throws BookingException;
    
    void cancelBooking(Long bookingId, Long userId) throws BookingException;
    
    Optional<BookingResponse> getBookingByReference(String bookingReference);
}

