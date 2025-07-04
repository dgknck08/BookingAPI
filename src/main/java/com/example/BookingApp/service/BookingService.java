package com.example.BookingApp.service;

import com.example.BookingApp.dto.booking.BookingDto;
import com.example.BookingApp.dto.user.UserDto;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingDto createReservation(BookingDto bookingDto, UserDto currentUser) {
        // Validasyonlar
        Event event = validateEvent(bookingDto.getEventId());
        Seat seat = validateSeat(bookingDto.getSeatId(), event.getId());
        User user = validateUser(currentUser.getUsername());
        
        // Event geçmiş tarihli mi kontrolü
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BookingException("Cannot book seats for past events");
        }
        
        validateSeatAvailability(seat);
        
        // Kullanıcının bu event için mevcut rezervasyonu var mı?
        checkExistingUserBooking(user.getId(), event.getId());

        try {
            // Booking 
            Booking booking = BookingMapper.toEntity(bookingDto, event, seat, user);
            
            // Seat durumu
            seat.setStatus(SeatStatus.RESERVED);
            seatRepository.save(seat);
            
            booking = bookingRepository.save(booking);
            
            return BookingMapper.toDto(booking);
            
        } catch (Exception e) {

            seat.setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(seat);
            throw new BookingException("Failed to create reservation: " + e.getMessage());
        }
    }

    @Transactional
    public BookingDto confirmBooking(Long bookingId, UserDto currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingException("Booking not found"));

        validateBookingOwnership(booking, currentUser.getUsername());
        
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException("Only pending bookings can be confirmed");
        }

        if (booking.getReservedUntil().isBefore(LocalDateTime.now())) {

            cancelExpiredBooking(booking);
            throw new BookingException("Reservation has expired");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        booking.getSeat().setStatus(SeatStatus.BOOKED);
        
        seatRepository.save(booking.getSeat());
        booking = bookingRepository.save(booking);

        return BookingMapper.toDto(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId, UserDto currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingException("Booking not found"));

        validateBookingOwnership(booking, currentUser.getUsername());
        
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.getSeat().setStatus(SeatStatus.AVAILABLE);
        
        seatRepository.save(booking.getSeat());
        bookingRepository.save(booking);
    }

    public List<BookingDto> getUserBookings(UserDto currentUser) {
        User user = validateUser(currentUser.getUsername());
        
        List<Booking> bookings = bookingRepository.findByUserOrderByBookedAtDesc(user);
        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<BookingDto> getBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .map(BookingMapper::toDto);
    }

    @Transactional
    public void cleanupExpiredReservations() {
        List<Booking> expiredBookings = bookingRepository.findExpiredReservations(
                LocalDateTime.now(), BookingStatus.PENDING);

        for (Booking booking : expiredBookings) {
            cancelExpiredBooking(booking);
        }
    }
    

    private Event validateEvent(Long eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new BookingException("Event not found"));
    }
    
    private Seat validateSeat(Long seatId, Long eventId) {
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new BookingException("Seat not found"));
            

        if (!seat.getEvent().getId().equals(eventId)) {
            throw new BookingException("Seat does not belong to the specified event");
        }
        
        return seat;
    }
    
    private User validateUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new BookingException("User not found"));
    }
    
    private void validateSeatAvailability(Seat seat) {
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new BookingException("Seat is not available");
        }

        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        if (bookingRepository.existsBySeatIdAndStatusIn(seat.getId(), activeStatuses)) {
            throw new BookingException("Seat is already reserved or booked");
        }
    }
    
    private void validateBookingOwnership(Booking booking, String username) {
        if (!booking.getUser().getUsername().equals(username)) {
            throw new BookingException("Unauthorized access to booking");
        }
    }
    
    private void checkExistingUserBooking(Long userId, Long eventId) {
        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        boolean hasExistingBooking = bookingRepository.existsByUserIdAndEventIdAndStatusIn(
            userId, eventId, activeStatuses);
            
        if (hasExistingBooking) {
            throw new BookingException("User already has an active booking for this event");
        }
    }
    
    private void cancelExpiredBooking(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
        booking.getSeat().setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(booking.getSeat());
        bookingRepository.save(booking);
    }
}