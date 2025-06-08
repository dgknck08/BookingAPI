package com.example.BookingApp.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.example.BookingApp.entity.Venue;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    
    // Basic search queries
    List<Venue> findByCity(String city);
    
    List<Venue> findByCountry(String country);
    
    List<Venue> findByCityAndCountry(String city, String country);
    
    Optional<Venue> findByName(String name);
    
    List<Venue> findByNameContainingIgnoreCase(String name);
    
    List<Venue> findByCapacityGreaterThanEqual(Integer minCapacity);
    
    List<Venue> findByCapacityLessThanEqual(Integer maxCapacity);
    
    List<Venue> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);
}