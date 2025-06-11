package com.example.BookingApp.mapper;

import java.time.LocalDateTime;

import com.example.BookingApp.dto.booking.BookingDto;
import com.example.BookingApp.entity.Booking;
import com.example.BookingApp.entity.Event;
import com.example.BookingApp.entity.Seat;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.entityenums.BookingStatus;

public class BookingMapper {

    public static BookingDto toDto(Booking booking) {
        if (booking == null) return null;

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setEventId(booking.getEvent().getId());
        dto.setSeatId(booking.getSeat().getId());
        dto.setBookingReference(booking.getBookingReference());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus());
        dto.setReservedUntil(booking.getReservedUntil());
        dto.setBookedAt(booking.getBookedAt());
        dto.setConfirmedAt(booking.getConfirmedAt());
        return dto;
    }

    public static Booking toEntity(BookingDto dto, Event event, Seat seat, User user) {
        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setSeat(seat);
        booking.setUser(user);
        booking.setTotalAmount(seat.getPrice());
        booking.setStatus(BookingStatus.PENDING);
        booking.setReservedUntil(LocalDateTime.now().plusMinutes(15));
        return booking;
    }
}

