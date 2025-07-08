package com.example.BookingApp.mapper;
import com.example.BookingApp.dto.event.*;
import com.example.BookingApp.dto.event.response.EventResponse;
import com.example.BookingApp.entity.Event;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {VenueMapper.class, SeatMapper.class})
public interface EventMapper {
    
    // Entity -> Response (for read operations)
    @Mapping(target = "venue", source = "venue")
    @Mapping(target = "availableSeats", ignore = true) 
    @Mapping(target = "availableSeatCount", ignore = true) 
    @Mapping(target = "totalSeatCount", ignore = true) 
    EventResponse toResponse(Event event);
    
    List<EventResponse> toResponseList(List<Event> events);
    

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "venue", ignore = true) 
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(EventCreateRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(EventCreateRequest request, @MappingTarget Event event);
}