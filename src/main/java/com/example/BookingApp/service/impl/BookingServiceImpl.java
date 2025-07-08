package com.example.BookingApp.service.impl;

import com.example.BookingApp.dto.booking.BookingCreateRequest;
import com.example.BookingApp.dto.booking.BookingResponse;
import com.example.BookingApp.entity.Booking;
import com.example.BookingApp.entity.Event;
import com.example.BookingApp.entity.Seat;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.entityenums.BookingStatus;
import com.example.BookingApp.entityenums.SeatStatus;
import com.example.BookingApp.exception.BookingException;
import com.example.BookingApp.mapper.BookingMapper;
import com.example.BookingApp.repository.BookingRepository;
import com.example.BookingApp.repository.EventRepository;
import com.example.BookingApp.repository.SeatRepository;
import com.example.BookingApp.repository.UserRepository;
import com.example.BookingApp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookedAtDesc(userId);
        return bookingMapper.toResponseList(bookings);
    }
    
    @Override
    public BookingResponse createReservation(BookingCreateRequest request) throws BookingException {
        // Validate event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new BookingException("Event not found"));
        
        // Validate seat exists and is available
        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new BookingException("Seat not found"));
        
        if (!seat.getEvent().getId().equals(request.getEventId())) {
            throw new BookingException("Seat does not belong to this event");
        }
        
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new BookingException("Seat is not available");
        }
        
        // Validate user exists (if provided)
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new BookingException("User not found"));
        }
        
        // Check booking period
        LocalDateTime now = LocalDateTime.now();
        if (event.getBookingStartDate() != null && now.isBefore(event.getBookingStartDate())) {
            throw new BookingException("Booking period has not started yet");
        }
        
        if (event.getBookingEndDate() != null && now.isAfter(event.getBookingEndDate())) {
            throw new BookingException("Booking period has ended");
        }
        
        // Create booking
        Booking booking = bookingMapper.toEntity(request);
        booking.setEvent(event);
        booking.setSeat(seat);
        booking.setUser(user);
        booking.setBookingReference(generateBookingReference());
        booking.setTotalAmount(seat.getPrice());
        booking.setStatus(BookingStatus.RESERVED);
        booking.setReservedUntil(now.plusMinutes(15)); 
        booking.setBookedAt(now);
        
        seat.setStatus(SeatStatus.RESERVED);
        seatRepository.save(seat);
        
        booking = bookingRepository.save(booking);
        
        return bookingMapper.toResponse(booking);
    }
    
    @Override
    public BookingResponse confirmBooking(Long bookingId, Long userId) throws BookingException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found"));
        
        if (booking.getUser() == null || !booking.getUser().getId().equals(userId)) {
            throw new BookingException("You are not authorized to confirm this booking");
        }
        
        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new BookingException("Booking cannot be confirmed");
        }
        
        if (booking.getReservedUntil().isBefore(LocalDateTime.now())) {
            // Cancel expired reservation
            booking.setStatus(BookingStatus.CANCELLED);
            booking.getSeat().setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(booking.getSeat());
            bookingRepository.save(booking);
            throw new BookingException("Reservation has expired");
        }
        
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        booking.getSeat().setStatus(SeatStatus.BOOKED);
        
        seatRepository.save(booking.getSeat());
        booking = bookingRepository.save(booking);
        
        return bookingMapper.toResponse(booking);
    }
    
    @Override
    public void cancelBooking(Long bookingId, Long userId) throws BookingException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found"));
        
        if (booking.getUser() == null || !booking.getUser().getId().equals(userId)) {
            throw new BookingException("You are not authorized to cancel this booking");
        }
        
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        booking.getSeat().setStatus(SeatStatus.AVAILABLE);
        
        seatRepository.save(booking.getSeat());
        bookingRepository.save(booking);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BookingResponse> getBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .map(bookingMapper::toResponse);
    }
    
    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}