package com.example.BookingApp.service;

import com.example.BookingApp.dto.booking.BookingDto;
import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entity.Booking;
import com.example.BookingApp.entity.Event;
import com.example.BookingApp.entity.Seat;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.entityenums.BookingStatus;
import com.example.BookingApp.entityenums.SeatStatus;
import com.example.BookingApp.mapper.BookingMapper;
import com.example.BookingApp.repository.BookingRepository;
import com.example.BookingApp.repository.EventRepository;
import com.example.BookingApp.repository.SeatRepository;
import com.example.BookingApp.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Transactional
    public BookingDto createReservation(BookingDto bookingDto, UserDto currentUser) {
        Event event = eventRepository.findById(bookingDto.getEventId())
            .orElseThrow(() -> new RuntimeException("Event with id " + bookingDto.getEventId() + " not found"));

        Seat seat = seatRepository.findById(bookingDto.getSeatId())
            .orElseThrow(() -> new RuntimeException("Seat with id " + bookingDto.getSeatId() + " not found"));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new RuntimeException("Seat with id " + seat.getId() + " is not available");
        }

        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        if (bookingRepository.existsBySeatIdAndStatusIn(seat.getId(), activeStatuses)) {
            throw new RuntimeException("Seat with id " + seat.getId() + " is already reserved or booked");
        }

        User user = userRepository.findByUsername(currentUser.getUsername())
            .orElseThrow(() -> new RuntimeException("User " + currentUser.getUsername() + " not found"));

        Booking booking = BookingMapper.toEntity(bookingDto, event, seat, user);

        // Update seat status
        seat.setStatus(SeatStatus.RESERVED);
        seatRepository.save(seat);

        booking = bookingRepository.save(booking);

        return BookingMapper.toDto(booking);
    }

    @Transactional
    public BookingDto confirmBooking(Long bookingId, UserDto currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking with id " + bookingId + " not found"));

        if (!booking.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new RuntimeException("Unauthorized access to booking with id " + bookingId);
        }

        if (booking.getReservedUntil().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reservation with id " + bookingId + " has expired");
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
            .orElseThrow(() -> new RuntimeException("Booking with id " + bookingId + " not found"));

        if (!booking.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new RuntimeException("Unauthorized access to booking with id " + bookingId);
        }

        booking.setStatus(BookingStatus.CANCELLED);

        booking.getSeat().setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(booking.getSeat());

        bookingRepository.save(booking);
    }

    public List<BookingDto> getUserBookings(UserDto currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
            .orElseThrow(() -> new RuntimeException("User " + currentUser.getUsername() + " not found"));

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
            booking.setStatus(BookingStatus.CANCELLED);
            booking.getSeat().setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(booking.getSeat());
            bookingRepository.save(booking);
        }
    }
}
