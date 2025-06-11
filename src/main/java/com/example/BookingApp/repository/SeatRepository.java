package com.example.BookingApp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.BookingApp.entity.Seat;
import com.example.BookingApp.entityenums.SeatStatus;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

	List<Seat> findByEventIdAndPriceBetweenOrderByPriceAsc(Long eventId, BigDecimal minPrice, BigDecimal maxPrice);

	List<Seat> findByEventIdAndSectionOrderByRowNumberAscSeatNumberAsc(Long eventId, String section);

	long countByEventId(Long eventId);
    
    List<Seat> findByEventIdOrderBySectionAscRowNumberAscSeatNumberAsc(Long eventId);
    
    List<Seat> findByEventIdAndStatus(Long eventId, SeatStatus status);
    
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.event.id = :eventId AND s.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") SeatStatus status);
    
    @Modifying
    @Query("UPDATE Seat s SET s.status = :newStatus WHERE s.id = :seatId")
    int updateSeatStatus(@Param("seatId") Long seatId, @Param("newStatus") SeatStatus newStatus);
    
    
    
}