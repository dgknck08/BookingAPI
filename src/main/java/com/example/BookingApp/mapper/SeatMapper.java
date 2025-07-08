package com.example.BookingApp.mapper;


import com.example.BookingApp.dto.event.response.SeatResponse;
import com.example.BookingApp.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {
    
    @Mapping(target = "eventId", source = "event.id")
    SeatResponse toResponse(Seat seat);
    
    List<SeatResponse> toResponseList(List<Seat> seats);
}