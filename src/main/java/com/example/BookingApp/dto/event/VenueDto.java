package com.example.BookingApp.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VenueDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private Integer capacity;
    private String description;
    private String imageUrl;
}
