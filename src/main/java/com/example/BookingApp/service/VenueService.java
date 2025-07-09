package com.example.BookingApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.BookingApp.dto.event.response.VenueResponse;
import com.example.BookingApp.entity.Venue;
import com.example.BookingApp.repository.VenueRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VenueService {
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Cacheable(value = "venues")
    public List<VenueResponse> getAllVenues() {
        List<Venue> venues = venueRepository.findAll();
        return venues.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    @Cacheable(value = "venues", key = "#venueId")
    public Optional<VenueResponse> getVenueById(Long venueId) {
        return venueRepository.findById(venueId).map(this::convertToResponse);
    }
    
    @Cacheable(value = "venues", key = "#city")
    public List<VenueResponse> getVenuesByCity(String city) {
        List<Venue> venues = venueRepository.findByCityIgnoreCase(city);
        return venues.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    public List<VenueResponse> searchVenues(String keyword) {
        List<Venue> venues = venueRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(keyword, keyword);
        return venues.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    public List<VenueResponse> getVenuesByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        List<Venue> venues = venueRepository.findByCapacityBetween(minCapacity, maxCapacity);
        return venues.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    private VenueResponse convertToResponse(Venue venue) {
        return new VenueResponse(
            venue.getId(),
            venue.getName(),
            venue.getAddress(),
            venue.getCity(),
            venue.getCountry(),
            venue.getCapacity(),
            venue.getDescription(),
            venue.getImageUrl()
        );
    }
}
