package com.example.BookingApp.mapper;

import com.example.BookingApp.dto.event.response.VenueResponse;
import com.example.BookingApp.entity.Venue;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface VenueMapper {
    
    VenueResponse toResponse(Venue venue);
    
    List<VenueResponse> toResponseList(List<Venue> venues);
}
