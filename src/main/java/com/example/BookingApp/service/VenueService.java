package com.example.BookingApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.BookingApp.dto.event.VenueDto;
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
    public List<VenueDto> getAllVenues() {
        List<Venue> venues = venueRepository.findAll();
        return venues.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Cacheable(value = "venues", key = "#venueId")
    public Optional<VenueDto> getVenueById(Long venueId) {
        Optional<Venue> venueOpt = venueRepository.findById(venueId);
        return venueOpt.map(this::convertToDto);
    }
    
    @Cacheable(value = "venues", key = "#city")
    public List<VenueDto> getVenuesByCity(String city) {
        List<Venue> venues = venueRepository.findByCityIgnoreCase(city);
        return venues.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<VenueDto> searchVenues(String keyword) {
        List<Venue> venues = venueRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(keyword, keyword);
        return venues.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<VenueDto> getVenuesByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        List<Venue> venues = venueRepository.findByCapacityBetween(minCapacity, maxCapacity);
        return venues.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    private VenueDto convertToDto(Venue venue) {
        VenueDto dto = new VenueDto();
        dto.setId(venue.getId());
        dto.setName(venue.getName());
        dto.setAddress(venue.getAddress());
        dto.setCity(venue.getCity());
        dto.setCountry(venue.getCountry());
        dto.setCapacity(venue.getCapacity());
        dto.setDescription(venue.getDescription());
        dto.setImageUrl(venue.getImageUrl());
        return dto;
    }
}
