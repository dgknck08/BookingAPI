package com.example.BookingApp.mapper;

import com.example.BookingApp.dto.booking.BookingCreateRequest;
import com.example.BookingApp.dto.booking.BookingResponse;
import com.example.BookingApp.entity.Booking;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "seatId", source = "seat.id")
    BookingResponse toResponse(Booking booking);
    
    List<BookingResponse> toResponseList(List<Booking> bookings);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true) 
    @Mapping(target = "seat", ignore = true) 
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "bookingReference", ignore = true) 
    @Mapping(target = "totalAmount", ignore = true) 
    @Mapping(target = "status", ignore = true) 
    @Mapping(target = "reservedUntil", ignore = true)
    @Mapping(target = "bookedAt", ignore = true) 
    @Mapping(target = "confirmedAt", ignore = true)
    Booking toEntity(BookingCreateRequest request);
}

