package com.example.BookingApp.dto.event.response;

public record VenueResponse(
	    Long id,
	    String name,
	    String address,
	    String city,
	    String country,
	    Integer capacity,
	    String description,
	    String imageUrl
	) {}