package com.example.BookingApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BookingApp.entity.Profile;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.entityenums.Gender;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    
    Optional<Profile> findByUser(User user);
    
    Optional<Profile> findByUserId(Long userId);
    
    List<Profile> findByCity(String city);
    
    List<Profile> findByCountry(String country);
    
    List<Profile> findByCityAndCountry(String city, String country);
    
    List<Profile> findByGender(Gender gender);
    
    @Query("SELECT p FROM Profile p WHERE p.firstName LIKE %:name% OR p.lastName LIKE %:name%")
    List<Profile> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT p FROM Profile p WHERE p.dateOfBirth >= :startDate AND p.dateOfBirth <= :endDate")
    List<Profile> findByDateOfBirthBetween(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT p FROM Profile p WHERE p.phoneNumber = :phoneNumber")
    Optional<Profile> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    @Query("SELECT COUNT(p) FROM Profile p WHERE p.city = :city")
    long countByCity(@Param("city") String city);
    
    @Query("SELECT COUNT(p) FROM Profile p WHERE p.country = :country")
    long countByCountry(@Param("country") String country);
    
    boolean existsByPhoneNumber(String phoneNumber);
}